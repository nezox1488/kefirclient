package kefirdlc.dev.module.impl.render.hud;
// coded by sitoku \\
// since 27.04.2026 \
// icon path: src/main/resources/asset/kefir.HudIcon/

import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontRegistry;

import java.awt.*;
import java.util.List;

public class PotionsScreen extends HudElementScreen {
    private static final int POTIONS_ICON_TEXTURE = HudIconTexture.loadTextureId("assets/kefir/HudIcon/potions.png", "kefir_potions_icon");

    public record PotionLine(String text, boolean negative) {}

    public PotionsScreen() {
        super(8, 118, 128, 18);
    }

    @Override
    public void render(Renderer2D renderer, int alpha, int blurAlpha) {
    }

    public void render(Renderer2D renderer, int alpha, int blurAlpha, List<PotionLine> potions) {
        float maxTextWidth = renderer.measureText(FontRegistry.SF_REGULAR, "Potions", 8).width();
        for (PotionLine potion : potions) {
            maxTextWidth = Math.max(maxTextWidth, renderer.measureText(FontRegistry.SF_REGULAR, potion.text(), 7).width());
        }

        float padding = 10f;
        float lineHeight = 9f;
        float header = 13f;
        float content = potions.isEmpty() ? 0 : potions.size() * lineHeight + 4;

        this.height = header + content;
        this.width = Math.max(98, maxTextWidth + padding);

        renderer.shadow(x, y, width, height, 5, 1, 1, new Color(0, 0, 0, blurAlpha).getRGB());
        renderer.rect(x, y, width, height, 5, new Color(18, 18, 24, alpha).getRGB());
        renderer.text(FontRegistry.SF_REGULAR, x + width / 2f - 4f, y + 8.5f, 8, "Potions", Color.WHITE.getRGB(), "c");
        float iconSize = 8f;
        float iconX = x + width - iconSize - 6f;
        float iconY = y + 3f;
        renderer.drawRgbaTexture(POTIONS_ICON_TEXTURE, iconX, iconY, iconSize, iconSize, new Color(255, 255, 255, alpha).getRGB());
        renderer.rect(x + 7, y + 11, width - 14, 2, 1f, new Color(85, 145, 255, 220).getRGB());

        float yOffset = y + 19;
        for (PotionLine line : potions) {
            int color = line.negative() ? new Color(255, 95, 95).getRGB() : new Color(220, 220, 220).getRGB();
            renderer.text(FontRegistry.SF_REGULAR, x + 6, yOffset, 7, line.text(), color);
            yOffset += lineHeight;
        }
    }
}
