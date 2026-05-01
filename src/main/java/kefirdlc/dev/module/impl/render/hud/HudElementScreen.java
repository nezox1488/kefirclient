package kefirdlc.dev.module.impl.render.hud;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontRegistry;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
public abstract class HudElementScreen {
    protected float x;
    protected float y;
    protected float width;
    protected float height;

    @Setter
    protected boolean dragging;
    protected float dragOffsetX;
    protected float dragOffsetY;

    protected HudElementScreen(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(Renderer2D renderer, int alpha, int blurAlpha);

    public void startDrag(float mouseX, float mouseY) {
        dragging = true;
        dragOffsetX = mouseX - x;
        dragOffsetY = mouseY - y;
    }

    public void dragTo(float mouseX, float mouseY) {
        if (!dragging) return;
        x = mouseX - dragOffsetX;
        y = mouseY - dragOffsetY;
    }

    public boolean contains(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void renderEditorBounds(Renderer2D renderer) {
        renderer.rectOutline(x, y, width, height, 6,
                new Color(110, 170, 255, 180).getRGB(), 1f);
        renderer.text(FontRegistry.SF_REGULAR, x + width / 2f, y - 3, 8, getName(),
                new Color(220, 220, 255).getRGB(), "c");
    }

    public String getName() {
        return getClass().getSimpleName().replace("Screen", "");
    }
}
