package kefirdlc.dev.ui.clickgui.elements;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.setting.api.Setting;
import kefirdlc.dev.module.setting.api.SettingElement;
import kefirdlc.dev.module.setting.impl.NumberSetting;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;

import java.awt.*;

import static kefirdlc.dev.util.wrapper.Wrapper.mc;

public class NumberElement implements SettingElement {
    private final NumberSetting setting;
    private final float height = 16;
    private boolean dragging;

    public NumberElement(NumberSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(Renderer2D renderer, FontObject font, float x, float y, float width, float alpha) {

        if (dragging) {
            double mouseX = mc.mouse.getX() * mc.getWindow().getScaledWidth() / (double) mc.getWindow().getWidth();
            setValueFromMouse(mouseX, x, width);
        }

        String display = setting.getName() + ": " + String.valueOf(setting.getValue().floatValue()).substring(0, Math.min(String.valueOf(setting.getValue().floatValue()).length(), 3));;

        renderer.text(font, x + 4, y + 7, 8, display, Color.GRAY.getRGB(), "l");

        float sliderHeight = 4;
        float sliderY = y + 10;
        float sliderWidth = width - 8;
        float sliderX = x + 4;

        renderer.rect(sliderX, sliderY, sliderWidth, sliderHeight, 2, new Color(40, 40, 45).getRGB());


        double percent = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());

        percent = Math.max(0, Math.min(1, percent));
        float fillWidth = (float) (sliderWidth * percent);

        renderer.rect(sliderX, sliderY, fillWidth, sliderHeight, 2, new Color(100, 150, 200).getRGB());

        renderer.circle(sliderX + fillWidth - 2, sliderY + 2, 3f, 2, 2, Color.WHITE.getRGB());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, float x, float y, float width) {
        if (isHovered(mouseX, mouseY, x, y, width) && button == 0) {
            dragging = true;
            setValueFromMouse(mouseX, x, width);
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, float x, float y, float width) {

    }

    private void setValueFromMouse(double mouseX, float x, float width) {

        double sliderEffectiveWidth = width - 8;
        double sliderStartX = x + 4;

        double percent = (mouseX - sliderStartX) / sliderEffectiveWidth;


        percent = Math.max(0, Math.min(1, percent));

        double value = setting.getMin() + (setting.getMax() - setting.getMin()) * percent;
        setting.setValueNumber(value);
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override public void updateHover(double mouseX, double mouseY, float x, float y, float width) {}
    @Override public boolean mouseClicked(double mouseX, double mouseY, float x, float y, float width) { return false; }

    @Override
    public Setting<?> getSetting() {
        return setting;
    }

    private boolean isHovered(double mouseX, double mouseY, float x, float y, float width) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}