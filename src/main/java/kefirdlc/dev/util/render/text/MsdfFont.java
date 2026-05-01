package kefirdlc.dev.util.render.text;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import kefirdlc.dev.util.render.backends.gl.GlBackend;
import kefirdlc.dev.util.render.backends.gl.ResourceUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

final class MsdfFont {
    static final class Glyph {
        final float advance;
        final boolean renderable;
        final float planeLeft;
        final float planeBottom;
        final float planeRight;
        final float planeTop;
        final float u0;
        final float v0;
        final float u1;
        final float v1;

        Glyph(float advance) {
            this.advance = advance;
            this.renderable = false;
            this.planeLeft = 0f;
            this.planeBottom = 0f;
            this.planeRight = 0f;
            this.planeTop = 0f;
            this.u0 = 0f;
            this.v0 = 0f;
            this.u1 = 0f;
            this.v1 = 0f;
        }

        Glyph(float advance,
              float planeLeft,
              float planeBottom,
              float planeRight,
              float planeTop,
              float atlasLeft,
              float atlasBottom,
              float atlasRight,
              float atlasTop,
              int atlasWidth,
              int atlasHeight) {
            this.advance = advance;
            this.renderable = true;
            this.planeLeft = planeLeft;
            this.planeBottom = planeBottom;
            this.planeRight = planeRight;
            this.planeTop = planeTop;
            this.u0 = atlasLeft / (float) atlasWidth;
            this.u1 = atlasRight / (float) atlasWidth;

            float vBottom = atlasBottom / (float) atlasHeight;
            float vTop = atlasTop / (float) atlasHeight;
            this.v0 = 1.0f - vBottom;
            this.v1 = 1.0f - vTop;
        }
    }

    private final Map<Integer, Glyph> glyphs;
    private final Map<Long, Float> kerning;
    private final int textureId;
    private final int textureWidth;
    private final int textureHeight;
    private final float distanceRange;
    private final float emSize;
    private final float lineHeight;
    private final float ascender;
    private final float descender;

    private MsdfFont(int textureId,
                     int textureWidth,
                     int textureHeight,
                     float distanceRange,
                     float emSize,
                     float lineHeight,
                     float ascender,
                     float descender,
                     Map<Integer, Glyph> glyphs,
                     Map<Long, Float> kerning) {
        this.textureId = textureId;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.distanceRange = distanceRange;
        this.emSize = emSize;
        this.lineHeight = lineHeight;
        this.ascender = ascender;
        this.descender = descender;
        this.glyphs = glyphs;
        this.kerning = kerning;
    }

    static MsdfFont load(GlBackend backend, String jsonResourcePath, String textureResourcePath) {
        Objects.requireNonNull(backend, "backend");
        Objects.requireNonNull(jsonResourcePath, "jsonResourcePath");
        Objects.requireNonNull(textureResourcePath, "textureResourcePath");

        String json = ResourceUtils.readText(jsonResourcePath);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        JsonObject atlas = root.getAsJsonObject("atlas");
        if (atlas == null) {
            throw new IllegalStateException("Missing 'atlas' section in MSDF font: " + jsonResourcePath);
        }
        int atlasWidth = atlas.get("width").getAsInt();
        int atlasHeight = atlas.get("height").getAsInt();
        float distanceRange = atlas.has("distanceRange") ? atlas.get("distanceRange").getAsFloat() : 6f;

        JsonObject metrics = root.getAsJsonObject("metrics");
        if (metrics == null) {
            throw new IllegalStateException("Missing 'metrics' section in MSDF font: " + jsonResourcePath);
        }
        float emSize = metrics.has("emSize") ? metrics.get("emSize").getAsFloat() : 1f;
        float lineHeight = metrics.has("lineHeight") ? metrics.get("lineHeight").getAsFloat() : emSize;
        float ascender = metrics.has("ascender") ? metrics.get("ascender").getAsFloat() : lineHeight;
        float descenderValue = metrics.has("descender") ? metrics.get("descender").getAsFloat() : 0f;
        float descender = Math.abs(descenderValue);

        Map<Integer, Glyph> glyphs = new HashMap<>();
        JsonArray glyphArray = root.getAsJsonArray("glyphs");
        if (glyphArray != null) {
            for (JsonElement element : glyphArray) {
                JsonObject glyphObj = element.getAsJsonObject();
                int codepoint = glyphObj.get("unicode").getAsInt();
                float advance = glyphObj.has("advance") ? glyphObj.get("advance").getAsFloat() : 0f;

                JsonObject planeBounds = glyphObj.has("planeBounds") ? glyphObj.getAsJsonObject("planeBounds") : null;
                JsonObject atlasBounds = glyphObj.has("atlasBounds") ? glyphObj.getAsJsonObject("atlasBounds") : null;

                Glyph glyph;
                if (planeBounds == null || atlasBounds == null) {
                    glyph = new Glyph(advance);
                } else {
                    float planeLeft = planeBounds.get("left").getAsFloat();
                    float planeBottom = planeBounds.get("bottom").getAsFloat();
                    float planeRight = planeBounds.get("right").getAsFloat();
                    float planeTop = planeBounds.get("top").getAsFloat();

                    float atlasLeft = atlasBounds.get("left").getAsFloat();
                    float atlasBottom = atlasBounds.get("bottom").getAsFloat();
                    float atlasRight = atlasBounds.get("right").getAsFloat();
                    float atlasTop = atlasBounds.get("top").getAsFloat();

                    glyph = new Glyph(advance, planeLeft, planeBottom, planeRight, planeTop,
                            atlasLeft, atlasBottom, atlasRight, atlasTop, atlasWidth, atlasHeight);
                }
                glyphs.put(codepoint, glyph);
            }
        }

        Map<Long, Float> kerning = new HashMap<>();
        JsonArray kerningArray = root.getAsJsonArray("kerning");
        if (kerningArray != null) {
            for (JsonElement element : kerningArray) {
                JsonObject kObj = element.getAsJsonObject();
                int left = kObj.get("unicode1").getAsInt();
                int right = kObj.get("unicode2").getAsInt();
                float advance = kObj.has("advance") ? kObj.get("advance").getAsFloat() : 0f;
                kerning.put(pairKey(left, right), advance);
            }
        }

        ByteBuffer imageBuffer = ResourceUtils.readBinary(textureResourcePath);
        int textureId;
        int textureWidth;
        int textureHeight;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);


            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, w, h, comp, 4);
            if (image == null) {
                throw new IllegalStateException("Failed to load MSDF atlas '" + textureResourcePath + "': " + STBImage.stbi_failure_reason());
            }
            textureWidth = w.get(0);
            textureHeight = h.get(0);
            textureId = backend.createMsdfTexture(textureWidth, textureHeight, image);
            STBImage.stbi_image_free(image);
        }

        return new MsdfFont(textureId, textureWidth, textureHeight, distanceRange, emSize,
                lineHeight, ascender, descender, glyphs, kerning);
    }

    int textureId() {
        return textureId;
    }

    int textureWidth() {
        return textureWidth;
    }

    int textureHeight() {
        return textureHeight;
    }

    float distanceRange() {
        return distanceRange;
    }

    float emSize() {
        return emSize;
    }

    float lineHeight() {
        return lineHeight;
    }

    float ascender() {
        return ascender;
    }

    float descender() {
        return descender;
    }

    Glyph glyph(int codepoint) {
        return glyphs.get(codepoint);
    }

    float kerning(int left, int right) {
        return kerning.getOrDefault(pairKey(left, right), 0f);
    }

    private static long pairKey(int left, int right) {
        return (((long) left) << 32) | (right & 0xFFFFFFFFL);
    }
}
