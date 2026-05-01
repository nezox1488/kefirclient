package kefirdlc.dev.module.impl.render.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;

public final class HudIconTexture {
    private HudIconTexture() {
    }

    public static int loadTextureId(String resourcePath, String dynamicName) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return 0;
            }

            try (InputStream stream = HudIconTexture.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (stream == null) {
                    return 0;
                }
                NativeImage image = NativeImage.read(stream);
                NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> dynamicName, image);
                Identifier id = new Identifier("kefir", "hud/" + dynamicName.toLowerCase());
                client.getTextureManager().registerTexture(id, texture);
                return texture.getGlTexture();
            }
        } catch (Exception ignored) {
            return 0;
        }
    }
}
