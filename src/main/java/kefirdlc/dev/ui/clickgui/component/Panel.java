package kefirdlc.dev.ui.clickgui.component;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;
import kefirdlc.dev.util.render.text.FontRegistry;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Panel {

    private final Category category;
    public float currentX, currentY;
    public float targetX;
    public float targetY;
    private final float width = 120;

    private boolean dragging = false;
    private float dragOffsetX, dragOffsetY;
    private boolean expanded = true;

    private final List<ModuleButton> allButtons = new ArrayList<>();

    private final float HEADER_HEIGHT = 24;
    private final float PADDING = 4;

    public Panel(Category category, float startX, float startY, List<Function> modules) {
        this.category = category;
        this.targetX = startX;
        this.targetY = startY;
        this.currentX = startX;
        this.currentY = startY;

        for (Function module : modules) {
            allButtons.add(new ModuleButton(module, PADDING, 0, width - PADDING * 2));
        }
    }

    private float getCurrentHeight() {
        if (!expanded) return HEADER_HEIGHT;

        float baseHeight = HEADER_HEIGHT + PADDING;
        float contentHeight = 0;
        for (ModuleButton btn : allButtons) {
            contentHeight += btn.getHeight() + PADDING;
        }

        return baseHeight + contentHeight;
    }

    private void updateButtonPositions(float startY) {
        float cy = startY;
        for (ModuleButton btn : allButtons) {
            btn.setLocalY(cy);
            cy += btn.getHeight() + PADDING;
        }
    }

    public void render(Renderer2D r, FontObject font, float alpha, float yAnimOffset, double mx, double my) {
        if (dragging) {
            targetX = (float) mx - dragOffsetX;
            targetY = (float) my - dragOffsetY;
            currentX = targetX;
            currentY = targetY;
        } else {
            currentX = lerp(currentX, targetX, 0.25f);
            currentY = lerp(currentY, targetY, 0.25f);
        }

        float height = getCurrentHeight();
        float renderY = currentY + yAnimOffset;

        int bgAlpha = (int) (180 * alpha);
        int headAlpha = (int) (220 * alpha);
        int textAlpha = (int) (255 * alpha);

        r.rect(currentX, renderY, width, height, 6, new Color(15, 15, 20, bgAlpha).getRGB());
        r.rect(currentX, renderY, width, HEADER_HEIGHT, 0, 6, 6, 0, new Color(35, 35, 40, headAlpha).getRGB());
        r.text(FontRegistry.INTER_MEDIUM, currentX + width / 2, renderY + HEADER_HEIGHT / 2 + 3, 10,
                category.getName(), new Color(255, 255, 255, textAlpha).getRGB(), "c");

        if (!expanded) return;

        updateButtonPositions(HEADER_HEIGHT + PADDING);
        for (ModuleButton btn : allButtons) {
            btn.updateHover(mx, my, currentX, renderY);
            btn.render(r, font, currentX, renderY, alpha);
        }
    }

    public void mouseClicked(double mx, double my, int btn) {
        if (!isHovered(mx, my)) return;

        if (my >= currentY && my <= currentY + HEADER_HEIGHT) {
            if (btn == 0) {
                dragging = true;
                dragOffsetX = (float) mx - currentX;
                dragOffsetY = (float) my - currentY;
            } else if (btn == 1) {
                expanded = !expanded;
            }
            return;
        }

        if (!expanded) return;
        for (ModuleButton mb : allButtons) {
            if (mb.mouseClicked(mx, my, btn, currentX, currentY)) return;
        }
    }

    public void mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (expanded && !dragging) {
            for (ModuleButton mb : allButtons) mb.mouseDragged(mx, my, btn, currentX, currentY);
        }
    }

    public void mouseReleased(double mx, double my, int btn) {
        dragging = false;
        if (expanded) {
            for (ModuleButton mb : allButtons) mb.mouseReleased(mx, my, btn);
        }
    }

    public boolean isHovered(double mx, double my) {
        return mx >= currentX && mx <= currentX + width && my >= currentY && my <= currentY + getCurrentHeight();
    }

    public Function getHoveredModule(double mx, double my) {
        if (!expanded) return null;

        for (ModuleButton button : allButtons) {
            float bx = currentX + button.getLocalX();
            float by = currentY + button.getLocalY();
            float bw = width - PADDING * 2;
            float bh = button.getHeight();
            if (mx >= bx && mx <= bx + bw && my >= by && my <= by + bh) {
                return button.getModule();
            }
        }
        return null;
    }

    private float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    public List<ModuleButton> buttons() {
        return allButtons;
    }

    public List<ModuleButton> getButtons() {
        return allButtons;
    }
}
