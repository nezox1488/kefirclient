package kefirdlc.dev.ui.clickgui.elements;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.module.setting.api.Setting;
import kefirdlc.dev.module.setting.api.SettingElement;
import kefirdlc.dev.module.setting.impl.BooleanSetting;
import kefirdlc.dev.ui.clickgui.ClickGuiScreen;
import kefirdlc.dev.util.render.animation.AnimationSystem;
import kefirdlc.dev.util.render.animation.Easings;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;

import java.awt.*;

public class BooleanElement implements SettingElement {

    private final BooleanSetting setting;
    private final float height = 14f;

    private float progress;
    private ToggleAnimation animation;

    public BooleanElement(BooleanSetting setting) {
        this.setting = setting;
        this.progress = setting.getValue() ? 1f : 0f;
    }

    @Override
    public void render(Renderer2D renderer, FontObject font,
                       float x, float y, float width, float alpha) {


        if (animation == null) {
            this.progress = setting.getValue() ? 1f : 0f;
        }


        renderer.text(
                font,
                x + 4,
                y + height / 2 + 2,
                8,
                setting.getName(),
                Color.WHITE.getRGB(),
                "l"
        );

        float switchWidth = 16f;
        float switchHeight = 8f;

        float sx = x + width - switchWidth - 4;
        float sy = y + (height - switchHeight) / 2f;

        Color offColor = new Color(60, 60, 60);
        Color onColor  = new Color(100, 150, 200);

        Color bg = lerp(offColor, onColor, progress);

        renderer.rect(sx, sy, switchWidth, switchHeight, 4, bg.getRGB());


        float knobX = sx + 1 + (switchWidth - switchHeight) * progress;
        float knobY = sy + 1;


        renderer.circle(
                knobX + 3,
                knobY + 3,
                3f,
                1,
                1,
                Color.WHITE.getRGB()
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, float x, float y, float width) {
        if (isHovered(mouseX, mouseY, x, y, width)) {
            if (button == 0) {

                setting.toggle();
                startAnimation(setting.getValue());
                return true;
            } else if (button == 1) {

                ClickGuiScreen screen = (ClickGuiScreen) KefirDLC.getInstance().getFunctionManager().getModule(ClickGuiScreen.class);
                if (screen != null) {
                    screen.getBindPopup().open(setting, x + width + 5, y);
                }
                return true;
            }
        }
        return false;
    }

    private void startAnimation(boolean enabled) {
        float target = enabled ? 1f : 0f;

        if (animation != null) {
            AnimationSystem.getInstance().unregister(animation);
        }
        animation = new ToggleAnimation(progress, target);
        AnimationSystem.getInstance().ensureRegistered(animation);
    }

    @Override public void updateHover(double mouseX, double mouseY, float x, float y, float width) {}
    @Override public void mouseReleased(double mouseX, double mouseY, int button) {}
    @Override public void mouseDragged(double mouseX, double mouseY, int button, float x, float y, float width) {}

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, float x, float y, float width) {
        return false;
    }

    @Override
    public Setting<?> getSetting() {
        return setting;
    }

    private boolean isHovered(double mouseX, double mouseY,
                              float x, float y, float width) {
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + height;
    }

    private static Color lerp(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (a.getRed()   + (b.getRed()   - a.getRed())   * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl= (int) (a.getBlue()  + (b.getBlue()  - a.getBlue())  * t);
        return new Color(r, g, bl);
    }

    private class ToggleAnimation implements AnimationSystem.Animated {

        private float value;
        private final float start;
        private final float target;
        private float time;

        private static final float DURATION = 0.18f;

        ToggleAnimation(float start, float target) {
            this.start = start;
            this.target = target;
            this.value = start;
            this.time = 0f;
        }

        @Override
        public boolean update(float deltaSeconds) {
            time += deltaSeconds;
            float t = Math.min(time / DURATION, 1f);
            float eased = Easings.EASE_OUT_CUBIC.ease(t);

            value = start + (target - start) * eased;


            BooleanElement.this.progress = value;

            if (t >= 1f) {
                BooleanElement.this.animation = null;
                return false;
            }
            return true;
        }
    }
}