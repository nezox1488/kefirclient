package kefirdlc.dev.mixin.render;
// coded by sitoku \\
// since 27.04.2026 \\


import com.mojang.blaze3d.buffers.GpuBufferSlice;
import kefirdlc.dev.util.math.MatrixCapture;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f basicProjectionMatrix, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        if (projectionMatrix == null) return;
        MatrixCapture.projectionMatrix.set(projectionMatrix);
        MatrixCapture.viewMatrix.set(positionMatrix);
    }
}