package kefirdlc.dev.module.impl.combat.furry.modes;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.module.impl.combat.AttackAura;
import kefirdlc.dev.module.impl.combat.furry.Angle;
import kefirdlc.dev.module.impl.combat.furry.AngleSmoothMode;
import kefirdlc.dev.module.impl.combat.furry.AngleUtil;
import kefirdlc.dev.util.math.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class SnapSmoothMode extends AngleSmoothMode {
    public SnapSmoothMode() {
        super("Snap");
    }

    Random rand = new Random();
    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        // https://media.discordapp.net/attachments/1201306605799608421/1449798170908295330/lonygriefaura.gif?ex=694b6a03&is=694a1883&hm=5a903e54a2e6312e5fecee70f13a1883cb6da87443ece1ca2c1e412b582ef4fb&=https://media.discordapp.net/attachments/1201306605799608421/1449798170908295330/lonygriefaura.gif?ex=694b6a03&is=694a1883&hm=5a903e54a2e6312e5fecee70f13a1883cb6da87443ece1ca2c1e412b582ef4fb&=
        // https://media.discordapp.net/attachments/1423943180717527040/1424286051760996424/E63BFFCA-1CFB-490A-AB8A-4033D0B30225.gif?ex=694b8bc0&is=694a3a40&hm=4a880fe1b567d865aa1ec2c39a7268842b520fce7e12cdc32498adab3d99b23d&=
        if (KefirDLC.getInstance().getFunctionManager().getModule(AttackAura.class).isToggled()) {
            Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
            float yawDelta = angleDelta.getYaw();
            float pitchDelta = angleDelta.getPitch();
            float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
            float speed = entity != null ? 1 : 0.9F;

            float lineYaw = (Math.abs(yawDelta / rotationDifference) * 180);
            float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);

            float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
            float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);

            Angle moveAngle = new Angle(currentAngle.getYaw(), currentAngle.getPitch());
            moveAngle.setYaw(MathHelper.lerp(MathUtil.getRandom(speed, speed + Math.cos(MathUtil.getRandom(0.01f,4))), currentAngle.getYaw(),
                    currentAngle.getYaw() + moveYaw));
            moveAngle.setPitch(MathHelper.lerp(MathUtil.getRandom(speed, speed + Math.cos(MathUtil.getRandom(0.01f,4))), currentAngle.getPitch(),
                    currentAngle.getPitch() + movePitch));

            return new Angle(moveAngle.getYaw(), moveAngle.getPitch());
        }

        return  AngleUtil.cameraAngle();
    }
    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.13, 0.13, 0.13);
    }
}
