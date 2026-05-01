package kefirdlc.dev.util.render.postfx;
// coded by sitoku \\
// since 27.04.2026 \\

import org.lwjgl.opengl.*;
import kefirdlc.dev.util.render.backends.gl.GlState;
import kefirdlc.dev.util.render.backends.gl.ShaderProgram;
import kefirdlc.dev.util.render.core.RenderFrameMetrics;

/**
 * Dual Kawase blur implementation optimized for screen-space blurs.
 * Creates a downsample pyramid and reconstructs the blurred image on the final pass.
 */
public final class DownsampleBlur {
    private static final int MAX_LEVELS = 6;
    private static final float MIN_RADIUS = 0.5f;
    private static final float SMALL_RADIUS_THRESHOLD = 3.5f;

    /**
     * Returns the minimum supported blur radius, below which the shader kernels become unstable.
     */
    public float minimumRadius() {
        return MIN_RADIUS;
    }

    /**
     * Returns the radius threshold where the implementation switches from the small kernel path
     * to the downsampled pyramid.
     */
    public float smallKernelThreshold() {
        return SMALL_RADIUS_THRESHOLD;
    }

    private static final class LevelTarget {
        int fbo;
        int texture;
        int width;
        int height;
    }

    private final ShaderProgram downsampleProgram;
    private final ShaderProgram upsampleProgram;
    private final ShaderProgram smallHProgram;
    private final ShaderProgram smallVProgram;
    private final int intermediateInternalFormat;
    private final int intermediatePixelType;
    private final int downSamplerLoc;
    private final int downTexelSizeLoc;
    private final int downOffsetLoc;
    private final int upSamplerLoc;
    private final int upTexelSizeLoc;
    private final int upOffsetLoc;
    private final int smallHSamplerLoc;
    private final int smallHTexelSizeLoc;
    private final int smallHRadiusLoc;
    private final int smallVSamplerLoc;
    private final int smallVTexelSizeLoc;
    private final int smallVRadiusLoc;

    private int quadVao;
    private int quadVbo;

    private final LevelTarget[] pyramid = new LevelTarget[MAX_LEVELS];
    private final LevelTarget fullResolutionTarget = new LevelTarget();
    private final LevelTarget smallTempTarget = new LevelTarget();

    public DownsampleBlur() {
        this(GL30.GL_RGBA16F, GL11.GL_FLOAT);
    }

    public DownsampleBlur(int intermediateInternalFormat, int intermediatePixelType) {
        if (intermediateInternalFormat == 0) {
            throw new IllegalArgumentException("intermediateInternalFormat must be a valid OpenGL format constant");
        }
        if (intermediatePixelType == 0) {
            throw new IllegalArgumentException("intermediatePixelType must be a valid OpenGL pixel type constant");
        }
        this.downsampleProgram = ShaderProgram.fromResources(
                "assets/hysteria/shaders/blur/blur_fullscreen.vert",
                "assets/hysteria/shaders/blur/blur_downsample.frag");
        this.upsampleProgram = ShaderProgram.fromResources(
                "assets/hysteria/shaders/blur/blur_fullscreen.vert",
                "assets/hysteria/shaders/blur/blur_upsample.frag");
        this.smallHProgram = ShaderProgram.fromResources(
                "assets/hysteria/shaders/blur/blur_fullscreen.vert",
                "assets/hysteria/shaders/blur/blur_small_horizontal.frag");
        this.smallVProgram = ShaderProgram.fromResources(
                "assets/hysteria/shaders/blur/blur_fullscreen.vert",
                "assets/hysteria/shaders/blur/blur_small_vertical.frag");

        this.intermediateInternalFormat = intermediateInternalFormat;
        this.intermediatePixelType = intermediatePixelType;

        this.downSamplerLoc = downsampleProgram.getUniformLocation("uSource");
        this.downTexelSizeLoc = downsampleProgram.getUniformLocation("uTexelSize");
        this.downOffsetLoc = downsampleProgram.getUniformLocation("uOffset");
        this.upSamplerLoc = upsampleProgram.getUniformLocation("uSource");
        this.upTexelSizeLoc = upsampleProgram.getUniformLocation("uTexelSize");
        this.upOffsetLoc = upsampleProgram.getUniformLocation("uOffset");
        this.smallHSamplerLoc = smallHProgram.getUniformLocation("uSource");
        this.smallHTexelSizeLoc = smallHProgram.getUniformLocation("uTexelSize");
        this.smallHRadiusLoc = smallHProgram.getUniformLocation("uRadius");
        this.smallVSamplerLoc = smallVProgram.getUniformLocation("uSource");
        this.smallVTexelSizeLoc = smallVProgram.getUniformLocation("uTexelSize");
        this.smallVRadiusLoc = smallVProgram.getUniformLocation("uRadius");

        for (int i = 0; i < pyramid.length; i++) {
            pyramid[i] = new LevelTarget();
        }

        this.quadVao = GL30.glGenVertexArrays();
        this.quadVbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(quadVao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, quadVbo);
        float[] vertices = new float[]{
                -1f, -1f, 0f, 0f,
                1f, -1f, 1f, 0f,
                -1f, 1f, 0f, 1f,
                1f, 1f, 1f, 1f
        };
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
        int stride = 4 * Float.BYTES;
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, stride, 0L);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 2L * Float.BYTES);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void destroy() {
        for (LevelTarget level : pyramid) {
            destroyLevel(level);
        }
        destroyLevel(fullResolutionTarget);
        destroyLevel(smallTempTarget);

        if (quadVao != 0) {
            GL30.glDeleteVertexArrays(quadVao);
            quadVao = 0;
        }
        if (quadVbo != 0) {
            GL15.glDeleteBuffers(quadVbo);
            quadVbo = 0;
        }

        downsampleProgram.delete();
        upsampleProgram.delete();
        smallHProgram.delete();
        smallVProgram.delete();
    }

    public int blurFromColorTexture(int sourceTexture, int width, int height, float radiusPx) {
        return blurFromColorTexture(sourceTexture, width, height, radiusPx, true);
    }

    public int blurFromColorTexture(int sourceTexture, int width, int height, float radiusPx, boolean preserveState) {
        if (sourceTexture == 0 || width <= 0 || height <= 0) {
            return 0;
        }

        float effectiveRadius = Math.max(radiusPx, MIN_RADIUS);

        boolean useSmallKernel = effectiveRadius <= SMALL_RADIUS_THRESHOLD;
        int passCount = 0;
        float[] offsets = null;

        if (useSmallKernel) {
            ensureLevel(fullResolutionTarget, width, height);
            ensureLevel(smallTempTarget, width, height);
        } else {
            passCount = determinePassCount(effectiveRadius, width, height);
            if (passCount <= 0) {
                return sourceTexture;
            }
            offsets = buildOffsets(passCount, effectiveRadius);
            ensureIntermediateTargets(width, height, passCount);
            ensureLevel(fullResolutionTarget, width, height);
        }

        GlState.Snapshot snapshot = preserveState ? GlState.push() : null;
        try (TextureUnitGuard unit0 = TextureUnitGuard.capture(0, GL11.GL_TEXTURE_2D)) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL30.GL_FRAMEBUFFER_SRGB);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL30.glBindVertexArray(quadVao);

            if (useSmallKernel) {
                runSmallRadiusBlur(sourceTexture, width, height, effectiveRadius);
            } else {
                runDownsampleBlur(sourceTexture, width, height, passCount, offsets);
            }

            return fullResolutionTarget.texture;
        } finally {
            GL30.glBindVertexArray(0);
            GL20.glUseProgram(0);
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            if (preserveState && snapshot != null) {
                GlState.pop(snapshot);
            }
        }
    }

    private void runSmallRadiusBlur(int sourceTexture, int width, int height, float effectiveRadius) {
        // Horizontal pass
        smallHProgram.use();
        if (smallHSamplerLoc >= 0) {
            GL20.glUniform1i(smallHSamplerLoc, 0);
        }
        if (smallHTexelSizeLoc >= 0) {
            GL20.glUniform2f(smallHTexelSizeLoc, 1.0f / Math.max(1, width), 1.0f / Math.max(1, height));
        }
        if (smallHRadiusLoc >= 0) {
            GL20.glUniform1f(smallHRadiusLoc, effectiveRadius);
        }
        bindTarget(smallTempTarget);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, sourceTexture);
        drawQuad();

        // Vertical pass
        smallVProgram.use();
        if (smallVSamplerLoc >= 0) {
            GL20.glUniform1i(smallVSamplerLoc, 0);
        }
        if (smallVTexelSizeLoc >= 0) {
            GL20.glUniform2f(smallVTexelSizeLoc, 1.0f / Math.max(1, width), 1.0f / Math.max(1, height));
        }
        if (smallVRadiusLoc >= 0) {
            GL20.glUniform1f(smallVRadiusLoc, effectiveRadius);
        }
        bindTarget(fullResolutionTarget);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, smallTempTarget.texture);
        drawQuad();
    }

    private void runDownsampleBlur(int sourceTexture, int width, int height, int passCount, float[] offsets) {
        if (offsets == null || offsets.length != passCount) {
            throw new IllegalArgumentException("offsets length must match passCount");
        }

        int currentTexture = sourceTexture;
        int currentWidth = width;
        int currentHeight = height;

        downsampleProgram.use();
        if (downSamplerLoc >= 0) {
            GL20.glUniform1i(downSamplerLoc, 0);
        }
        for (int i = 0; i < passCount; i++) {
            LevelTarget target = pyramid[i];
            bindTarget(target);
            if (downTexelSizeLoc >= 0) {
                GL20.glUniform2f(downTexelSizeLoc,
                        1.0f / Math.max(1, currentWidth),
                        1.0f / Math.max(1, currentHeight));
            }
            if (downOffsetLoc >= 0) {
                GL20.glUniform1f(downOffsetLoc, offsets[i]);
            }
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture);
            drawQuad();
            currentTexture = target.texture;
            currentWidth = target.width;
            currentHeight = target.height;
        }

        upsampleProgram.use();
        if (upSamplerLoc >= 0) {
            GL20.glUniform1i(upSamplerLoc, 0);
        }
        for (int i = passCount - 2; i >= 0; i--) {
            LevelTarget target = pyramid[i];
            bindTarget(target);
            if (upTexelSizeLoc >= 0) {
                GL20.glUniform2f(upTexelSizeLoc,
                        1.0f / Math.max(1, currentWidth),
                        1.0f / Math.max(1, currentHeight));
            }
            if (upOffsetLoc >= 0) {
                GL20.glUniform1f(upOffsetLoc, offsets[i]);
            }
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture);
            drawQuad();
            currentTexture = target.texture;
            currentWidth = target.width;
            currentHeight = target.height;
        }

        bindTarget(fullResolutionTarget);
        if (upTexelSizeLoc >= 0) {
            GL20.glUniform2f(upTexelSizeLoc,
                    1.0f / Math.max(1, currentWidth),
                    1.0f / Math.max(1, currentHeight));
        }
        if (upOffsetLoc >= 0) {
            GL20.glUniform1f(upOffsetLoc, offsets.length > 0 ? offsets[0] : MIN_RADIUS);
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture);
        drawQuad();
    }

    private void drawQuad() {
        RenderFrameMetrics.getInstance().recordDrawCall(2);
        GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
    }

    private void bindTarget(LevelTarget target) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, target.fbo);
        GL11.glViewport(0, 0, target.width, target.height);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
    }

    private void ensureIntermediateTargets(int baseWidth, int baseHeight, int passCount) {
        for (int i = 0; i < passCount; i++) {
            int divisor = 1 << (i + 1);
            int w = Math.max(1, baseWidth / divisor);
            int h = Math.max(1, baseHeight / divisor);
            ensureLevel(pyramid[i], w, h);
        }
    }

    private void ensureLevel(LevelTarget target, int width, int height) {
        if (target.texture != 0 && (target.width != width || target.height != height)) {
            GL11.glDeleteTextures(target.texture);
            GL30.glDeleteFramebuffers(target.fbo);
            target.texture = 0;
            target.fbo = 0;
        }
        if (target.texture == 0) {
            target.texture = createRenderTexture(width, height);
            target.fbo = createFramebuffer(target.texture);
        }
        target.width = width;
        target.height = height;
    }

    private void destroyLevel(LevelTarget target) {
        if (target == null) {
            return;
        }
        if (target.texture != 0) {
            GL11.glDeleteTextures(target.texture);
            target.texture = 0;
        }
        if (target.fbo != 0) {
            GL30.glDeleteFramebuffers(target.fbo);
            target.fbo = 0;
        }
        target.width = 0;
        target.height = 0;
    }

    private int createRenderTexture(int width, int height) {
        int tex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, intermediateInternalFormat, width, height, 0,
                GL11.GL_RGBA, intermediatePixelType, (java.nio.ByteBuffer) null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return tex;
    }

    private int createFramebuffer(int texture) {
        int fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                GL11.GL_TEXTURE_2D, texture, 0);
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            GL30.glDeleteFramebuffers(fbo);
            GL11.glDeleteTextures(texture);
            throw new IllegalStateException("Blur framebuffer incomplete: status=" + status);
        }
        return fbo;
    }

    private int determinePassCount(float radiusPx, int width, int height) {
        int available = 0;
        int w = width;
        int h = height;
        while (available < MAX_LEVELS && (w > 1 || h > 1)) {
            w = Math.max(1, w / 2);
            h = Math.max(1, h / 2);
            available++;
            if (w == 1 && h == 1) {
                break;
            }
        }
        if (available == 0) {
            available = 1;
        }
        int desired = Math.max(1, (int) Math.ceil(radiusPx / 8f));
        // Improve quality for mid radii (10..16px): ensure at least 3 passes.
        if (radiusPx <= 16f) {
            desired = Math.max(desired, 3);
        }
        return Math.min(available, desired);
    }

    private float[] buildOffsets(int passCount, float radiusPx) {
        float[] offsets = new float[passCount];
        // Use a slightly denser kernel for smaller radii to avoid under-blur
        // Distribute radius non-linearly: more weight to earlier (higher-res) passes
        float sumWeights = 0f;
        float[] weights = new float[passCount];
        for (int i = 0; i < passCount; i++) {
            float t = (float) i / Math.max(1, passCount - 1);
            // Ease-out curve: early passes get larger share
            float w = 1.0f - t * 0.5f;
            weights[i] = w;
            sumWeights += w;
        }
        for (int i = 0; i < passCount; i++) {
            float share = weights[i] / sumWeights;
            float levelScale = 1f / (float) (1 << i);
            offsets[i] = Math.max(MIN_RADIUS, radiusPx * share * levelScale + 0.5f);
        }
        return offsets;
    }
}
