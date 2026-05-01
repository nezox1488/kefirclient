package kefirdlc.dev.mixin.player;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.event.impl.player.PlayerVelocityStrafeEvent;
import kefirdlc.dev.module.impl.combat.furry.RotationController;
import kefirdlc.dev.module.impl.render.NoRender;
import kefirdlc.dev.util.wrapper.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements Wrapper {

    @Shadow public abstract float getYaw();

    @Redirect(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d hookVelocity(Vec3d movementInput, float speed, float yaw) {
        if ((Object) this == mc.player) {

            float reportYaw = RotationController.INSTANCE.getRotation().getYaw();

            PlayerVelocityStrafeEvent event = new PlayerVelocityStrafeEvent(
                    movementInput,
                    speed,
                    reportYaw,
                    Entity.movementInputToVelocity(movementInput, speed, reportYaw)
            );

            KefirDLC.getInstance().getEventBus().post(event);
            return event.getVelocity();
        }
        return Entity.movementInputToVelocity(movementInput, speed, yaw);
    }



    @Inject(method = "isOnFire", at = @At("HEAD"), cancellable = true)
    private void onIsOnFire(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this != mc.player) return;
        if (KefirDLC.getInstance() == null) return;

        NoRender noRender = KefirDLC.getInstance().getFunctionManager().getModule(NoRender.class);
        if (noRender != null && noRender.isToggled() && noRender.fireOverlay.getValue()) {
            cir.setReturnValue(false);
        }
    }

}