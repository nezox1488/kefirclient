package kefirdlc.dev.module.impl.combat.furry.modes;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.module.impl.combat.AttackAura;
import kefirdlc.dev.module.impl.combat.furry.Angle;
import kefirdlc.dev.module.impl.combat.furry.AngleSmoothMode;
import kefirdlc.dev.module.impl.combat.furry.AngleUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class LinearSmoothMode extends AngleSmoothMode {
    public LinearSmoothMode() {
        super("Linear");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        if (KefirDLC.getInstance().getFunctionManager().getModule(AttackAura.class).isToggled()) {
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
    @Override
    public Vec3d randomValue() {
        return new Vec3d(0, 0, 0);
    }
}
