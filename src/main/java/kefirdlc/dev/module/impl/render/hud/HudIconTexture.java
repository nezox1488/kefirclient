package kefirdlc.dev.module.impl.render.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;

public final class HudIconTexture {
    private HudIconTexture() {
    }

    private static void forceWhiteRgbPreserveAlpha(NativeImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getColorArgb(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                image.setColorArgb(x, y, (alpha << 24) | 0x00FFFFFF);
            }
        }
    }

    private static InputStream openIconStream(String resourcePath) {
        ClassLoader loader = HudIconTexture.class.getClassLoader();
        InputStream direct = loader.getResourceAsStream(resourcePath);
        if (direct != null) return direct;

        String[] fallbackPaths = {
                resourcePath.replace("assets/", "asset/").replace("/HudIcon/", ".HudIcon/"),
                resourcePath.replace("/HudIcon/", "/hudicon/"),
                resourcePath.toLowerCase()
        };

        for (String path : fallbackPaths) {
            InputStream fallback = loader.getResourceAsStream(path);
            if (fallback != null) return fallback;
        }
        return null;
    }

    public static int loadTextureId(String resourcePath, String dynamicName) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return 0;
            }

            try (InputStream stream = openIconStream(resourcePath)) {
                if (stream == null) {
                    return 0;
                }
                NativeImage image = NativeImage.read(stream);
                forceWhiteRgbPreserveAlpha(image);
                NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> dynamicName, image);
                Identifier id = Identifier.of("kefir", "hud/" + dynamicName.toLowerCase());
                client.getTextureManager().registerTexture(id, texture);
                GpuTexture gpuTexture = texture.getGlTexture();
                if (gpuTexture instanceof GlTexture glTexture) {
                    return glTexture.getGlId();
                }
                return 0;
            }
        } catch (Exception ignored) {
            return 0;
        }
    }
}
