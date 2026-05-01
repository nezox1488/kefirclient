package kefirdlc.dev.ui.clickgui.elements;
// coded by sitoku \\
// since 27.04.2026 \\
import kefirdlc.dev.module.setting.api.Setting;
import kefirdlc.dev.module.setting.api.SettingElement;
import kefirdlc.dev.module.setting.impl.StringSetting;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class TextElement implements SettingElement {

    private final StringSetting setting;
    private boolean listening;
    private final float height = 18f;

    public TextElement(StringSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(Renderer2D r, FontObject font, float x, float y, float width, float alpha) {
        int textAlpha = (int) (255 * alpha);


        r.text(font, x + 4, y + 6, 7, setting.getName(), new Color(180, 180, 180, textAlpha).getRGB(), "l");


        float boxY = y + 10;
        float boxH = 12;
        int bgColor = listening ? new Color(40, 40, 45, textAlpha).getRGB() : new Color(30, 30, 35, textAlpha).getRGB();

        r.rect(x + 4, boxY, width - 8, boxH, 2, bgColor);
        if (listening) {
            r.rectOutline(x + 4, boxY, width - 8, boxH, 2, new Color(100, 160, 255, textAlpha).getRGB(), 1f);
        }

        String content = setting.getValue() + (listening && System.currentTimeMillis() % 1000 > 500 ? "_" : "");
        r.text(font, x + 7, boxY + boxH / 2 + 2, 7, content, new Color(255, 255, 255, textAlpha).getRGB(), "l");
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn, float x, float y, float width) {
        if (mx >= x && mx <= x + width && my >= y && my <= y + height) {
            listening = !listening;
            return true;
        }
        listening = false;
        return false;
    }


    public void keyTyped(int key, int scanCode, int action) {
        if (!listening || action == 0) return;

        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (!setting.getValue().isEmpty()) {
                setting.setValue(setting.getValue().substring(0, setting.getValue().length() - 1));
            }
        } else if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_ESCAPE) {
            listening = false;
        } else {

        }
    }

    public void handleKeyPress(int key) {
        if (!listening) return;


        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
            if (!setting.getValue().isEmpty()) {
                setting.setValue(setting.getValue().substring(0, setting.getValue().length() - 1));
            }
        }

        else if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            listening = false;
        }

        else {
            char c = keyToChar(key);
            if (c != 0) {
                setting.setValue(setting.getValue() + c);
            }
        }
    }



    private char keyToChar(int key) {
        boolean shift = org.lwjgl.glfw.GLFW.glfwGetKey(
                net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle(),
                org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

        if (key >= 65 && key <= 90) return (char) (key + (shift ? 0 : 32));
        if (key >= 48 && key <= 57) return (char) key;
        if (key == 32) return ' ';
        if (key == 45) return shift ? '_' : '-';
        if (key == 46) return '.';
        return 0;
    }

    public boolean isListening() {
        return listening;
    }
    @Override public float getHeight() { return height; }
    @Override public Setting<?> getSetting() { return setting; }
    @Override public void updateHover(double mx, double my, float x, float y, float w) {}
    @Override public void mouseReleased(double mx, double my, int b) {}
    @Override public void mouseDragged(double mx, double my, int b, float x, float y, float w) {}
    @Override public boolean mouseClicked(double mx, double my, float x, float y, float w) { return false; }
}