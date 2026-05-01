package kefirdlc.dev.util.render.core;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.util.render.backends.gl.GlBackend;
import kefirdlc.dev.util.render.text.FontObject;
import kefirdlc.dev.util.render.text.TextRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class Renderer2D {
    private static final float MIN_BLUR_STRENGTH = 0.5f;
    private static final float BLUR_STRENGTH_EPSILON = 0.05f;
    private final GlBackend backend;
    private final java.util.ArrayDeque<ClipState> clipStack = new java.util.ArrayDeque<>();
    private final java.util.ArrayDeque<Float> alphaStack = new java.util.ArrayDeque<>();
    private final TransformStack transformStack = new TransformStack();
    private java.util.Map<String, TextRenderer> idToTextRenderer = new java.util.HashMap<>();
    private final ShapeBatcher batcher;

    private boolean frameBegun = false;
    private int frameWidth = 0;
    private int frameHeight = 0;
    private double guiScale = 1.0;
    private boolean blurPrepared = false;
    private float blurPreparedStrength = 0f;
    private int blurPreparedWidth = 0;
    private int blurPreparedHeight = 0;
    private boolean regionBlurPrepared = false;
    private float regionBlurPreparedStrength = 0f;
    private int regionBlurCaptureX = 0;
    private int regionBlurCaptureY = 0;
    private int regionBlurCaptureWidth = 0;
    private int regionBlurCaptureHeight = 0;
    



    private static final int[] COLOR_CODES = new int[32];

    static {
        for (int i = 0; i < 32; ++i) {
            int base = (i >> 3 & 1) * 85;
            int r = (i >> 2 & 1) * 170 + base;
            int g = (i >> 1 & 1) * 170 + base;
            int b = (i & 1) * 170 + base;
            if (i == 6) r += 85;
            if (i >= 16) {
                r /= 4;
                g /= 4;
                b /= 4;
            }
            COLOR_CODES[i] = (r & 255) << 16 | (g & 255) << 8 | (b & 255);
        }
    }

    public Renderer2D(GlBackend backend) {
        this.backend = backend;
        this.batcher = new ShapeBatcher(backend);
        resetAlphaStack();
    }

    public void begin(int width, int height) {
        if (frameBegun) {
            throw new IllegalStateException("begin() called while a frame is already active");
        }
        frameBegun = true;
        frameWidth = width;
        frameHeight = height;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getWindow() != null) {
            guiScale = client.getWindow().getScaleFactor();
        } else {
            guiScale = 1.0;
        }

        blurPrepared = false;
        blurPreparedStrength = 0f;
        blurPreparedWidth = 0;
        blurPreparedHeight = 0;
        regionBlurPrepared = false;
        regionBlurPreparedStrength = 0f;
        regionBlurCaptureX = 0;
        regionBlurCaptureY = 0;
        regionBlurCaptureWidth = 0;
        regionBlurCaptureHeight = 0;
        

        
        RenderFrameMetrics.getInstance().beginFrame(width, height);
        backend.beginFrame(width, height);
        backend.setScissorEnabled(false);
        clipStack.clear();
        transformStack.clear();
        transformStack.pushScale((float) guiScale, (float) guiScale, 0f, 0f);
        resetAlphaStack();
    }

    public double getGuiScale() {
        return guiScale;
    }

    private void ensureFrame() {
        if (!frameBegun) {
            throw new IllegalStateException("begin() must be called before issuing draw commands");
        }
    }

    public void rect(float x, float y, float w, float h, int rgbaPremul) {
        ensureFrame();
        batcher.enqueueRect(x, y, w, h, 0f, 0f, 0f, 0f, modulateColor(rgbaPremul), transformStack.current());
    }

    public void rect(float x, float y, float w, float h, float rounding, int rgbaPremul) {
        rect(x, y, w, h, rounding, rounding, rounding, rounding, rgbaPremul);
    }

    public void rect(float x, float y, float w, float h,
                     float roundTopLeft, float roundTopRight,
                     float roundBottomRight, float roundBottomLeft,
                     int rgbaPremul) {
        ensureFrame();
        float[] radii = scratchRadii(roundTopLeft, roundTopRight, roundBottomRight, roundBottomLeft);
        normalizeCornerRadii(w, h, radii);
        batcher.enqueueRect(x, y, w, h,
                radii[0], radii[1], radii[2], radii[3],
                modulateColor(rgbaPremul), transformStack.current());
    }

    public void drawRgbaTexture(int texture, float x, float y, float w, float h) {
        drawRgbaTextureInternal(texture, x, y, w, h, 0xFFFFFFFF, true, false);
    }

    public void drawRgbaTexture(int texture, float x, float y, float w, float h, int tintRgba) {
        drawRgbaTextureInternal(texture, x, y, w, h, tintRgba, true, false);
    }

    public void drawRgbaTexture(int texture, float x, float y, float w, float h, int tintRgba, boolean flipVertically) {
        drawRgbaTextureInternal(texture, x, y, w, h, tintRgba, flipVertically, false);
    }

    public void drawPremultipliedRgbaTexture(int texture, float x, float y, float w, float h) {
        drawRgbaTextureInternal(texture, x, y, w, h, 0xFFFFFFFF, true, true);
    }

    public void drawPremultipliedRgbaTexture(int texture, float x, float y, float w, float h,
                                             int tintRgba, boolean flipVertically) {
        drawRgbaTextureInternal(texture, x, y, w, h, tintRgba, flipVertically, true);
    }

    private void drawRgbaTextureInternal(int texture, float x, float y, float w, float h,
                                         int tintRgba, boolean flipVertically,
                                         boolean preservePremultipliedColor) {
        ensureFrame();
        if (texture <= 0) {
            return;
        }
        float v0 = flipVertically ? 1f : 0f;
        float v1 = flipVertically ? 0f : 1f;
        backend.drawRgbaTexturedQuad(texture, x, y, w, h, 0f, v0, 1f, v1,
                modulateColor(tintRgba), transformStack.current(), preservePremultipliedColor);
    }

    public void end() {
        if (!frameBegun) {
            throw new IllegalStateException("end() called without a matching begin()");
        }
        batcher.flush();
        backend.endFrame();
        RenderFrameMetrics.getInstance().endFrame();
        frameBegun = false;
        frameWidth = 0;
        frameHeight = 0;
        guiScale = 1.0;
        blurPrepared = false;
        blurPreparedStrength = 0f;
        blurPreparedWidth = 0;
        blurPreparedHeight = 0;
        regionBlurPrepared = false;
        regionBlurPreparedStrength = 0f;
        regionBlurCaptureX = 0;
        regionBlurCaptureY = 0;
        regionBlurCaptureWidth = 0;
        regionBlurCaptureHeight = 0;
        
 
        
        resetAlphaStack();
    }

    public void flush() {
        ensureFrame();
        batcher.flush();
    }

    public void pushClipRect(int x, int y, int w, int h) {
        pushRoundedClipRect((float) x, (float) y, (float) w, (float) h, 0f, 0f, 0f, 0f);
    }

    public void pushRoundedClipRect(float x, float y, float w, float h,
                                    float roundTopLeft, float roundTopRight,
                                    float roundBottomRight, float roundBottomLeft) {
        ensureFrame();
        ClipState incoming = ClipState.fromRect(
                x,
                y,
                w,
                h,
                roundTopLeft,
                roundTopRight,
                roundBottomRight,
                roundBottomLeft,
                transformStack.current()
        );
        ClipState applied;
        if (clipStack.isEmpty()) {
            applied = incoming;
        } else {
            ClipState current = clipStack.peek();
            applied = ClipState.intersect(current, incoming);
        }
        clipStack.push(applied);
        applyClipState(applied);
    }

    public void popClipRect() {
        ensureFrame();
        if (clipStack.isEmpty()) return;
        clipStack.pop();
        if (clipStack.isEmpty()) {
            backend.setScissorEnabled(false);
        } else {
            applyClipState(clipStack.peek());
        }
    }

    private void applyClipState(ClipState state) {
        if (state == null) {
            backend.setScissorEnabled(false);
            return;
        }
        backend.setScissorEnabled(true);
        backend.setScissorRect(state.x(), state.y(), state.w(), state.h(),
                state.roundTopLeft(), state.roundTopRight(), state.roundBottomRight(), state.roundBottomLeft());
    }

    public void rectOutline(float x, float y, float w, float h, int rgbaPremul, float thickness) {
        ensureFrame();
        batcher.enqueueRectOutline(x, y, w, h,
                0f, 0f, 0f, 0f,
                modulateColor(rgbaPremul), Math.max(1f, thickness), transformStack.current());
    }

    public void rectOutline(float x, float y, float w, float h, float rounding, int rgbaPremul, float thickness) {
        rectOutline(x, y, w, h, rounding, rounding, rounding, rounding, rgbaPremul, thickness);
    }

    public void rectOutline(float x, float y, float w, float h,
                            float roundTopLeft, float roundTopRight,
                            float roundBottomRight, float roundBottomLeft,
                            int rgbaPremul, float thickness) {
        ensureFrame();
        float[] radii = scratchRadii(roundTopLeft, roundTopRight, roundBottomRight, roundBottomLeft);
        normalizeCornerRadii(w, h, radii);
        batcher.enqueueRectOutline(x, y, w, h,
                radii[0], radii[1], radii[2], radii[3],
                modulateColor(rgbaPremul), Math.max(1f, thickness), transformStack.current());
    }

    public void gradient(float x, float y, float w, float h, int c00, int c10, int c11, int c01) {
        ensureFrame();
        batcher.enqueueGradient(x, y, w, h, 0f, 0f, 0f, 0f,
                modulateColor(c00),
                modulateColor(c10),
                modulateColor(c11),
                modulateColor(c01),
                transformStack.current());
    }

    public void gradient(float x, float y, float w, float h, float rounding, int c00, int c10, int c11, int c01) {
        gradient(x, y, w, h, rounding, rounding, rounding, rounding, c00, c10, c11, c01);
    }

    public void gradient(float x, float y, float w, float h,
                         float roundTopLeft, float roundTopRight,
                         float roundBottomRight, float roundBottomLeft,
                         int c00, int c10, int c11, int c01) {
        ensureFrame();
        float[] radii = scratchRadii(roundTopLeft, roundTopRight, roundBottomRight, roundBottomLeft);
        normalizeCornerRadii(w, h, radii);
        batcher.enqueueGradient(x, y, w, h,
                radii[0], radii[1], radii[2], radii[3],
                modulateColor(c00),
                modulateColor(c10),
                modulateColor(c11),
                modulateColor(c01),
                transformStack.current());
    }

    public void circle(float cx, float cy, float radius, float startDeg, float pct, int rgbaPremul) {
        ensureFrame();
        batcher.enqueueCircle(cx, cy, radius, startDeg, pct, modulateColor(rgbaPremul), transformStack.current());
    }

    public void shadow(float x, float y, float w, float h, float rounding,
                       float blurStrength, float spread, int rgbaPremul) {
        shadow(x, y, w, h, rounding, rounding, rounding, rounding, blurStrength, spread, rgbaPremul);
    }

    public void shadow(float x, float y, float w, float h,
                       float roundTopLeft, float roundTopRight,
                       float roundBottomRight, float roundBottomLeft,
                       float blurStrength, float spread, int rgbaPremul) {
        ensureFrame();
        if (w <= 0f || h <= 0f) {
            return;
        }
        float safeBlur = Math.max(0f, blurStrength);
        float safeSpread = Math.max(0f, spread);
        if (safeBlur <= 0f && safeSpread <= 0f) {
            return;
        }
        float[] radii = scratchRadii(roundTopLeft, roundTopRight, roundBottomRight, roundBottomLeft);
        normalizeCornerRadii(w, h, radii);
        backend.drawDropShadowRect(x, y, w, h,
                radii[0], radii[1], radii[2], radii[3],
                safeBlur, safeSpread,
                modulateColor(rgbaPremul),
                transformStack.current());
    }

    public void blur(float x, float y, float w, float h, float rounding) {
        blur(x, y, w, h, rounding, 1.0f);
    }

    public void blur(float x, float y, float w, float h, float rounding, float alpha) {
        ensureFrame();
        if (!blurPrepared) {
            return;
        }
        float opacity = clamp01(alpha) * currentAlphaMultiplier();
        if (opacity <= 0.0001f) {
            return;
        }
        backend.drawPreparedBlurRounded(x, y, w, h, Math.max(0f, rounding), opacity, transformStack.current());
    }

    public void blurRegion(float x, float y, float w, float h, float rounding) {
        blurRegion(x, y, w, h, rounding, 1.0f);
    }

    public void blurRegion(float x, float y, float w, float h, float rounding, float alpha) {
        ensureFrame();
        if (!regionBlurPrepared) {
            return;
        }
        float opacity = clamp01(alpha) * currentAlphaMultiplier();
        if (opacity <= 0.0001f) {
            return;
        }
        backend.drawPreparedRegionBlurRounded(x, y, w, h, Math.max(0f, rounding), opacity,
                transformStack.current(), regionBlurCaptureX, regionBlurCaptureY,
                regionBlurCaptureWidth, regionBlurCaptureHeight);
    }

    public void prepareBlur(float strength) {
        ensureFrame();
        int width = frameWidth;
        int height = frameHeight;
        if (width <= 0 || height <= 0) {
            blurPrepared = false;
            blurPreparedWidth = 0;
            blurPreparedHeight = 0;
            return;
        }

        float radius = Math.max(MIN_BLUR_STRENGTH, strength);
        boolean alreadyPrepared = blurPrepared
                && blurPreparedWidth == width
                && blurPreparedHeight == height
                && Math.abs(blurPreparedStrength - radius) <= BLUR_STRENGTH_EPSILON;
        if (alreadyPrepared) {
            return;
        }

        backend.prepareScreenBlur(width, height, radius);
        blurPrepared = true;
        blurPreparedStrength = radius;
        blurPreparedWidth = width;
        blurPreparedHeight = height;
    }

    public void prepareBlurRegion(float x, float y, float w, float h, float strength) {
        ensureFrame();
        if (frameWidth <= 0 || frameHeight <= 0) {
            regionBlurPrepared = false;
            regionBlurCaptureWidth = 0;
            regionBlurCaptureHeight = 0;
            return;
        }
        if (w <= 0f || h <= 0f) {
            regionBlurPrepared = false;
            regionBlurCaptureWidth = 0;
            regionBlurCaptureHeight = 0;
            return;
        }

        float[] matrix = transformStack.current();
        Bounds bounds = computeTransformedBounds(matrix, x, y, w, h);

        int captureLeft = clampToViewportFloor(bounds.minX, frameWidth);
        int captureTop = clampToViewportFloor(bounds.minY, frameHeight);
        int captureRight = clampToViewportCeil(bounds.maxX, frameWidth);
        int captureBottom = clampToViewportCeil(bounds.maxY, frameHeight);

        int captureWidth = Math.max(0, captureRight - captureLeft);
        int captureHeight = Math.max(0, captureBottom - captureTop);
        if (captureWidth <= 0 || captureHeight <= 0) {
            regionBlurPrepared = false;
            regionBlurCaptureWidth = 0;
            regionBlurCaptureHeight = 0;
            return;
        }

        float radius = Math.max(MIN_BLUR_STRENGTH, strength);
        boolean alreadyPrepared = regionBlurPrepared
                && regionBlurCaptureX == captureLeft
                && regionBlurCaptureY == captureTop
                && regionBlurCaptureWidth == captureWidth
                && regionBlurCaptureHeight == captureHeight
                && Math.abs(regionBlurPreparedStrength - radius) <= BLUR_STRENGTH_EPSILON;
        if (alreadyPrepared) {
            return;
        }

        boolean success = backend.prepareRegionBlur(captureLeft, captureTop, captureWidth, captureHeight, radius);
        regionBlurPrepared = success;
        if (success) {
            regionBlurPreparedStrength = radius;
            regionBlurCaptureX = captureLeft;
            regionBlurCaptureY = captureTop;
            regionBlurCaptureWidth = captureWidth;
            regionBlurCaptureHeight = captureHeight;
        } else {
            regionBlurPreparedStrength = 0f;
            regionBlurCaptureWidth = 0;
            regionBlurCaptureHeight = 0;
        }
    }

    private static int clampToViewportFloor(float value, int viewportMax) {
        int floored = (int) Math.floor(value);
        if (floored < 0) {
            return 0;
        }
        if (floored > viewportMax) {
            return viewportMax;
        }
        return floored;
    }

    private static int clampToViewportCeil(float value, int viewportMax) {
        int ceiled = (int) Math.ceil(value);
        if (ceiled < 0) {
            return 0;
        }
        if (ceiled > viewportMax) {
            return viewportMax;
        }
        return ceiled;
    }

    private static Bounds computeTransformedBounds(float[] matrix, float x, float y, float w, float h) {
        float x0 = x;
        float y0 = y;
        float x1 = x + w;
        float y1 = y + h;

        float wx0y0x = transformX(matrix, x0, y0);
        float wx0y0y = transformY(matrix, x0, y0);
        float wx1y0x = transformX(matrix, x1, y0);
        float wx1y0y = transformY(matrix, x1, y0);
        float wx1y1x = transformX(matrix, x1, y1);
        float wx1y1y = transformY(matrix, x1, y1);
        float wx0y1x = transformX(matrix, x0, y1);
        float wx0y1y = transformY(matrix, x0, y1);

        float minX = Math.min(Math.min(wx0y0x, wx1y0x), Math.min(wx1y1x, wx0y1x));
        float maxX = Math.max(Math.max(wx0y0x, wx1y0x), Math.max(wx1y1x, wx0y1x));
        float minY = Math.min(Math.min(wx0y0y, wx1y0y), Math.min(wx1y1y, wx0y1y));
        float maxY = Math.max(Math.max(wx0y0y, wx1y0y), Math.max(wx1y1y, wx0y1y));

        return new Bounds(minX, minY, maxX, maxY);
    }

    private static float transformX(float[] matrix, float px, float py) {
        if (matrix == null || matrix.length < 6) {
            return px;
        }
        return matrix[0] * px + matrix[1] * py + matrix[2];
    }

    private static float transformY(float[] matrix, float px, float py) {
        if (matrix == null || matrix.length < 6) {
            return py;
        }
        return matrix[3] * px + matrix[4] * py + matrix[5];
    }

    private record Bounds(float minX, float minY, float maxX, float maxY) {
    }

    public void setTransform(float[] m3) {
        ensureFrame();
        transformStack.clear();
        transformStack.pushScale((float) guiScale, (float) guiScale, 0f, 0f);
        transformStack.replaceTop(m3);
    }

    public void pushRotation(float degrees) {
        ensureFrame();
        transformStack.pushRotation(degrees);
    }

    public void popRotation() {
        ensureFrame();
        transformStack.pop();
    }

    public void pushTranslation(float tx, float ty) {
        ensureFrame();
        transformStack.pushTranslation(tx, ty);
    }

    public void popTransform() {
        ensureFrame();
        transformStack.pop();
    }

    public void pushScale(float scale) {
        pushScale(scale, scale);
    }

    public void pushScale(float sx, float sy) {
        ensureFrame();
        transformStack.pushScale(sx, sy, 0f, 0f);
    }

    public void pushScaleCentered(float scale) {
        pushScaleCentered(scale, scale);
    }

    public void pushScaleCentered(float sx, float sy) {
        ensureFrame();
        if (frameWidth <= 0 || frameHeight <= 0) {
            throw new IllegalStateException("Cannot compute frame center before begin(width, height) is called with positive dimensions");
        }
        transformStack.pushScale(sx, sy, frameWidth * 0.5f, frameHeight * 0.5f);
    }

    public void pushScale(float scale, float originX, float originY) {
        pushScale(scale, scale, originX, originY);
    }

    public void pushScale(float sx, float sy, float originX, float originY) {
        ensureFrame();
        transformStack.pushScale(sx, sy, originX, originY);
    }

    public void popScale() {
        ensureFrame();
        transformStack.pop();
    }

    public void pushAlpha(float alpha) {
        ensureFrame();
        float parent = currentAlphaMultiplier();
        float clamped = clamp01(alpha);
        alphaStack.push(parent * clamped);
    }

    public void popAlpha() {
        ensureFrame();
        if (alphaStack.size() > 1) {
            alphaStack.pop();
        }
    }

    public void registerTextRenderer(String fontId, TextRenderer tr) {
        if (tr != null) idToTextRenderer.put(fontId, tr);
    }

    public void registerTextRenderer(FontObject fo, TextRenderer tr) {
        if (tr != null) idToTextRenderer.put(fo.id, tr);
    }

    public TransformStack getTransformStack() {
        return transformStack;
    }


    public void text(FontObject fo, float x, float y, float size, String s, int rgbaPremul) {
        ensureFrame();
        if (fo == null) {
            throw new IllegalArgumentException("FontObject must not be null");
        }
        if (s == null || s.isEmpty() || size <= 0f) {
            return;
        }
        TextRenderer tr = idToTextRenderer.get(fo.id);
        if (tr == null) return;

        float currentX = x;
        int currentColor = rgbaPremul;


        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);


            if (c == '§' && i + 1 < s.length()) {

                if (!sb.isEmpty()) {
                    String part = sb.toString();
                    tr.drawText(currentX, y, size, part, modulateColor(currentColor), transformStack.current());

                    TextRenderer.TextMetrics m = tr.measureText(part, size);
                    currentX += (m != null ? m.width() : 0f);
                    sb.setLength(0);
                }


                int codeIndex = "0123456789abcdef".indexOf(Character.toLowerCase(s.charAt(i + 1)));
                if (codeIndex != -1) {

                    int rgb = COLOR_CODES[codeIndex];
                    int alpha = (rgbaPremul >> 24) & 0xFF;
                    currentColor = (alpha << 24) | rgb;
                } else if (Character.toLowerCase(s.charAt(i + 1)) == 'r') {

                    currentColor = rgbaPremul;
                }

                i++;
            } else {
                sb.append(c);
            }
        }


        if (!sb.isEmpty()) {
            tr.drawText(currentX, y, size, sb.toString(), modulateColor(currentColor), transformStack.current());
        }
    }

    public void text(FontObject fo, float x, float y, float size, Text mcText, int rgbaPremul, String alignKey) {
        ensureFrame();
        if (fo == null || mcText == null || size <= 0f) return;

        TextRenderer tr = idToTextRenderer.get(fo.id);
        if (tr == null) return;

        StringBuilder sb = new StringBuilder();
        tr.findStyle(sb, mcText);
        String styled = sb.toString();


        text(fo, x, y, size, styled, rgbaPremul, alignKey);
    }

    public void text(FontObject fo, float x, float y, float size, String s, int rgbaPremul, String alignKey) {

        if ("c".equals(alignKey) || "center".equals(alignKey)) {
            float w = getStringWidth(fo, s, size);
            text(fo, x - w / 2f, y, size, s, rgbaPremul);
        } else if ("r".equals(alignKey) || "right".equals(alignKey)) {
            float w = getStringWidth(fo, s, size);
            text(fo, x - w, y, size, s, rgbaPremul);
        } else {
            text(fo, x, y, size, s, rgbaPremul);
        }
    }

    public TextRenderer.TextMetrics measureText(FontObject fo, String text, float size) {
        if (fo == null) throw new IllegalArgumentException("FontObject must not be null");
        if (size <= 0f) return new TextRenderer.TextMetrics(0f, 0f);
        TextRenderer tr = idToTextRenderer.get(fo.id);
        if (tr == null) return new TextRenderer.TextMetrics(0f, 0f);
        return tr.measureText(text == null ? "" : text, size);
    }

    public TextRenderer.TextMetrics measureText(FontObject fo, Text mcText, float size) {
        if (fo == null) throw new IllegalArgumentException("FontObject must not be null");
        if (mcText == null || size <= 0f) return new TextRenderer.TextMetrics(0f, 0f);

        TextRenderer tr = idToTextRenderer.get(fo.id);
        if (tr == null) return new TextRenderer.TextMetrics(0f, 0f);

        StringBuilder sb = new StringBuilder();
        tr.findStyle(sb, mcText);

        return tr.measureText(sb.toString(), size);
    }
    public float getStringWidth(FontObject fo, Text mcText, float size) {
        TextRenderer.TextMetrics m = measureText(fo, mcText, size);
        return m == null ? 0f : m.width();
    }

    public float getStringWidth(FontObject fo, String text, float size) {
        if (text == null || text.isEmpty()) return 0f;

        String stripped = text.replaceAll("§[0-9a-fk-or]", "");

        TextRenderer.TextMetrics metrics = measureText(fo, stripped, size);
        return metrics == null ? 0f : metrics.width();
    }

    private void resetAlphaStack() {
        alphaStack.clear();
        alphaStack.push(1f);
    }

    private float currentAlphaMultiplier() {
        return alphaStack.isEmpty() ? 1f : alphaStack.peek();
    }

    private int modulateColor(int rgbaPremul) {
        float factor = currentAlphaMultiplier();
        if (factor >= 0.999f) {
            return rgbaPremul;
        }
        int a = (rgbaPremul >>> 24) & 0xFF;
        int r = (rgbaPremul >>> 16) & 0xFF;
        int g = (rgbaPremul >>> 8) & 0xFF;
        int b = rgbaPremul & 0xFF;
        int na = scaleChannel(a, factor);
        int nr = scaleChannel(r, factor);
        int ng = scaleChannel(g, factor);
        int nb = scaleChannel(b, factor);
        return (na << 24) | (nr << 16) | (ng << 8) | nb;
    }

    private static int scaleChannel(int value, float factor) {
        float scaled = value * factor;
        if (scaled <= 0f) {
            return 0;
        }
        if (scaled >= 255f) {
            return 255;
        }
        return Math.round(scaled);
    }

    private static float clamp01(float value) {
        if (value < 0f) {
            return 0f;
        }
        if (value > 1f) {
            return 1f;
        }
        return value;
    }

    private static float[] scratchRadii(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        return new float[]{topLeft, topRight, bottomRight, bottomLeft};
    }

    private static void normalizeCornerRadii(float w, float h, float[] radii) {
        if (radii == null || radii.length < 4) {
            throw new IllegalArgumentException("radii");
        }

        for (int i = 0; i < 4; i++) {
            float value = radii[i];
            if (!Float.isFinite(value)) {
                value = 0f;
            }
            radii[i] = Math.max(0f, value);
        }

        float absW = Math.abs(w);
        float absH = Math.abs(h);
        if (absW <= 0f || absH <= 0f) {
            java.util.Arrays.fill(radii, 0f);
            return;
        }

        enforceRadiusLimit(radii, 0, 1, absW);
        enforceRadiusLimit(radii, 3, 2, absW);
        enforceRadiusLimit(radii, 0, 3, absH);
        enforceRadiusLimit(radii, 1, 2, absH);
    }

    private static void enforceRadiusLimit(float[] radii, int a, int b, float limit) {
        float sum = radii[a] + radii[b];
        if (sum > limit && limit > 0f) {
            float scale = limit / sum;
            radii[a] *= scale;
            radii[b] *= scale;
        }
    }

    private static boolean nearlyEqual(float a, float b) {
        return Math.abs(a - b) <= 1.0e-4f;
    }

    private static boolean nearlyZero(float value) {
        return Math.abs(value) <= 1.0e-4f;
    }

    private static boolean isIdentityTransform(float[] matrix) {
        if (matrix == null || matrix.length < 9) {
            return true;
        }
        return nearlyEqual(matrix[0], 1f)
                && nearlyZero(matrix[1])
                && nearlyZero(matrix[2])
                && nearlyZero(matrix[3])
                && nearlyEqual(matrix[4], 1f)
                && nearlyZero(matrix[5])
                && nearlyZero(matrix[6])
                && nearlyZero(matrix[7])
                && nearlyEqual(matrix[8], 1f);
    }

    private static boolean isAxisAlignedTransform(float[] matrix) {
        if (matrix == null || matrix.length < 9) {
            return true;
        }
        return nearlyZero(matrix[1]) && nearlyZero(matrix[3]) && nearlyZero(matrix[6]) && nearlyZero(matrix[7])
                && nearlyEqual(matrix[8], 1f);
    }

    private static float transformPointX(float[] matrix, float x, float y) {
        if (matrix == null || matrix.length < 9) {
            return x;
        }
        return matrix[0] * x + matrix[1] * y + matrix[2];
    }

    private static float transformPointY(float[] matrix, float x, float y) {
        if (matrix == null || matrix.length < 9) {
            return y;
        }
        return matrix[3] * x + matrix[4] * y + matrix[5];
    }

    private static float computeRadiusScale(float[] matrix) {
        if (matrix == null || matrix.length < 9) {
            return 1f;
        }
        float scaleX = Math.abs(matrix[0]);
        float scaleY = Math.abs(matrix[4]);
        float minScale = Math.min(scaleX, scaleY);
        if (minScale <= 1.0e-4f) {
            return 0f;
        }
        return minScale;
    }

    private record ClipState(int x, int y, int w, int h,
                             float roundTopLeft, float roundTopRight,
                             float roundBottomRight, float roundBottomLeft) {

        private static ClipState fromRect(float x, float y, float w, float h,
                                          float roundTopLeft, float roundTopRight,
                                          float roundBottomRight, float roundBottomLeft) {
            return fromRect(x, y, w, h, roundTopLeft, roundTopRight, roundBottomRight, roundBottomLeft, null);
        }

        private static ClipState fromRect(float x, float y, float w, float h,
                                          float roundTopLeft, float roundTopRight,
                                          float roundBottomRight, float roundBottomLeft,
                                          float[] transform) {
            if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(w) || !Float.isFinite(h)) {
                return new ClipState(0, 0, 0, 0, 0f, 0f, 0f, 0f);
            }
            boolean hasTransform = transform != null && transform.length >= 9 && !isIdentityTransform(transform);
            float[] radii = scratchRadii(roundTopLeft, roundTopRight, roundBottomRight, roundBottomLeft);
            normalizeCornerRadii(Math.abs(w), Math.abs(h), radii);
            if (!hasTransform) {
                float left = (float) Math.floor(Math.min(x, x + w));
                float top = (float) Math.floor(Math.min(y, y + h));
                float right = (float) Math.ceil(Math.max(x, x + w));
                float bottom = (float) Math.ceil(Math.max(y, y + h));
                int ix = (int) left;
                int iy = (int) top;
                int iw = Math.max(0, (int) (right - left));
                int ih = Math.max(0, (int) (bottom - top));
                if (iw <= 0 || ih <= 0) {
                    return new ClipState(ix, iy, 0, 0, 0f, 0f, 0f, 0f);
                }
                return new ClipState(ix, iy, iw, ih, radii[0], radii[1], radii[2], radii[3]);
            }

            float x2 = x + w;
            float y2 = y + h;
            float[] xs = new float[]{x, x2, x, x2};
            float[] ys = new float[]{y, y, y2, y2};
            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < 4; i++) {
                float tx = transformPointX(transform, xs[i], ys[i]);
                float ty = transformPointY(transform, xs[i], ys[i]);
                if (!Float.isFinite(tx) || !Float.isFinite(ty)) {
                    return new ClipState(0, 0, 0, 0, 0f, 0f, 0f, 0f);
                }
                if (tx < minX) {
                    minX = tx;
                }
                if (tx > maxX) {
                    maxX = tx;
                }
                if (ty < minY) {
                    minY = ty;
                }
                if (ty > maxY) {
                    maxY = ty;
                }
            }

            float left = (float) Math.floor(Math.min(minX, maxX));
            float top = (float) Math.floor(Math.min(minY, maxY));
            float right = (float) Math.ceil(Math.max(minX, maxX));
            float bottom = (float) Math.ceil(Math.max(minY, maxY));
            int ix = (int) left;
            int iy = (int) top;
            int iw = Math.max(0, (int) (right - left));
            int ih = Math.max(0, (int) (bottom - top));
            if (iw <= 0 || ih <= 0) {
                return new ClipState(ix, iy, 0, 0, 0f, 0f, 0f, 0f);
            }

            if (isAxisAlignedTransform(transform)) {
                float radiusScale = computeRadiusScale(transform);
                if (radiusScale > 0f) {
                    for (int i = 0; i < radii.length; i++) {
                        radii[i] *= radiusScale;
                    }
                } else {
                    java.util.Arrays.fill(radii, 0f);
                }
            } else {
                java.util.Arrays.fill(radii, 0f);
            }
            normalizeCornerRadii(Math.abs(right - left), Math.abs(bottom - top), radii);
            return new ClipState(ix, iy, iw, ih, radii[0], radii[1], radii[2], radii[3]);
        }

        private static ClipState intersect(ClipState a, ClipState b) {
            if (a == null) {
                return b;
            }
            if (b == null) {
                return a;
            }

            int nx = Math.max(a.x(), b.x());
            int ny = Math.max(a.y(), b.y());
            int nr = Math.min(a.x() + a.w(), b.x() + b.w());
            int nb = Math.min(a.y() + a.h(), b.y() + b.h());
            int nw = Math.max(0, nr - nx);
            int nh = Math.max(0, nb - ny);
            if (nw <= 0 || nh <= 0) {
                return new ClipState(nx, ny, 0, 0, 0f, 0f, 0f, 0f);
            }
            if (matchesRect(nx, ny, nw, nh, b)) {

                return new ClipState(nx, ny, nw, nh,
                        b.roundTopLeft(), b.roundTopRight(), b.roundBottomRight(), b.roundBottomLeft());
            }
            if (matchesRect(nx, ny, nw, nh, a)) {

                return new ClipState(nx, ny, nw, nh,
                        a.roundTopLeft(), a.roundTopRight(), a.roundBottomRight(), a.roundBottomLeft());
            }
            return new ClipState(nx, ny, nw, nh, 0f, 0f, 0f, 0f);
        }

        private static boolean matchesRect(int x, int y, int w, int h, ClipState other) {

            return other != null && other.x() == x && other.y() == y && other.w() == w && other.h() == h;
        }
    }

    /**
     * Рендерит текст с градиентом между двумя цветами
     */
    public void gradientText(FontObject fo, float x, float y, float size, String text, int colorStart, int colorEnd) {
        gradientText(fo, x, y, size, text, colorStart, colorEnd, "l");
    }

    /**
     * Рендерит текст с градиентом между двумя цветами с выравниванием
     */
    public void gradientText(FontObject fo, float x, float y, float size, String text, int colorStart, int colorEnd, String alignKey) {
        ensureFrame();
        if (fo == null) {
            throw new IllegalArgumentException("FontObject must not be null");
        }
        if (text == null || text.isEmpty() || size <= 0f) {
            return;
        }
        
        TextRenderer tr = idToTextRenderer.get(fo.id);
        if (tr == null) return;


        float renderX = x;
        if ("c".equals(alignKey) || "center".equals(alignKey)) {
            float w = getStringWidth(fo, text, size);
            renderX = x - w / 2f;
        } else if ("r".equals(alignKey) || "right".equals(alignKey)) {
            float w = getStringWidth(fo, text, size);
            renderX = x - w;
        }


        tr.drawGradientText(renderX, y, size, text, modulateColor(colorStart), modulateColor(colorEnd));
    }
    

    


}