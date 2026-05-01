package kefirdlc.dev.module.impl.render.hud;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;

import java.awt.*;

public class TargetHudScreen extends HudElementScreen {

    private float animatedHealth = 1f;

    public TargetHudScreen() {
        super(8, 30, 150, 38);
    }

    @Override
    public void render(Renderer2D renderer, int alpha, int blurAlpha) {
    }

    private int resolveEntityTextureId(LivingEntity target) {
        if (target == null) return 0;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return 0;

        if (!(target instanceof AbstractClientPlayerEntity player)) {
            return 0;
        }

        Identifier textureId = player.getSkinTexture();
        var texture = client.getTextureManager().getTexture(textureId);
        var gpuTexture = texture.getGlTexture();
        if (gpuTexture instanceof GlTexture glTexture) {
            return glTexture.getGlId();
        }
        return 0;
    }

    public void render(Renderer2D renderer, int alpha, int blurAlpha, LivingEntity target) {
        this.width = 118;
        this.height = 30;

        String name = target == null ? "No target" : target.getName().getString();
        float hp = target == null ? 0f : Math.max(0, target.getHealth() + target.getAbsorptionAmount());
        float maxHp = target == null ? 20f : Math.max(1f, target.getMaxHealth() + target.getAbsorptionAmount());
        float hpPercent = Math.max(0f, Math.min(1f, hp / maxHp));

        animatedHealth += (hpPercent - animatedHealth) * 0.18f;

        renderer.shadow(x, y, width, height, 6, 1, 1, new Color(0, 0, 0, blurAlpha).getRGB());
        renderer.rect(x, y, width, height, 6, new Color(18, 18, 24, alpha).getRGB());

        float headSize = 18f;
        float headX = x + 5f;
        float headY = y + 6f;
        renderer.rect(headX, headY, headSize, headSize, 5f, new Color(35, 35, 45, 230).getRGB());
        int entityTexture = resolveEntityTextureId(target);
        if (entityTexture > 0) {
            renderer.drawRgbaTexture(entityTexture, headX + 1f, headY + 1f, headSize - 2f, headSize - 2f, Color.WHITE.getRGB());
        } else {
            String avatarLetter = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
            renderer.text(FontRegistry.SF_REGULAR, headX + headSize / 2f, headY + 11f, 8, avatarLetter, Color.WHITE.getRGB(), "c");
        }

        float textX = headX + headSize + 6f;
        renderer.text(FontRegistry.SF_REGULAR, textX, y + 11, 7.5f, name, Color.WHITE.getRGB());
        renderer.text(FontRegistry.SF_REGULAR, textX, y + 19, 7, String.format("%.1f hp", hp), new Color(220, 220, 220).getRGB());

        float barX = textX;
        float barY = y + 23;
        float barW = width - (textX - x) - 6f;
        float barH = 3.0f;

        renderer.rect(barX, barY, barW, barH, 1.5f, new Color(45, 45, 55, 190).getRGB());
        renderer.rect(barX, barY, barW * animatedHealth, barH, 1.5f, new Color(85, 145, 255, 240).getRGB());
    }
}
