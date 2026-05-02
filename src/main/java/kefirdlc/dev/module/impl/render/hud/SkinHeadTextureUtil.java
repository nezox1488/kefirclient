package kefirdlc.dev.module.impl.render.hud;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;

public final class SkinHeadTextureUtil {
    private SkinHeadTextureUtil() {
    }

    public static int resolveSkinTextureId(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getNetworkHandler() == null || player == null) {
            return 0;
        }

        Identifier textureId = resolveFromPlayerEntry(client, player);
        if (textureId == null) {
            textureId = resolveFromPlayerEntity(player);
        }
        if (textureId == null) {
            return 0;
        }

        var texture = client.getTextureManager().getTexture(textureId);
        GpuTexture gpuTexture = texture.getGlTexture();
        if (gpuTexture instanceof GlTexture glTexture) {
            return glTexture.getGlId();
        }

        if (texture == MissingSprite.getMissingSpriteTexture()) {
            return 0;
        }
        return 0;
    }

    private static Identifier resolveFromPlayerEntry(MinecraftClient client, PlayerEntity player) {
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(player.getUuid());
        if (entry == null) {
            return null;
        }

        try {
            Method getSkinTexture = entry.getClass().getMethod("getSkinTexture");
            Object result = getSkinTexture.invoke(entry);
            if (result instanceof Identifier id) {
                return id;
            }
        } catch (Exception ignored) {
        }

        try {
            Method getSkinTextures = entry.getClass().getMethod("getSkinTextures");
            Object skinTextures = getSkinTextures.invoke(entry);
            Method texture = skinTextures.getClass().getMethod("texture");
            Object result = texture.invoke(skinTextures);
            if (result instanceof Identifier id) {
                return id;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Identifier resolveFromPlayerEntity(PlayerEntity player) {
        try {
            Method getSkinTexture = player.getClass().getMethod("getSkinTexture");
            Object result = getSkinTexture.invoke(player);
            if (result instanceof Identifier id) {
                return id;
            }
        } catch (Exception ignored) {
        }

        try {
            Method getSkinTextures = player.getClass().getMethod("getSkinTextures");
            Object skinTextures = getSkinTextures.invoke(player);
            Method texture = skinTextures.getClass().getMethod("texture");
            Object result = texture.invoke(skinTextures);
            if (result instanceof Identifier id) {
                return id;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
