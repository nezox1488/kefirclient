package kefirdlc.dev.ui.clickgui.component;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.setting.impl.BooleanSetting;
import kefirdlc.dev.util.input.KeyNameUtil;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;
import kefirdlc.dev.util.render.text.FontRegistry;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class BindPopup {
    private float x, y, width, height;


    private Function currentModule;
    private BooleanSetting currentSetting;

    private boolean isActive;
    private boolean listening;
    private float animProgress = 0f;

    private final float PADDING = 6;
    private final float BUTTON_HEIGHT = 18;
    private final float SPACING = 4;
    private final float HEADER_HEIGHT = 20;

    public BindPopup() {
        this.width = 100;
        this.height = PADDING + HEADER_HEIGHT + SPACING + BUTTON_HEIGHT + SPACING + BUTTON_HEIGHT + PADDING;
    }


    public void open(Function module, float x, float y) {
        this.currentModule = module;
        this.currentSetting = null;
        this.x = x;
        this.y = y;
        initOpen();
    }


    public void open(BooleanSetting setting, float x, float y) {
        this.currentSetting = setting;
        this.currentModule = null;
        this.x = x;
        this.y = y;
        initOpen();
    }

    private void initOpen() {
        this.isActive = true;
        this.listening = false;
        this.animProgress = 0f;
    }

    public void close() {
        this.isActive = false;
        this.listening = false;
        this.currentModule = null;
        this.currentSetting = null;
    }

    public void render(Renderer2D r, FontObject font, double mouseX, double mouseY) {
        if (!isActive) return;


        String name = currentModule != null ? currentModule.getName() : (currentSetting != null ? currentSetting.getName() : "");
        int currentKey = currentModule != null ? currentModule.getKey() : (currentSetting != null ? currentSetting.getKey() : -1);
        String modeName = currentModule != null ? currentModule.getBindMode().name() : "Toggle";

        if (animProgress < 1f) {
            animProgress += 0.12f;
            if (animProgress > 1f) animProgress = 1f;
        }

        float alpha = animProgress;
        int alphaInt = (int) (255 * alpha);
        int bgColor = new Color(15, 15, 20, (int)(240 * alpha)).getRGB();
        int accentColor = new Color(100, 160, 255, alphaInt).getRGB();
        int textColor = new Color(255, 255, 255, alphaInt).getRGB();
        int labelColor = new Color(140, 140, 145, alphaInt).getRGB();
        int hoverColor = new Color(255, 255, 255, (int)(15 * alpha)).getRGB();

        r.shadow(x, y, width, height, 8, 1.5f, 1f, new Color(0, 0, 0, (int)(100 * alpha)).getRGB());
        r.rect(x, y, width, height, 5, bgColor);


        r.text(FontRegistry.INTER_MEDIUM, x + width / 2, y + PADDING + 6, 8, name, textColor, "c");


        float bindY = y + PADDING + HEADER_HEIGHT;
        boolean hoverBind = isHovered(mouseX, mouseY, x + PADDING, bindY, width - PADDING * 2, BUTTON_HEIGHT);

        if (hoverBind || listening) {
            r.rect(x + PADDING, bindY, width - PADDING * 2, BUTTON_HEIGHT, 4, listening ? new Color(100, 160, 255, (int)(40 * alpha)).getRGB() : hoverColor);
        }
        if (listening) {
            r.rectOutline(x + PADDING, bindY, width - PADDING * 2, BUTTON_HEIGHT, 4, accentColor, 1f);
        }

        String keyName = KeyNameUtil.getKeyName(currentKey);
        String bindText = listening ? "..." : (keyName == null || keyName.isEmpty() ? "None" : keyName);

        r.text(FontRegistry.SF_REGULAR, x + PADDING + 4, bindY + BUTTON_HEIGHT/2 + 2, 7, "Key", labelColor, "l");
        r.text(FontRegistry.INTER_MEDIUM, x + width - PADDING - 4, bindY + BUTTON_HEIGHT/2 + 2, 7, bindText, listening ? accentColor : textColor, "r");


        float modeY = bindY + BUTTON_HEIGHT + SPACING;
        if (currentModule != null) {
            boolean hoverMode = isHovered(mouseX, mouseY, x + PADDING, modeY, width - PADDING * 2, BUTTON_HEIGHT);
            if (hoverMode) r.rect(x + PADDING, modeY, width - PADDING * 2, BUTTON_HEIGHT, 4, hoverColor);

            modeName = modeName.charAt(0) + modeName.substring(1).toLowerCase();
            r.text(FontRegistry.SF_REGULAR, x + PADDING + 4, modeY + BUTTON_HEIGHT/2 + 2, 7, "Mode", labelColor, "l");
            r.text(FontRegistry.INTER_MEDIUM, x + width - PADDING - 4, modeY + BUTTON_HEIGHT/2 + 2, 7, modeName, accentColor, "r");
        } else {

            r.text(FontRegistry.SF_REGULAR, x + width/2, modeY + BUTTON_HEIGHT/2 + 2, 7, "Setting Bind", labelColor, "c");
        }
    }

    public void mouseClicked(double mx, double my, int btn) {
        if (!isActive) return;
        if (!isHovered(mx, my, x, y, width, height)) {
            close();
            return;
        }

        float bindY = y + PADDING + HEADER_HEIGHT;
        float modeY = bindY + BUTTON_HEIGHT + SPACING;
        float btnWidth = width - PADDING * 2;

        if (isHovered(mx, my, x + PADDING, bindY, btnWidth, BUTTON_HEIGHT) && btn == 0) {
            listening = !listening;
            return;
        }

        if (currentModule != null && isHovered(mx, my, x + PADDING, modeY, btnWidth, BUTTON_HEIGHT) && btn == 0) {
            Function.BindMode current = currentModule.getBindMode();
            currentModule.setBindMode(current == Function.BindMode.TOGGLE ? Function.BindMode.HOLD : Function.BindMode.TOGGLE);
        }
    }

    public void keyTyped(int key) {
        if (isActive && listening) {
            int bind = (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_DELETE) ? -1 : key;

            if (currentModule != null) currentModule.setKey(bind);
            else if (currentSetting != null) currentSetting.setKey(bind);

            listening = false;
        }
    }

    private boolean isHovered(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
    public boolean isActive() { return isActive; }
}