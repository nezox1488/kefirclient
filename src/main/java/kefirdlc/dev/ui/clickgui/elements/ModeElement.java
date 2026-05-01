package kefirdlc.dev.ui.clickgui.elements;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.setting.api.Setting;
import kefirdlc.dev.module.setting.api.SettingElement;
import kefirdlc.dev.module.setting.impl.ModeSetting;
import kefirdlc.dev.util.render.animation.Easings;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;

import java.awt.*;

public class ModeElement implements SettingElement {

    private final ModeSetting setting;
    private boolean open;
    private float expandAnimation = 0.0f;

    public ModeElement(ModeSetting s) {
        this.setting = s;
    }
    
    private void updateAnimation() {
        float target = open ? 1.0f : 0.0f;
        expandAnimation = lerp(expandAnimation, target, 0.15f);
    }

    @Override
    public void render(Renderer2D r, FontObject f, float x, float y, float w, float a) {
        updateAnimation();
        

        r.text(f, x + 4, y + 10, 8, setting.getName(), 
               new Color(255, 255, 255, (int)(255 * a)).getRGB(), "l");
        r.text(f, x + w - 4, y + 10, 8, setting.getValue(), 
               new Color(160, 160, 160, (int)(255 * a)).getRGB(), "r");


        if (expandAnimation > 0.01f) {
            float iy = y + 16;

            for (int i = 0; i < setting.getModes().size(); i++) {
                String m = setting.getModes().get(i);
                boolean isSelected = m.equals(setting.getValue());
                

                float elementDelay = i * 0.06f;
                float elementAnimation = Math.max(0, Math.min(1, (expandAnimation - elementDelay) / (1.0f - elementDelay)));
                elementAnimation = Easings.EASE_OUT_CUBIC.ease(elementAnimation);
                
                if (elementAnimation > 0.01f) {

                    float yOffset = (1.0f - elementAnimation) * 5;
                    float elementAlpha = a * elementAnimation;
                    
                    int color = isSelected 
                        ? new Color(120, 170, 220, (int)(255 * elementAlpha)).getRGB() 
                        : new Color(128, 128, 128, (int)(255 * elementAlpha)).getRGB();

                    r.text(f, x + 6, iy - yOffset + 8, 7, m, color, "l");
                }
                
                iy += 12 * expandAnimation;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b, float x, float y, float w) {

        if (isHovered(mx, my, x, y, w, 16)) {
            if (b == 1) {
                open = !open;
                return true;
            }
        }


        if (open && expandAnimation > 0.5f) {
            float iy = y + 16;
            for (String m : setting.getModes()) {
                if (mx >= x && mx <= x + w && my >= iy && my <= iy + 12) {
                    if (b == 0) {
                        setting.setValue(m);
                        return true;
                    }
                }
                iy += 12;
            }
        }
        return false;
    }

    @Override 
    public float getHeight() {
        if (!open && expandAnimation < 0.01f) {
            return 16;
        }
        return 16 + setting.getModes().size() * 12 * expandAnimation;
    }

    private boolean isHovered(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
    
    private float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    @Override public void updateHover(double mouseX, double mouseY, float x, float y, float width) {}
    @Override public void mouseReleased(double mouseX, double mouseY, int button) {}
    @Override public void mouseDragged(double mouseX, double mouseY, int button, float x, float y, float width) {}
    @Override public boolean mouseClicked(double mouseX, double mouseY, float x, float y, float width) { return false; }
    @Override public Setting<?> getSetting() { return setting; }
}