package kefirdlc.dev.ui.clickgui.elements;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.setting.api.Setting;
import kefirdlc.dev.module.setting.api.SettingElement;
import kefirdlc.dev.module.setting.impl.BooleanSetting;
import kefirdlc.dev.module.setting.impl.MultiBoxSetting;
import kefirdlc.dev.util.render.animation.Easings;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;

import java.awt.*;

public class MultiBoxElement implements SettingElement {

    private final MultiBoxSetting setting;
    private boolean expanded;
    private float expandAnimation = 0.0f;
    private final float headerHeight = 16f;
    private final float itemHeight = 12f;

    public MultiBoxElement(MultiBoxSetting setting) {
        this.setting = setting;
    }
    
    private void updateAnimation() {
        float target = expanded ? 1.0f : 0.0f;
        expandAnimation = lerp(expandAnimation, target, 0.15f);
    }

    @Override
    public void render(Renderer2D r, FontObject font,
                       float x, float y, float width, float alpha) {
        updateAnimation();


        r.text(font, x + 4, y + 12, 8,
                setting.getName(),
                new Color(255, 255, 255, (int)(255 * alpha)).getRGB(), "l");


        String indicator = expanded || expandAnimation > 0.5f ? " " : " ";
        r.text(font, x + width - 6, y + 12, 8,
                indicator,
                new Color(160, 160, 160, (int)(255 * alpha)).getRGB(), "r");


        if (expandAnimation > 0.01f) {
            float cy = y + headerHeight;
            
            for (int i = 0; i < setting.getSettings().size(); i++) {
                BooleanSetting bs = setting.getSettings().get(i);
                

                float elementDelay = i * 0.08f;
                float elementAnimation = Math.max(0, Math.min(1, (expandAnimation - elementDelay) / (1.0f - elementDelay)));
                elementAnimation = Easings.EASE_OUT_CUBIC.ease(elementAnimation);
                
                if (elementAnimation > 0.01f) {

                    float yOffset = (1.0f - elementAnimation) * 6;
                    float elementAlpha = alpha * elementAnimation;
                    
                    int color = bs.getValue()
                            ? new Color(100, 160, 255, (int)(255 * elementAlpha)).getRGB()
                            : new Color(128, 128, 128, (int)(255 * elementAlpha)).getRGB();

                    r.text(font, x + 8, cy - yOffset + 9, 7,
                            bs.getName(),
                            color, "l");
                }
                
                cy += itemHeight * expandAnimation;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my,
                                int button,
                                float x, float y, float width) {


        if (hover(mx, my, x, y, width, headerHeight) && button == 1) {
            expanded = !expanded;
            return true;
        }


        if (expanded && expandAnimation > 0.5f) {
            float cy = y + headerHeight;

            for (BooleanSetting bs : setting.getSettings()) {
                if (hover(mx, my, x, cy, width, itemHeight) && button == 0) {
                    bs.toggle();
                    return true;
                }
                cy += itemHeight;
            }
        }
        return false;
    }

    @Override
    public float getHeight() {
        if (!expanded && expandAnimation < 0.01f) {
            return headerHeight;
        }
        return headerHeight + setting.getSettings().size() * itemHeight * expandAnimation;
    }

    private boolean hover(double mx, double my,
                          float x, float y,
                          float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
    
    private float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }


    public void updateHover(double mx, double my, float x, float y, float w) {}
    public void mouseReleased(double mx, double my, int b) {}
    public void mouseDragged(double mx, double my, int b, float x, float y, float w) {}
    public boolean mouseClicked(double mx, double my, float x, float y, float w) { return false; }
    public Setting<?> getSetting() { return setting; }
}