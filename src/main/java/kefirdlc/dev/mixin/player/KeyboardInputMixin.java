package kefirdlc.dev.mixin.player;
// coded by sitoku \\
// since 27.04.2026 \\

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.event.impl.input.EventKeyboardInput;
import kefirdlc.dev.event.impl.input.InputEvent;
import kefirdlc.dev.module.impl.combat.furry.Angle;
import kefirdlc.dev.module.impl.combat.furry.RotationController;
import kefirdlc.dev.module.impl.combat.furry.RotationPlan;
import kefirdlc.dev.util.Player.PlayerInventoryComponent;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static kefirdlc.dev.util.wrapper.Wrapper.mc;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {



    @ModifyExpressionValue(method = "tick", at = @At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/util/PlayerInput;"))
    private PlayerInput tickHook(PlayerInput original) {
         InputEvent event = new InputEvent(original);
        KefirDLC.getInstance().getEventBus().post(event);
        PlayerInventoryComponent.input(event);
        return transformInput(event.getInput());
    }



    @Inject(method = "tick", at = @At("RETURN"))
    public void onTick(CallbackInfo ci) {


        EventKeyboardInput event = new EventKeyboardInput(movementVector.y, movementVector.x);
        KefirDLC.getInstance().getEventBus().post(event);


        this.movementVector = new Vec2f(event.getMovementSideways(), event.getMovementForward());
    }

    @Unique
    private PlayerInput transformInput(PlayerInput input) {
        RotationController rotationController = RotationController.INSTANCE;
        Angle angle = rotationController.getCurrentAngle();
        RotationPlan configurable = rotationController.getCurrentRotationPlan();

        if (mc.player == null || angle == null || configurable == null || !(configurable.isMoveCorrection() && configurable.isFreeCorrection())) {
            return input;
        }

        float deltaYaw = mc.player.getYaw() - angle.getYaw();
        float z = getMovementMultiplier(input.forward(), input.backward());
        float x = getMovementMultiplier(input.left(), input.right());
        float newX = x * MathHelper.cos(deltaYaw * 0.017453292f) - z * MathHelper.sin(deltaYaw * 0.017453292f);
        float newZ = z * MathHelper.cos(deltaYaw * 0.017453292f) + x * MathHelper.sin(deltaYaw * 0.017453292f);
        int movementSideways = Math.round(newX), movementForward = Math.round(newZ);

        return new PlayerInput(movementForward > 0F, movementForward < 0F, movementSideways > 0F, movementSideways < 0F, input.jump(), input.sneak(), input.sprint());
    }

    @Unique
    private static float getMovementMultiplier(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0F;
        } else {
            return positive ? 1.0F : -1.0F;
        }
    }
}