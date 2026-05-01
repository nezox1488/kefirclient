package kefirdlc.dev.module.impl.render.hud;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontRegistry;

import java.awt.*;

public class WatermarkScreen extends HudElementScreen {

    public WatermarkScreen() {
        super(8, 8, 168, 15);
    }

    @Override
    public void render(Renderer2D renderer, int alpha, int blurAlpha) {
    }

    public void render(Renderer2D renderer, int alpha, int blurAlpha, String client, String fps, String role) {
        float gap = 3f;
        float pad = 5f;
        float h = 14f;

        float w1 = renderer.measureText(FontRegistry.SF_REGULAR, client, 9.0f).width() + pad * 2;
        float w2 = renderer.measureText(FontRegistry.SF_REGULAR, fps, 9.0f).width() + pad * 2;
        float w3 = renderer.measureText(FontRegistry.SF_REGULAR, role, 9.0f).width() + pad * 2;

        float x1 = x;
        float x2 = x1 + w1 + gap;
        float x3 = x2 + w2 + gap;

        this.width = w1 + w2 + w3 + gap * 2;
        this.height = h;

        drawCard(renderer, x1, y, w1, h, alpha, blurAlpha);
        renderer.text(FontRegistry.SF_REGULAR, x1 + pad, y + 10.5f, 9.0f, client, Color.WHITE.getRGB());

        drawCard(renderer, x2, y, w2, h, alpha, blurAlpha);
        renderer.text(FontRegistry.SF_REGULAR, x2 + pad, y + 10.5f, 9.0f, fps, Color.WHITE.getRGB());

        drawCard(renderer, x3, y, w3, h, alpha, blurAlpha);
        renderer.text(FontRegistry.SF_REGULAR, x3 + pad, y + 10.5f, 9.0f, role, new Color(200, 200, 255).getRGB());
    }

    private void drawCard(Renderer2D renderer, float x, float y, float w, float h, int alpha, int blurAlpha) {
        renderer.shadow(x, y, w, h, 5, 1, 1, new Color(0, 0, 0, blurAlpha).getRGB());
        renderer.rect(x, y, w, h, 5, new Color(18, 18, 24, alpha).getRGB());
    }
}
