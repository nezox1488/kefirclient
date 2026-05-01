package kefirdlc.dev.mixin.game;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.event.impl.game.EventUpdate;
import kefirdlc.dev.event.impl.game.HotBarUpdateEvent;
import kefirdlc.dev.ui.clickgui.ClickGuiScreen;
import kefirdlc.dev.module.impl.render.NoRender;
import kefirdlc.dev.util.input.MouseHandler;
import kefirdlc.dev.util.others.Lisener.Counter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        EventUpdate eventUpdate = new EventUpdate();
        eventUpdate.call();
        MouseHandler.handleMouse();
        Counter.updateFPS();
        if (eventUpdate.isCanceled()) {
            ci.cancel();
        }
    }




   


    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;"), cancellable = true)
    public void handleInputEventsHook(CallbackInfo ci) {
        HotBarUpdateEvent event = new HotBarUpdateEvent();
        KefirDLC.getInstance().getEventBus().post(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "handleInputEvents", at = @At("HEAD"), cancellable = true)
    public void handleInputEvents(CallbackInfo ci) {
        ClickGuiScreen clickGui = KefirDLC.getInstance().getFunctionManager().getModule(ClickGuiScreen.class);
        if (clickGui != null && clickGui.isOpen()) {
            ci.cancel();
        }
    }


    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void onHasOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (KefirDLC.getInstance() == null) return;
        NoRender noRender = KefirDLC.getInstance().getFunctionManager().getModule(NoRender.class);
        if (noRender != null && noRender.isToggled() && noRender.glow.getValue()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    public void updateWindowTitle(CallbackInfoReturnable<String> cir) {
         cir.setReturnValue("KefirDLC 1.21.11 BETA ALPHA ++ MEGA BONK 2 RECODE ");
    }
}
