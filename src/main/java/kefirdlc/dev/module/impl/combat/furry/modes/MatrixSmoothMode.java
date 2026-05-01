package kefirdlc.dev.module.impl.combat.furry.modes;
// coded by sitoku \\
// since 27.04.2026 \\


import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.module.impl.combat.AttackAura;
import kefirdlc.dev.module.impl.combat.furry.Angle;
import kefirdlc.dev.module.impl.combat.furry.AngleSmoothMode;
import kefirdlc.dev.module.impl.combat.furry.AngleUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.security.SecureRandom;

public class MatrixSmoothMode extends AngleSmoothMode {
    public MatrixSmoothMode() {
        super("Matrix");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        if (KefirDLC.getInstance().getFunctionManager().getModule(AttackAura.class).isToggled()){
            Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
            float yawDelta = angleDelta.getYaw();
            float pitchDelta = angleDelta.getPitch();

            float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

            float straightLineYaw = Math.abs(yawDelta / rotationDifference) * 360.0F;
            float straightLinePitch = Math.abs(pitchDelta / rotationDifference) * 360.0F;

            return new Angle(currentAngle.getYaw() + Math.min(Math.max(yawDelta, -straightLineYaw), straightLineYaw), currentAngle.getPitch() + Math.min(Math.max(pitchDelta, -straightLinePitch), straightLinePitch));
        }

        return  AngleUtil.cameraAngle();
    }


    private float randomLerp(float min, float max) {
        return MathHelper.lerp(new SecureRandom().nextFloat(), min, max);
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.1, 0.1, 0.1);
    }
}