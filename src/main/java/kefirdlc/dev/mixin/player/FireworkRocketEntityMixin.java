package kefirdlc.dev.mixin.player;
// coded by sitoku \\
// since 27.04.2026 \\

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.event.impl.game.FireworkEvent;
import kefirdlc.dev.module.impl.combat.furry.RotationController;
import kefirdlc.dev.util.wrapper.Wrapper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin extends ProjectileEntity implements Wrapper {

    @Shadow
    private LivingEntity shooter;

    @Unique
    private Vec3d rotation;

    public FireworkRocketEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }


    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d getRotationVectorHook(LivingEntity instance, Operation<Vec3d> original) {
        Vec3d result;
        if (shooter == mc.player) {
            result = RotationController.INSTANCE.getMoveRotation().toVector();
        } else {
            result = original.call(instance);
        }


        this.rotation = result;

        return result;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getVelocity()Lnet/minecraft/util/math/Vec3d;", ordinal = 0))
    public Vec3d getVelocityHook(LivingEntity instance, Operation<Vec3d> original) {
        if (shooter == mc.player) {
            FireworkEvent event = new FireworkEvent(original.call(instance));
            KefirDLC.getInstance().getEventBus().post(event);
            return event.getVector();
        }
        return original.call(instance);
    }

//    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;", ordinal = 0))
//    public Vec3d tick(Vec3d instance, double x, double y, double z) {
//
//        if (this.rotation == null) {
//            this.rotation = new Vec3d(x, y, z).normalize();
//        }
//
//        ElytraBooster elytraBooster = PenisMain.getInstance().getFunctionManager().getFunction(ElytraBooster.class);
//        if (elytraBooster.isEnabled()) {
//            if (elytraBooster.aimMode.is("Auto")) {
//                double boost = MobilityHandler.getBoost();
//                return instance.add(
//                        rotation.x * 0.1 + (rotation.x * boost - instance.x) * 0.5D,
//                        rotation.y * 0.1 + (rotation.y * boost - instance.y) * 0.5D,
//                        rotation.z * 0.1 + (rotation.z * boost - instance.z) * 0.5D
//                );
//            } else {
//                Vec3d boost = new Vec3d(
//                        elytraBooster.range.getFloatValue() / 2 + 0.3,
//                        elytraBooster.range.getFloatValue() / 2 + 0.3,
//                        elytraBooster.range.getFloatValue() / 2 + 0.3
//                );
//                return instance.add(
//                        rotation.x * 0.1 + (rotation.x * 1.5D - instance.x) * 0.5D,
//                        rotation.y * 0.1 + (rotation.y * 1.5D - instance.y) * 0.5D,
//                        rotation.z * 0.1 + (rotation.z * 1.5D - instance.z) * 0.5D
//                ).multiply(boost);
//            }
//        } else return instance.add(
//                rotation.x * 0.1 + (rotation.x * 1.5D - instance.x) * 0.5D,
//                rotation.y * 0.1 + (rotation.y * 1.5D - instance.y) * 0.5D,
//                rotation.z * 0.1 + (rotation.z * 1.5D - instance.z) * 0.5D
//        );
//    }
}