package kefirdlc.dev.mixin.render;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.module.impl.render.NoRender;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderOverlay(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
        if (KefirDLC.getInstance() == null) return;
        NoRender noRender = KefirDLC.getInstance().getFunctionManager().getModule(NoRender.class);
        if (noRender == null || !noRender.isToggled()) return;
        if (!noRender.fireOverlay.getValue()) return;

        if (texture != null && texture.getPath().contains("fire")) {
            ci.cancel();
        }
    }
}
