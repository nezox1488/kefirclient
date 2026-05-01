package kefirdlc.dev.mixin.player;
// coded by sitoku \\
// since 27.04.2026 \\

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import kefirdlc.dev.module.impl.combat.furry.RotationController;
import kefirdlc.dev.util.wrapper.Wrapper;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;



@Mixin(PlayerEntity.class)
public class PlayerEntityMixin  implements Wrapper {



        @ModifyExpressionValue(
                method = {
                        "knockbackTarget",
                        "doSweepingAttack"
                },
                at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F")
        )
        private float hookAttackRotation(float original) {

            return RotationController.INSTANCE.getMoveRotation().getYaw();
        }

}
