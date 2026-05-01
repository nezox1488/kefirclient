package kefirdlc.dev.ui.clickgui.elements;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.setting.api.Setting;
import kefirdlc.dev.module.setting.api.SettingElement;
import kefirdlc.dev.module.setting.impl.*;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;
import kefirdlc.dev.util.render.text.FontRegistry;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SettingsPopup {
    private float x, y, width, height;
    private Function currentModule;
    private boolean active;
    private final List<SettingElement> elements = new ArrayList<>();

    public SettingsPopup() {
        this.width = 120;
    }

    public void open(Function module, float x, float y) {
        this.currentModule = module;
        this.x = x;
        this.y = y;
        this.active = true;
        this.elements.clear();


        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting bs) elements.add(new BooleanElement(bs));
            else if (setting instanceof NumberSetting ns) elements.add(new NumberElement(ns));
            else if (setting instanceof MultiBoxSetting ms) elements.add(new MultiBoxElement(ms));
            else if (setting instanceof ModeSetting ms) elements.add(new ModeElement(ms));
            else if (setting instanceof ColorSetting cs) elements.add(new ColorElement(cs));
            else if (setting instanceof StringSetting ss) elements.add(new TextElement(ss));
        }

        recalcHeight();
    }

    private void recalcHeight() {
        float h = 20;
        for (SettingElement e : elements) {
            h += e.getHeight();
        }
        this.height = h + 4;
    }

    public void close() {
        this.active = false;
        this.currentModule = null;
    }


    public void render(Renderer2D r, FontObject font, double mx, double my) {
        if (!active || currentModule == null) return;

        recalcHeight();


        r.shadow(x, y, width, height, 10, 2f, 1f, new Color(0,0,0, 100).getRGB());
        r.rect(x, y, width, height, 5, new Color(20, 20, 25, 250).getRGB());
        r.rectOutline(x, y, width, height, 5, new Color(40, 40, 45).getRGB(), 1f);


        r.text(FontRegistry.INTER_MEDIUM, x + width/2, y + 10, 8, currentModule.getName(), -1, "c");
        r.rect(x + 5, y + 18, width - 10, 1, 0, new Color(60, 60, 60).getRGB());


        float cy = y + 22;
        for (SettingElement e : elements) {
            e.render(r, font, x + 2, cy, width - 4, 1f);
            cy += e.getHeight();
        }
    }

    public void mouseClicked(double mx, double my, int btn) {
        if (!active) return;


        if (mx >= x && mx <= x + width && my >= y && my <= y + height) {
            float cy = y + 22;
            for (SettingElement e : elements) {

                if (e.mouseClicked(mx, my, btn, x + 2, cy, width - 4)) {
                    return;
                }
                cy += e.getHeight();
            }
        } else {

            close();
        }
    }

    public void mouseReleased(double mx, double my, int btn) {
        if (!active) return;
        for (SettingElement e : elements) e.mouseReleased(mx, my, btn);
    }

    public void mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (!active) return;
        float cy = y + 22;
        for (SettingElement e : elements) {
            e.mouseDragged(mx, my, btn, x + 2, cy, width - 4);
            cy += e.getHeight();
        }
    }
    public List<SettingElement> getElements() {
        return elements;
    }
    public boolean isActive() { return active; }
}
