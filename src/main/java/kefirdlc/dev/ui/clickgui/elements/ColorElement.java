package kefirdlc.dev.ui.clickgui.elements;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.setting.api.Setting;
import kefirdlc.dev.module.setting.api.SettingElement;
import kefirdlc.dev.module.setting.impl.ColorSetting;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;

import java.awt.*;

import static kefirdlc.dev.util.wrapper.Wrapper.mc;

public class ColorElement implements SettingElement {

    private final ColorSetting setting;
    private boolean expanded = false;

    private static final float PADDING = 6f;
    private static final float COMPONENT_GAP = 6f;
    private static final float HEADER_HEIGHT = 18f;


    private static final float SB_HEIGHT = 80f;
    private static final float SLIDER_HEIGHT = 10f;

    private static final float SLIDER_RADIUS = 4f;
    private static final float CORNER_RADIUS = 6f;

    private static final int CHECKER_LIGHT = 0xFF3C3C3C;
    private static final int CHECKER_DARK = 0xFF2A2A2A;
    private static final float CHECKER_SIZE = 4f;

    private static final float SLIDER_CURSOR_WIDTH = 4f;
    private static final int SLIDER_CURSOR_COLOR = 0xFFFFFFFF;


    private boolean draggingSB = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;

    public ColorElement(ColorSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(Renderer2D r, FontObject font, float x, float y, float width, float alpha) {

        if (expanded && (draggingSB || draggingHue || draggingAlpha)) {
            double mouseX = mc.mouse.getX() * mc.getWindow().getScaledWidth() / (double) mc.getWindow().getWidth();
            double mouseY = mc.mouse.getY() * mc.getWindow().getScaledHeight() / (double) mc.getWindow().getHeight();

            float startY = y + HEADER_HEIGHT + PADDING;
            float contentX = x + PADDING;
            float contentWidth = width - PADDING * 2;

            if (draggingSB) {
                updateSB(mouseX, mouseY, contentX, startY, contentWidth, SB_HEIGHT);
            } else if (draggingHue) {
                updateHue(mouseX, contentX, contentWidth);
            } else if (draggingAlpha) {
                updateAlpha(mouseX, contentX, contentWidth);
            }
        }

        int textAlpha = (int) (255 * alpha);

        r.text(font, x + 4, y + HEADER_HEIGHT / 2 + 3, 8, setting.getName(), new Color(255, 255, 255, textAlpha).getRGB(), "l");

        float swatchSize = 12f;
        float swatchX = x + width - swatchSize - 4;
        float swatchY = y + (HEADER_HEIGHT - swatchSize) / 2f;

        renderCheckerboard(r, swatchX, swatchY, swatchSize, swatchSize, 4f, alpha);
        r.rect(swatchX, swatchY, swatchSize, swatchSize, 3, applyAlpha(setting.getValue().getRGB(), alpha));

        if (!expanded) return;
        float startY = y + HEADER_HEIGHT + PADDING;
        float contentX = x + PADDING;
        float contentWidth = width - PADDING * 2;

        renderSBPicker(r, contentX, startY, contentWidth, SB_HEIGHT, alpha);

        float currentY = startY + SB_HEIGHT + COMPONENT_GAP;

        renderHueSlider(r, contentX, currentY, contentWidth, SLIDER_HEIGHT, alpha);

        currentY += SLIDER_HEIGHT + COMPONENT_GAP;

        renderAlphaSlider(r, contentX, currentY, contentWidth, SLIDER_HEIGHT, alpha);
    }

    private void renderHueSlider(Renderer2D r, float x, float y, float w, float h, float alpha) {

        r.pushRoundedClipRect(x, y, w, h, SLIDER_RADIUS, SLIDER_RADIUS, SLIDER_RADIUS, SLIDER_RADIUS);

        float[] hueStops = {0f, 1f/6f, 2f/6f, 3f/6f, 4f/6f, 5f/6f, 1f};

        for (int i = 0; i < hueStops.length - 1; i++) {
            float h1 = hueStops[i];
            float h2 = hueStops[i+1];

            int c1 = applyAlpha(Color.getHSBColor(h1, 1f, 1f).getRGB(), alpha);
            int c2 = applyAlpha(Color.getHSBColor(h2, 1f, 1f).getRGB(), alpha);

            float xPos = x + (w * h1);
            float nextXPos = x + (w * h2);


            float segWidth = (nextXPos - xPos) + 1.0f;


            r.gradient(xPos, y, segWidth, h, 0, 0, 0, 0, c1, c2, c2, c1);
        }

        r.popClipRect();

        float huePos = setting.getHsb()[0] * w;
        renderSliderCursor(r, x + huePos, y + h/2, alpha);
    }

    private void renderSBPicker(Renderer2D r, float x, float y, float w, float h, float alpha) {
        int colorHue = applyAlpha(Color.getHSBColor(setting.getHsb()[0], 1f, 1f).getRGB(), alpha);
        int colorWhite = applyAlpha(0xFFFFFFFF, alpha);
        int colorBlack = applyAlpha(0xFF000000, alpha);
        int colorTransparent = applyAlpha(0x00000000, alpha);

        r.gradient(x, y, w, h, CORNER_RADIUS, colorWhite, colorHue, colorHue, colorWhite);

        r.gradient(x, y, w, h, CORNER_RADIUS, colorTransparent, colorTransparent, colorBlack, colorBlack);

        float sat = setting.getHsb()[1];
        float bri = setting.getHsb()[2];
        float cx = x + (sat * w);
        float cy = y + ((1f - bri) * h);

    }

    private void renderAlphaSlider(Renderer2D r, float x, float y, float w, float h, float alpha) {
        r.pushRoundedClipRect(x, y, w, h, SLIDER_RADIUS, SLIDER_RADIUS, SLIDER_RADIUS, SLIDER_RADIUS);
        renderCheckerboard(r, x, y, w, h, SLIDER_RADIUS, alpha);
        r.popClipRect();

        Color c = Color.getHSBColor(setting.getHsb()[0], setting.getHsb()[1], setting.getHsb()[2]);
        int colorFull = applyAlpha(c.getRGB(), alpha);
        int colorTrans = applyAlpha(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0).getRGB(), alpha);

        r.gradient(x, y, w, h, SLIDER_RADIUS, colorFull, colorFull, colorTrans, colorTrans);

        float alphaVal = setting.getValue().getAlpha() / 255f;
        float cursorX = (1f - alphaVal) * w;
        renderSliderCursor(r, x + cursorX, y + h/2, alpha);
    }

    private void renderSliderCursor(Renderer2D r, float cx, float cy, float alpha) {
        float cw = SLIDER_CURSOR_WIDTH;
        float ch = SLIDER_HEIGHT + 2;
        r.rect(cx - cw/2, cy - ch/2, cw, ch, 2, applyAlpha(SLIDER_CURSOR_COLOR, alpha));
    }

    private void renderCheckerboard(Renderer2D r, float x, float y, float w, float h, float rad, float alpha) {
        float size = CHECKER_SIZE;
        boolean rowToggle = false;

        r.rect(x, y, w, h, rad, applyAlpha(CHECKER_DARK, alpha));

        for (float cy = y; cy < y + h; cy += size) {
            boolean colToggle = rowToggle;
            float rowH = Math.min(size, y + h - cy);
            for (float cx = x; cx < x + w; cx += size) {
                if (colToggle) {
                    float colW = Math.min(size, x + w - cx);
                    r.rect(cx, cy, colW, rowH, 0, applyAlpha(CHECKER_LIGHT, alpha));
                }
                colToggle = !colToggle;
            }
            rowToggle = !rowToggle;
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button, float x, float y, float width) {
        if (isHovered(mx, my, x, y, width, HEADER_HEIGHT)) {
            if (button == 1) {
                expanded = !expanded;
                return true;
            }
        }
        if (!expanded) return false;

        float startY = y + HEADER_HEIGHT + PADDING;
        float contentX = x + PADDING;
        float contentWidth = width - PADDING * 2;

        if (isHovered(mx, my, contentX, startY, contentWidth, SB_HEIGHT) && button == 0) {
            draggingSB = true;
            updateSB(mx, my, contentX, startY, contentWidth, SB_HEIGHT);
            return true;
        }
        float currentY = startY + SB_HEIGHT + COMPONENT_GAP;

        if (isHovered(mx, my, contentX, currentY, contentWidth, SLIDER_HEIGHT) && button == 0) {
            draggingHue = true;
            updateHue(mx, contentX, contentWidth);
            return true;
        }
        currentY += SLIDER_HEIGHT + COMPONENT_GAP;

        if (isHovered(mx, my, contentX, currentY, contentWidth, SLIDER_HEIGHT) && button == 0) {
            draggingAlpha = true;
            updateAlpha(mx, contentX, contentWidth);
            return true;
        }
        return false;
    }

    @Override public void mouseDragged(double mx, double my, int button, float x, float y, float width) {}

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        draggingSB = false;
        draggingHue = false;
        draggingAlpha = false;
    }

    private void updateSB(double mx, double my, float x, float y, float w, float h) {
        float sat = (float) ((mx - x) / w);
        float bri = 1f - (float) ((my - y) / h);
        setting.setSaturation(Math.max(0, Math.min(1, sat)));
        setting.setBrightness(Math.max(0, Math.min(1, bri)));
    }

    private void updateHue(double mx, float x, float w) {
        float hue = (float) ((mx - x) / w);
        setting.setHue(Math.max(0, Math.min(1, hue)));
    }

    private void updateAlpha(double mx, float x, float w) {
        float pos = (float) ((mx - x) / w);
        pos = Math.max(0, Math.min(1, pos));
        float alpha = 1f - pos;
        Color c = setting.getValue();
        setting.setValue(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255)));
    }

    private int applyAlpha(int argb, float alpha) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int newAlpha = (int) (a * alpha);
        return (newAlpha << 24) | (r << 16) | (g << 8) | b;
    }

    private boolean isHovered(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
                                                                                                                                     public float getHeight() {
        if (expanded) return HEADER_HEIGHT +                                                            PADDING + SB_HEIGHT + COMPONENT_GAP + SLIDER_HEIGHT + COMPONENT_GAP + SLIDER_HEIGHT + PADDING;
        return HEADER_HEIGHT;
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, float x, float y, float width) { return false; }
    @Override public Setting<?> getSetting() { return setting; }
    @Override public void updateHover(double mouseX, double mouseY, float x, float y, float width) {}
}