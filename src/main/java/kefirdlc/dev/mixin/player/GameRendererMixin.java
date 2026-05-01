package kefirdlc.dev.mixin.player;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.module.impl.render.NoRender;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onTiltViewWhenHurt(CallbackInfo ci) {
        if (KefirDLC.getInstance() == null) return;
        NoRender noRender = KefirDLC.getInstance().getFunctionManager().getModule(NoRender.class);
        if (noRender != null && noRender.isToggled() && noRender.hurtCam.getValue()) {
            ci.cancel();
        }
    }
}
