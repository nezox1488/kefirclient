package kefirdlc.dev.ui.clickgui.component;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.setting.api.Setting;
import kefirdlc.dev.module.setting.api.SettingElement;
import kefirdlc.dev.module.setting.impl.*;
import kefirdlc.dev.ui.clickgui.elements.*;
import kefirdlc.dev.ui.clickgui.ClickGuiScreen;
import kefirdlc.dev.util.input.KeyNameUtil;
import kefirdlc.dev.util.render.animation.Easings;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleButton {

    private final Function module;

    private final float localX;
    private float localY;
    private final float width;
    private final float baseHeight = 18;
    

    private boolean expanded = false;
    private float expandAnimation = 0.0f;
    

    private float hoverAnimation = 0.0f;
    private boolean isHovered = false;
    
    private final List<SettingElement> settingElements = new ArrayList<>();

    public ModuleButton(Function module, float localX, float localY, float width) {
        this.module = module;
        this.localX = localX;
        this.localY = localY;
        this.width = width;

        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting bs) settingElements.add(new BooleanElement(bs));
            else if (setting instanceof NumberSetting ns) settingElements.add(new NumberElement(ns));
            else if (setting instanceof MultiBoxSetting ms) settingElements.add(new MultiBoxElement(ms));
            else if (setting instanceof ModeSetting ms) settingElements.add(new ModeElement(ms));
            else if (setting instanceof ColorSetting cs) settingElements.add(new ColorElement(cs));
            else if (setting instanceof StringSetting ss) settingElements.add(new TextElement(ss));
        }
    }
    
    private void updateAnimations() {

        float targetExpand = expanded ? 1.0f : 0.0f;
        expandAnimation = lerp(expandAnimation, targetExpand, 0.15f);
        

        float targetHover = isHovered ? 1.0f : 0.0f;
        hoverAnimation = lerp(hoverAnimation, targetHover, 0.2f);
    }

    public void render(Renderer2D renderer, FontObject font, float panelX, float panelY, float alpha) {
        updateAnimations();
        
        float x = panelX + localX;
        float y = panelY + localY;


        float hoverFactor = hoverAnimation * 0.3f;
        int baseAlpha = (int) ((module.isToggled() ? 180 : 30) * alpha);
        int textAlpha = (int) (255 * alpha);

        Color baseColor = module.isToggled()
                ? new Color(60 + (int)(20 * hoverFactor), 100 + (int)(20 * hoverFactor), 180 + (int)(20 * hoverFactor), baseAlpha)
                : new Color(30 + (int)(15 * hoverFactor), 30 + (int)(15 * hoverFactor), 30 + (int)(15 * hoverFactor), baseAlpha);


        renderer.rect(x, y, width, baseHeight, 2, baseColor.getRGB());
        

        renderer.text(font, x + 4, y + baseHeight / 2 + 2.5f, 8, module.getName(),
                     new Color(255, 255, 255, textAlpha).getRGB(), "l");


        String keyText = "[" + KeyNameUtil.getKeyName(module.getKey()) + "]";
        renderer.text(font, x + width - 4, y + baseHeight / 2 + 2, 7, 
                     keyText, new Color(180, 180, 180, textAlpha).getRGB(), "r");


        if (expandAnimation > 0.01f && !settingElements.isEmpty()) {
            renderAnimatedSettings(renderer, font, x, y, alpha);
        }
    }
    
    private void renderAnimatedSettings(Renderer2D renderer, FontObject font, float x, float y, float alpha) {
        float settingsY = y + baseHeight + 1;
        

        float totalHeight = 0;
        for (SettingElement element : settingElements) {
            totalHeight += element.getHeight();
        }
        
        float animatedHeight = totalHeight * expandAnimation;
        
        if (animatedHeight > 1) {
            int settingsBgAlpha = (int)(50 * alpha * expandAnimation);
            renderer.rect(x, settingsY, width, animatedHeight, 2, 
                         new Color(20, 20, 25, settingsBgAlpha).getRGB());
        }

        float currentY = settingsY;
        for (int i = 0; i < settingElements.size(); i++) {
            SettingElement element = settingElements.get(i);
            

            float elementDelay = i * 0.08f;
            float elementAnimation = Math.max(0, Math.min(1, (expandAnimation - elementDelay) / (1.0f - elementDelay)));
            elementAnimation = Easings.EASE_OUT_CUBIC.ease(elementAnimation);
            
            if (elementAnimation > 0.01f) {

                float yOffset = (1.0f - elementAnimation) * 8;
                float elementAlpha = alpha * elementAnimation;
                
                element.render(renderer, font, x + 2, currentY - yOffset, width - 4, elementAlpha);
            }
            
            currentY += element.getHeight() * expandAnimation;
        }
    }

    public Function getModule() {
        return module;
    }

    public float getLocalX() {
        return localX;
    }

    public float getLocalY() {
        return localY;
    }

    public void setLocalY(float localY) {
        this.localY = localY;
    }

    public List<SettingElement> getSettingElements() {
        return settingElements;
    }

    public boolean isExpanded() {
        return expanded;
    }
    public boolean mouseClicked(double mx, double my, int button, float panelX, float panelY) {
        float x = panelX + localX;
        float y = panelY + localY ;


        if (isHovered(mx, my, x, y, width, baseHeight)) {

            boolean rightSideClick = mx > x + width - 30;
            if (rightSideClick && button == 0) {
                openBindPopup(x + width + 5, y);
                return true;
            }


            if (button == 0) {
                module.toggle();
                return true;
            } 

            else if (button == 1 && !settingElements.isEmpty()) {
                expanded = !expanded;
                return true;
            }
        }


        if (expanded && expandAnimation > 0.5f) {
            float sy = y + baseHeight;
            for (SettingElement element : settingElements) {
                if (element.mouseClicked(mx, my, button, x + 2, sy, width - 4)) {
                    return true;
                }
                sy += element.getHeight();
            }
        }
        return false;
    }

    private void openBindPopup(float px, float py) {
        ClickGuiScreen screen = (ClickGuiScreen) KefirDLC.getInstance().getFunctionManager().getModule(ClickGuiScreen.class);
        if (screen != null) {
            screen.getBindPopup().open(module, px, py);
        }
    }

    public void mouseDragged(double mx, double my, int button, float px, float py) {
        if (!expanded || expandAnimation < 0.5f) return;
        
        float sy = py + localY + baseHeight;
        for (SettingElement e : settingElements) {
            e.mouseDragged(mx, my, button, px + localX + 2, sy, width - 4);
            sy += e.getHeight();
        }
    }

    public void mouseReleased(double mx, double my, int button) {
        if (expanded) {
            for (SettingElement e : settingElements) e.mouseReleased(mx, my, button);
        }
    }
    
    public void updateHover(double mx, double my, float panelX, float panelY) {
        float x = panelX + localX;
        float y = panelY + localY;
        isHovered = isHovered(mx, my, x, y, width, baseHeight);
    }

    public float getHeight() {
        if (!expanded && expandAnimation < 0.01f) return baseHeight;
        
        float settingsHeight = 0;
        for (SettingElement e : settingElements) {
            settingsHeight += e.getHeight();
        }
        
        return baseHeight + (settingsHeight * expandAnimation);
    }

    private boolean isHovered(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
    
    private float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }
}
