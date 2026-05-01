package kefirdlc.dev.module.impl.render.hud;
// coded by sitoku \\
// since 27.04.2026 \
// icon path: src/main/resources/asset/kefir.HudIcon/

import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontRegistry;

import java.awt.*;
import java.util.List;

public class HotkeysScreen extends HudElementScreen {

    public HotkeysScreen() {
        super(8, 75, 86, 16);
    }

    @Override
    public void render(Renderer2D renderer, int alpha, int blurAlpha) {
    }

    public void render(Renderer2D renderer, int alpha, int blurAlpha, List<String> activeBinds) {
        float lineHeight = 8f;
        float header = 11f;
        float content = activeBinds.isEmpty() ? 0 : activeBinds.size() * lineHeight + 4;

        this.height = header + content;
        float maxWidth = renderer.measureText(FontRegistry.SF_REGULAR, "Hotkeys", 8).width() + 12f;
        for (String bindText : activeBinds) {
            maxWidth = Math.max(maxWidth, renderer.measureText(FontRegistry.SF_REGULAR, bindText, 7).width() + 12f);
        }
        this.width = Math.min(98f, maxWidth);

        renderer.shadow(x, y, width, height, 5, 1, 1, new Color(0, 0, 0, blurAlpha).getRGB());
        renderer.rect(x, y, width, height, 5, new Color(18, 18, 24, alpha).getRGB());
        renderer.text(FontRegistry.SF_REGULAR, x + 5, y + 8f, 8, "Hotkeys", Color.WHITE.getRGB());
        float iconSize = 6f;
        float iconX = x + width - iconSize - 4f;
        float iconY = y + 4f;
        renderer.rect(iconX, iconY, iconSize, iconSize, 1.5f, new Color(70, 120, 255, alpha).getRGB());

        float yOffset = y + 14.5f;
        for (String bindText : activeBinds) {
            renderer.text(FontRegistry.SF_REGULAR, x + 6, yOffset, 7, bindText, new Color(220, 220, 220).getRGB());
            yOffset += lineHeight;
        }
    }
}
