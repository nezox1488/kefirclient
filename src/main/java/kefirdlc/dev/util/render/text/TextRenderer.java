package kefirdlc.dev.util.render.text;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.event.impl.render.TextFactoryEvent;
import kefirdlc.dev.util.render.backends.gl.GlBackend;
import kefirdlc.dev.util.render.utils.ColorUtils;
import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public final class TextRenderer {
    private static final float[] IDENTITY_TRANSFORM = new float[]{
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f
    };

    private final GlBackend backend;
    private final MsdfFont font;
    

    private static final Char2IntArrayMap COLOR_CODES = new Char2IntArrayMap() {{
        put('0', 0xFF000000);
        put('1', 0xFF0000AA);
        put('2', 0xFF00AA00);
        put('3', 0xFF00AAAA);
        put('4', 0xFFAA0000);
        put('5', 0xFFAA00AA);
        put('6', 0xFFFFAA00);
        put('7', 0xFFAAAAAA);
        put('8', 0xFF555555);
        put('9', 0xFF5555FF);
        put('a', 0xFF55FF55);
        put('b', 0xFF55FFFF);
        put('c', 0xFFFF5555);
        put('d', 0xFFFF55FF);
        put('e', 0xFFFFFF55);
        put('f', 0xFFFFFFFF);
    }};

    public TextRenderer(GlBackend backend, MsdfFont font) {
        this.backend = Objects.requireNonNull(backend, "backend");
        this.font = Objects.requireNonNull(font, "font");
    }

    public void drawText(float x, float y, float size, String text, int rgbaPremul) {
        // Process text through TextFactoryEvent for NameProtect and other modules
        String processedText = processTextThroughEvent(text);
        drawText(x, y, size, processedText, rgbaPremul, "l", IDENTITY_TRANSFORM);
    }

    public void drawText(float x, float y, float size, String text, int rgbaPremul, float[] transform) {
        // Process text through TextFactoryEvent for NameProtect and other modules
        String processedText = processTextThroughEvent(text);
        drawText(x, y, size, processedText, rgbaPremul, "l", transform);
    }

    public void drawText(float x, float y, float size, String text, int rgbaPremul, String alignKey) {
        // Process text through TextFactoryEvent for NameProtect and other modules
        String processedText = processTextThroughEvent(text);
        drawText(x, y, size, processedText, rgbaPremul, alignKey, IDENTITY_TRANSFORM);
    }
    public void drawText(Text text, double x, double y, float size) {
        StringBuilder sb = new StringBuilder();
        findStyle(sb, text);

        // Process text through TextFactoryEvent for NameProtect and other modules
        String processedText = processTextThroughEvent(sb.toString());
        drawText((float) x, (float) y, size, processedText, ColorUtils.getText());
    }
    public void drawText(float x, float y, float size, String text, int rgbaPremul, String alignKey, float[] transform) {
        if (size <= 0f) {
            return;
        }
        String content = text == null ? "" : text;
        if (content.isEmpty()) {
            return;
        }

        float[] matrix = (transform != null && transform.length >= 6) ? transform : IDENTITY_TRANSFORM;
        float scale = size / Math.max(1e-6f, font.emSize());
        float lineHeight = font.lineHeight() * scale;
        float baselineY = y;
        String align = alignKey == null ? "l" : alignKey.toLowerCase();
        int defaultColor = rgbaPremul;
        int texture = font.textureId();
        float pxRange = font.distanceRange();

        String[] lines = content.split("\\n", -1);
        for (String line : lines) {
            float width = measureLineWidth(line, scale);
            float startX = x;
            if ("c".equals(align) || "center".equals(align)) {
                startX = x - width * 0.5f;
            } else if ("r".equals(align) || "right".equals(align)) {
                startX = x - width;
            }
            drawTextLineWithColors(startX, baselineY, scale, line, defaultColor, matrix, texture, pxRange);
            baselineY += lineHeight;
        }
    }

    private void drawTextLine(float x,
                              float baseline,
                              float scale,
                              String line,
                              int color,
                              float[] matrix,
                              int texture,
                              float pxRange) {
        if (line.isEmpty()) {
            return;
        }
        float penX = x;
        float baselineY = baseline;
        int prevCodepoint = -1;

        for (int i = 0; i < line.length();) {
            char ch = line.charAt(i);

            if (ch == '\\' && i + 9 < line.length() && line.charAt(i + 1) == 'c') {
                i += 10;
                continue;
            }

            int cp = line.codePointAt(i);
            int cpLen = Character.charCount(cp);
            i += cpLen;

            MsdfFont.Glyph glyph = font.glyph(cp);
            int glyphCode = cp;
            if (glyph == null) {
                glyph = font.glyph('?');
                glyphCode = '?';
                if (glyph == null) {
                    continue;
                }
            }

            if (prevCodepoint != -1) {
                penX += font.kerning(prevCodepoint, glyphCode) * scale;
            }

            if (glyph.renderable) {
                float x0 = penX + glyph.planeLeft * scale;
                float y0 = baselineY - glyph.planeTop * scale;
                float x1 = penX + glyph.planeRight * scale;
                float y1 = baselineY - glyph.planeBottom * scale;
                float width = x1 - x0;
                float height = y1 - y0;
                if (width > 0f && height > 0f) {
                    backend.enqueueMsdfGlyph(texture, pxRange,
                            x0, y0, width, height,
                            glyph.u0, glyph.v1, glyph.u1, glyph.v0,
                            color, matrix);
                }
            }

            penX += glyph.advance * scale;
            prevCodepoint = glyphCode;
        }
    }

    private void drawTextLineWithColors(float x,
                                        float baseline,
                                        float scale,
                                        String line,
                                        int defaultColor,
                                        float[] matrix,
                                        int texture,
                                        float pxRange) {
        if (line.isEmpty()) {
            return;
        }
        
        float penX = x;
        float baselineY = baseline;
        int prevCodepoint = -1;
        int currentColor = defaultColor;
        

        StringBuilder colorBuffer = new StringBuilder();
        boolean colorFormat = false;
        boolean customColorFormat = false;

        for (int i = 0; i < line.length();) {
            char ch = line.charAt(i);
            

            if (ch == '§' && i + 1 < line.length()) {
                colorFormat = true;
                i++;
                continue;
            } else if (colorFormat) {
                colorFormat = false;
                char colorCode = Character.toLowerCase(line.charAt(i));
                if (COLOR_CODES.containsKey(colorCode)) {
                    currentColor = COLOR_CODES.get(colorCode);
                } else if (colorCode == 'r') {
                    currentColor = defaultColor;
                }
                i++;
                continue;
            }
            

            if (ch == '⏏') {
                if (customColorFormat) {

                    try {
                        int customColor = Integer.parseInt(colorBuffer.toString());
                        currentColor = 0xFF000000 | customColor;
                    } catch (NumberFormatException ignored) {

                    }
                    colorBuffer.setLength(0);
                }
                customColorFormat = !customColorFormat;
                i++;
                continue;
            } else if (customColorFormat) {
                colorBuffer.append(ch);
                i++;
                continue;
            }

            int cp = line.codePointAt(i);
            int cpLen = Character.charCount(cp);
            i += cpLen;

            MsdfFont.Glyph glyph = font.glyph(cp);
            int glyphCode = cp;
            if (glyph == null) {
                glyph = font.glyph('?');
                glyphCode = '?';
                if (glyph == null) {
                    continue;
                }
            }

            if (prevCodepoint != -1) {
                penX += font.kerning(prevCodepoint, glyphCode) * scale;
            }

            if (glyph.renderable) {
                float x0 = penX + glyph.planeLeft * scale;
                float y0 = baselineY - glyph.planeTop * scale;
                float x1 = penX + glyph.planeRight * scale;
                float y1 = baselineY - glyph.planeBottom * scale;
                float width = x1 - x0;
                float height = y1 - y0;
                if (width > 0f && height > 0f) {
                    backend.enqueueMsdfGlyph(texture, pxRange,
                            x0, y0, width, height,
                            glyph.u0, glyph.v1, glyph.u1, glyph.v0,
                            currentColor, matrix);
                }
            }

            penX += glyph.advance * scale;
            prevCodepoint = glyphCode;
        }
    }

    public TextMetrics measureText(String text, float size) {
        // Process text through TextFactoryEvent for accurate measurement
        String processedText = processTextThroughEvent(text);
        
        if (size <= 0f) {
            return new TextMetrics(0f, 0f);
        }
        String content = processedText == null ? "" : processedText;
        if (content.isEmpty()) {
            return new TextMetrics(0f, 0f);
        }
        float scale = size / Math.max(1e-6f, font.emSize());
        float lineHeight = font.lineHeight() * scale;
        String[] lines = content.split("\\n", -1);
        float maxWidth = 0f;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, measureLineWidth(line, scale));
        }
        float height = Math.max(lineHeight * lines.length, lineHeight);
        return new TextMetrics(maxWidth, height);
    }
    
    /**
     * Processes text through TextFactoryEvent to allow modules like NameProtect to modify it
     */
    private String processTextThroughEvent(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        try {
            TextFactoryEvent event = new TextFactoryEvent(text);
            KefirDLC.getInstance().getEventBus().post(event);
            return event.getText();
        } catch (Exception e) {
            // If event system fails, return original text
            return text;
        }
    }

    private float measureLineWidth(String line, float scale) {
        if (line.isEmpty()) {
            return 0f;
        }
        float penX = 0f;
        int prevCodepoint = -1;
        

        StringBuilder colorBuffer = new StringBuilder();
        boolean colorFormat = false;
        boolean customColorFormat = false;
        
        for (int i = 0; i < line.length();) {
            char ch = line.charAt(i);
            

            if (ch == '\\' && i + 9 < line.length() && line.charAt(i + 1) == 'c') {
                i += 10;
                continue;
            }
            

            if (ch == '§' && i + 1 < line.length()) {
                colorFormat = true;
                i++;
                continue;
            } else if (colorFormat) {
                colorFormat = false;
                i++;
                continue;
            }
            

            if (ch == '⏏') {
                if (customColorFormat) {
                    colorBuffer.setLength(0);
                }
                customColorFormat = !customColorFormat;
                i++;
                continue;
            } else if (customColorFormat) {
                colorBuffer.append(ch);
                i++;
                continue;
            }
            
            int cp = line.codePointAt(i);
            int cpLen = Character.charCount(cp);
            i += cpLen;

            MsdfFont.Glyph glyph = font.glyph(cp);
            int glyphCode = cp;
            if (glyph == null) {
                glyph = font.glyph('?');
                glyphCode = '?';
                if (glyph == null) {
                    continue;
                }
            }

            if (prevCodepoint != -1) {
                penX += font.kerning(prevCodepoint, glyphCode) * scale;
            }
            penX += glyph.advance * scale;
            prevCodepoint = glyphCode;
        }
        return penX;
    }

    /**
     * Преобразован в record для совместимости с Renderer2D (методы width() и height() создаются автоматически)
     */
    public record TextMetrics(float width, float height) {
    }


    
    /**
     * Рисует текст по центру
     */
    public void drawCenteredText(float x, float y, float size, String text, int color) {
        // Process text through TextFactoryEvent for NameProtect and other modules
        String processedText = processTextThroughEvent(text);
        TextMetrics metrics = measureText(processedText, size);
        drawText(x - metrics.width() / 2f, y, size, processedText, color);
    }
    
    /**
     * Рисует градиентный текст
     */
    public void drawGradientText(float x, float y, float size, String text, int colorStart, int colorEnd) {
        if (text == null || text.isEmpty()) return;
        
        // Process text through TextFactoryEvent for NameProtect and other modules
        String processedText = processTextThroughEvent(text);
        String cleanText = removeColorCodes(processedText);
        int length = cleanText.length();
        
        StringBuilder gradientText = new StringBuilder();
        for (int i = 0; i < length; i++) {
            float progress = length > 1 ? (float) i / (length - 1) : 0f;
            int color = interpolateColor(colorStart, colorEnd, progress);
            gradientText.append(ColorUtils.formatting(color)).append(cleanText.charAt(i));
        }
        
        drawText(x, y, size, gradientText.toString(), colorStart);
    }
    
    public void findStyle(StringBuilder sb, Text component) {
        Style style = component.getStyle();
        if (component.getSiblings().isEmpty()) {
            if (style.getColor() != null) sb.append(ColorUtils.formatting(style.getColor().getRgb()));
            sb.append(component.getString()).append(Formatting.RESET);
        } else component.getWithStyle(style).forEach(text -> findStyle(sb,text));
    }
    
    /**
     * Удаляет цветовые коды из текста
     */
    public String removeColorCodes(String text) {
        if (text == null || text.isEmpty()) return text;
        
        StringBuilder result = new StringBuilder();
        boolean colorFormat = false;
        boolean customColorFormat = false;
        StringBuilder colorBuffer = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            

            if (ch == '§' && i + 1 < text.length()) {
                colorFormat = true;
                continue;
            } else if (colorFormat) {
                colorFormat = false;
                continue;
            }
            

            if (ch == '⏏') {
                if (customColorFormat) {
                    colorBuffer.setLength(0);
                }
                customColorFormat = !customColorFormat;
                continue;
            } else if (customColorFormat) {
                colorBuffer.append(ch);
                continue;
            }
            
            result.append(ch);
        }
        
        return result.toString();
    }
    

    private int interpolateColor(int colorStart, int colorEnd, float t) {
        float startAlpha = (colorStart >> 24 & 255) / 255.0F;
        float startRed = (colorStart >> 16 & 255) / 255.0F;
        float startGreen = (colorStart >> 8 & 255) / 255.0F;
        float startBlue = (colorStart & 255) / 255.0F;

        float endAlpha = (colorEnd >> 24 & 255) / 255.0F;
        float endRed = (colorEnd >> 16 & 255) / 255.0F;
        float endGreen = (colorEnd >> 8 & 255) / 255.0F;
        float endBlue = (colorEnd & 255) / 255.0F;

        float alpha = startAlpha + t * (endAlpha - startAlpha);
        float red = startRed + t * (endRed - startRed);
        float green = startGreen + t * (endGreen - startGreen);
        float blue = startBlue + t * (endBlue - startBlue);

        return ((int) (alpha * 255.0F) << 24) | ((int) (red * 255.0F) << 16) | ((int) (green * 255.0F) << 8) | (int) (blue * 255.0F);
    }
}