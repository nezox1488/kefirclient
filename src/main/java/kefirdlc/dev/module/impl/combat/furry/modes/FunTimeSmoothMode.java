package kefirdlc.dev.module.impl.combat.furry.modes;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.module.impl.combat.AttackAura;
import kefirdlc.dev.module.impl.combat.furry.Angle;
import kefirdlc.dev.module.impl.combat.furry.AngleSmoothMode;
import kefirdlc.dev.module.impl.combat.furry.AngleUtil;
import kefirdlc.dev.module.impl.combat.furry.attack.AttackHandler;
import kefirdlc.dev.util.math.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.security.SecureRandom;

public class FunTimeSmoothMode extends AngleSmoothMode {
    public FunTimeSmoothMode() {
        super("FunTime");
    } // https://media.discordapp.net/attachments/1201306605799608421/1449798170908295330/lonygriefaura.gif?ex=694b6a03&is=694a1883&hm=5a903e54a2e6312e5fecee70f13a1883cb6da87443ece1ca2c1e412b582ef4fb&=https://media.discordapp.net/attachments/1201306605799608421/1449798170908295330/lonygriefaura.gif?ex=694b6a03&is=694a1883&hm=5a903e54a2e6312e5fecee70f13a1883cb6da87443ece1ca2c1e412b582ef4fb&=
    // https://media.discordapp.net/attachments/1423943180717527040/1424286051760996424/E63BFFCA-1CFB-490A-AB8A-4033D0B30225.gif?ex=694b8bc0&is=694a3a40&hm=4a880fe1b567d865aa1ec2c39a7268842b520fce7e12cdc32498adab3d99b23d&=
    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        if (KefirDLC.getInstance().getFunctionManager().getModule(AttackAura.class).isToggled()) {
            AttackHandler attackHandler = KefirDLC.getInstance().getAttackPerpetrator().getAttackHandler();

            TimerUtil attackTimer = attackHandler.getAttackTimer();
            int count = attackHandler.getCount();

            Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
            float yawDelta = angleDelta.getYaw(), pitchDelta = angleDelta.getPitch();
            float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

            if (entity != null) {
                float speed = attackHandler.canAttack(KefirDLC.getInstance().getFunctionManager().getModule(AttackAura.class).getConfig(), 0) ? 1 : new SecureRandom().nextBoolean() ? 0.4F : 0.2F;

                float lineYaw = (Math.abs(yawDelta / rotationDifference) * 180);
                float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);

                float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
                float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);

                Angle moveAngle = new Angle(currentAngle.getYaw(), currentAngle.getPitch());
                moveAngle.setYaw(MathHelper.lerp(randomLerp(speed, speed + 0.2F), currentAngle.getYaw(), currentAngle.getYaw() + moveYaw));
                moveAngle.setPitch(MathHelper.lerp(randomLerp(speed, speed + 0.2F), currentAngle.getPitch(), currentAngle.getPitch() + movePitch));

                return moveAngle;
            } else {
                int suck = count % 3;
                float speed = attackTimer.finished(400) ? new SecureRandom().nextBoolean() ? 0.4F : 0.2F : -0.2F;
                float random = attackTimer.elapsedTime() / 40F + (count % 7);

                Angle randomAngle = switch (suck) {
                    case 0 -> new Angle((float) Math.cos(random % 2), (float) Math.sin(random% 2));
                    case 1 -> new Angle((float) Math.sin(random % 1.4), (float) Math.cos(random % 1.4));
                    case 2 -> new Angle((float) Math.sin(random % 3), (float) -Math.cos(random % 3));
                    default -> new Angle((float) -Math.cos(random % 1.11), (float) Math.sin(random% 1.11));
                };

                float yaw = !attackTimer.finished(2000) ? randomLerp(20, 30) * randomAngle.getYaw() : 0;
                float pitch2 = randomLerp(0, 2) * (float) Math.cos((double) System.currentTimeMillis() / 5000);
                float pitch = !attackTimer.finished(2000) ? randomLerp(4, 10) * randomAngle.getPitch() + pitch2 : 0;

                float lineYaw = (Math.abs(yawDelta / rotationDifference) * 180);
                float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);

                float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
                float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);

                Angle moveAngle = new Angle(currentAngle.getYaw(), currentAngle.getPitch());
                moveAngle.setYaw(MathHelper.lerp(Math.clamp(randomLerp(speed, speed + 0.2F), 0, 1), currentAngle.getYaw(), currentAngle.getYaw() + moveYaw) + yaw);
                moveAngle.setPitch(MathHelper.lerp(Math.clamp(randomLerp(speed, speed + 0.2F), 0, 1), currentAngle.getPitch(), currentAngle.getPitch() + movePitch) + pitch);

                return moveAngle;
            }
        }
        return AngleUtil.cameraAngle();
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.06, 0.1, 0.06);
    }

    private float randomLerp(float min, float max) {
        return MathHelper.lerp(new SecureRandom().nextFloat(), min, max);
    }
}