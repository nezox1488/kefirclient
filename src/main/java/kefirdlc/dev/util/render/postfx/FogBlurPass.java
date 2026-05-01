package kefirdlc.dev.util.render.postfx;
// coded by sitoku \\
// since 27.04.2026 \\

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import kefirdlc.dev.util.render.backends.gl.GlState;
import kefirdlc.dev.util.render.backends.gl.ShaderProgram;

import java.nio.FloatBuffer;

/**
 * Applies a depth-aware fog blur by combining a blurred color pyramid with the original color buffer.
 */
public final class FogBlurPass {
    public record TextureBinding(int textureId, int target) {
        public boolean isValid() {
            return textureId > 0 && target != 0;
        }

        public boolean isTexture2D() {
            return textureId > 0 && target == GL11.GL_TEXTURE_2D;
        }
    }

    private static final float LARGE_RADIUS_THRESHOLD = 12.0f;
    private static final float MEDIUM_RADIUS_THRESHOLD = 4.0f;
    private static final float LARGE_RADIUS_SCALE = 1.0f;
    private static final float MEDIUM_RADIUS_SCALE = 0.5f;
    private static final float SMALL_RADIUS_SCALE = 0.25f;

    private final DownsampleBlur blur = new DownsampleBlur(GL11.GL_RGBA8, GL11.GL_UNSIGNED_BYTE);
    private final RenderTarget compositeTarget = new RenderTarget();
    private final RenderTarget scaledColorTarget = new RenderTarget();
    private final FullScreenQuad quad = new FullScreenQuad();
    private final ShaderProgram program;
    private final ShaderProgram copyProgram;
    private final int uSourceLoc;
    private final int uBlurredLoc;
    private final int uDepthLoc;
    private final int uNearFarLoc;
    private final int uInverseProjectionLoc;
    private final int uInverseProjectionValidLoc;
    private final int uThresholdLoc;
    private final int uTintColorLoc;
    private final int uTintStrengthLoc;
    private final int uBlurredTexelSizeLoc;
    private final int uBlurResolutionScaleLoc;
    private final int uCopySourceLoc;
    private boolean destroyed;

    public FogBlurPass() {
        this.program = ShaderProgram.fromResources(
                "assets/hysteria/shaders/blur/blur_fullscreen.vert",
                "assets/hysteria/shaders/postfx/fog_blur.frag");
        this.copyProgram = ShaderProgram.fromResources(
                "assets/hysteria/shaders/blur/blur_fullscreen.vert",
                "assets/hysteria/shaders/postfx/fog_copy.frag");
        this.uSourceLoc = program.getUniformLocation("uSource");
        this.uBlurredLoc = program.getUniformLocation("uBlurred");
        this.uDepthLoc = program.getUniformLocation("uDepth");
        this.uNearFarLoc = program.getUniformLocation("uNearFar");
        this.uInverseProjectionLoc = program.getUniformLocation("uInverseProjection");
        this.uInverseProjectionValidLoc = program.getUniformLocation("uInverseProjectionValid");
        this.uThresholdLoc = program.getUniformLocation("uThreshold");
        this.uTintColorLoc = program.getUniformLocation("uTintColor");
        this.uTintStrengthLoc = program.getUniformLocation("uTintStrength");
        this.uBlurredTexelSizeLoc = program.getUniformLocation("uBlurredTexelSize");
        this.uBlurResolutionScaleLoc = program.getUniformLocation("uBlurResolutionScale");
        this.uCopySourceLoc = copyProgram.getUniformLocation("uSource");
    }

    public record Result(int colorTexture, int framebuffer, int width, int height) {}

    public Result render(TextureBinding colorBinding,
                         TextureBinding depthBinding,
                         int width,
                         int height,
                         float blurRadius,
                         Matrix4f inverseProjection,
                         boolean inverseProjectionValid,
                         float nearPlane,
                         float farPlane,
                         float minThreshold,
                         float maxThreshold,
                         float tintR,
                         float tintG,
                         float tintB,
                         float tintStrength) {
        if (colorBinding == null || colorBinding.textureId() <= 0 || width <= 0 || height <= 0) {
            return new Result(0, 0, 0, 0);
        }
        if (destroyed) {
            return new Result(colorBinding.textureId(), 0, width, height);
        }
        if (!colorBinding.isTexture2D()) {
            return new Result(0, 0, 0, 0);
        }
        if (depthBinding == null || !depthBinding.isTexture2D()) {
            return new Result(colorBinding.textureId(), 0, width, height);
        }

        int colorTexture = colorBinding.textureId();
        int depthTexture = depthBinding.textureId();


        float sanitizedNear = Math.max(1e-4f, nearPlane);
        float sanitizedFar = Math.max(sanitizedNear + 1e-3f, farPlane);
        float thresholdMin = Math.min(minThreshold, maxThreshold);
        float thresholdMax = Math.max(minThreshold, maxThreshold);
        float sanitizedTintStrength = Math.max(0f, Math.min(1f, tintStrength));
        float sanitizedTintR = Math.max(0f, Math.min(1f, tintR));
        float sanitizedTintG = Math.max(0f, Math.min(1f, tintG));
        float sanitizedTintB = Math.max(0f, Math.min(1f, tintB));
        boolean sanitizedInverseProjectionValid = inverseProjectionValid && inverseProjection != null;
        Matrix4f projectionMatrix = sanitizedInverseProjectionValid ? new Matrix4f(inverseProjection) : new Matrix4f();

        float sanitizedBlurRadius = Math.max(0f, blurRadius);
        float resolutionScale = chooseResolutionScale(sanitizedBlurRadius);
        int downscaledWidth = Math.max(1, Math.round(width * resolutionScale));
        int downscaledHeight = Math.max(1, Math.round(height * resolutionScale));

        quad.ensure();

        GlState.Snapshot snapshot = GlState.push();
        try {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL30.GL_FRAMEBUFFER_SRGB);

            int blurSourceTexture = colorTexture;
            int blurSourceWidth = width;
            int blurSourceHeight = height;

            boolean downscalePass = resolutionScale < LARGE_RADIUS_SCALE
                    && (downscaledWidth != width || downscaledHeight != height);
            float blurResolutionScale = downscalePass ? resolutionScale : LARGE_RADIUS_SCALE;
            if (downscalePass) {
                scaledColorTarget.ensure(downscaledWidth, downscaledHeight);

                // Copy the scene color into the downscaled target
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, scaledColorTarget.fbo);
                GL11.glViewport(0, 0, downscaledWidth, downscaledHeight);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);

                copyProgram.use();
                if (uCopySourceLoc >= 0) {
                    GL20.glUniform1i(uCopySourceLoc, 0);
                }

                try (TextureUnitGuard unit0 = TextureUnitGuard.capture(0, GL11.GL_TEXTURE_2D, colorBinding.target())) {
                    GL13.glActiveTexture(GL13.GL_TEXTURE0);
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture);
                    quad.bindAndDraw();
                }

                GL20.glUseProgram(0);
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

                blurSourceTexture = scaledColorTarget.colorTex;
                blurSourceWidth = downscaledWidth;
                blurSourceHeight = downscaledHeight;
            }

            // Blur the source color buffer without creating an additional snapshot
            float scaledBlurRadius = sanitizedBlurRadius * blurResolutionScale;
            int blurredTexture = blur.blurFromColorTexture(
                    blurSourceTexture,
                    blurSourceWidth,
                    blurSourceHeight,
                    scaledBlurRadius,
                    false);
            if (blurredTexture == 0) {
                return new Result(colorTexture, 0, width, height);
            }

            compositeTarget.ensure(width, height);

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL30.GL_FRAMEBUFFER_SRGB);

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, compositeTarget.fbo);
            GL11.glViewport(0, 0, width, height);
            GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);

            program.use();
            if (uSourceLoc >= 0) {
                GL20.glUniform1i(uSourceLoc, 0);
            }
            if (uBlurredLoc >= 0) {
                GL20.glUniform1i(uBlurredLoc, 1);
            }
            if (uDepthLoc >= 0) {
                GL20.glUniform1i(uDepthLoc, 2);
            }
            if (uNearFarLoc >= 0) {
                GL20.glUniform2f(uNearFarLoc, sanitizedNear, sanitizedFar);
            }
            if (uInverseProjectionValidLoc >= 0) {
                GL20.glUniform1i(uInverseProjectionValidLoc, sanitizedInverseProjectionValid ? 1 : 0);
            }
            if (uInverseProjectionLoc >= 0 && sanitizedInverseProjectionValid) {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    FloatBuffer buffer = stack.mallocFloat(16);
                    projectionMatrix.get(buffer);
                    GL20.glUniformMatrix4fv(uInverseProjectionLoc, false, buffer);
                }
            }
            if (uThresholdLoc >= 0) {
                GL20.glUniform2f(uThresholdLoc, thresholdMin, thresholdMax);
            }
            if (uTintColorLoc >= 0) {
                GL20.glUniform3f(uTintColorLoc, sanitizedTintR, sanitizedTintG, sanitizedTintB);
            }
            if (uTintStrengthLoc >= 0) {
                GL20.glUniform1f(uTintStrengthLoc, sanitizedTintStrength);
            }
            if (uBlurredTexelSizeLoc >= 0) {
                GL20.glUniform2f(uBlurredTexelSizeLoc,
                        1.0f / Math.max(1, blurSourceWidth),
                        1.0f / Math.max(1, blurSourceHeight));
            }
            if (uBlurResolutionScaleLoc >= 0) {
                GL20.glUniform1f(uBlurResolutionScaleLoc, blurResolutionScale);
            }

            try (TextureUnitGuard unit0 = TextureUnitGuard.capture(0, GL11.GL_TEXTURE_2D, colorBinding.target());
                 TextureUnitGuard unit1 = TextureUnitGuard.capture(1, GL11.GL_TEXTURE_2D);
                 TextureUnitGuard unit2 = TextureUnitGuard.capture(2, GL11.GL_TEXTURE_2D, depthBinding.target())) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                GL11.glBindTexture(colorBinding.target(), colorTexture);
                GL13.glActiveTexture(GL13.GL_TEXTURE1);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, blurredTexture);
                GL13.glActiveTexture(GL13.GL_TEXTURE2);
                GL11.glBindTexture(depthBinding.target(), depthTexture);

                quad.bindAndDraw();
            }

            return new Result(compositeTarget.colorTex, compositeTarget.fbo, width, height);
        } finally {
            GL20.glUseProgram(0);
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GlState.pop(snapshot);
        }
    }

    public void destroy() {
        if (destroyed) {
            return;
        }
        destroyed = true;
        blur.destroy();
        compositeTarget.destroy();
        scaledColorTarget.destroy();
        quad.destroy();
        program.delete();
        copyProgram.delete();
    }

    /**
     * Chooses the render resolution scale for the blur pre-pass based on the requested radius.
     */
    private static float chooseResolutionScale(float blurRadius) {
        float sanitized = Math.max(0f, blurRadius);
        if (sanitized >= LARGE_RADIUS_THRESHOLD) {
            return LARGE_RADIUS_SCALE;
        }
        if (sanitized >= MEDIUM_RADIUS_THRESHOLD) {
            return MEDIUM_RADIUS_SCALE;
        }
        return SMALL_RADIUS_SCALE;
    }
}
