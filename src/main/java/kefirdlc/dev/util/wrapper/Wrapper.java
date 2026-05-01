package kefirdlc.dev.util.wrapper;
// coded by sitoku \\
// since 27.04.2026 \\

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.Window;

public interface Wrapper {
    MinecraftClient mc = MinecraftClient.getInstance();
    Tessellator tessellator = Tessellator.getInstance();
    RenderTickCounter tickCounter = mc.getRenderTickCounter();
    Window window = mc.getWindow();
}
