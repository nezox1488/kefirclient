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

import java.util.concurrent.ThreadLocalRandom;

public class FunTimeMode extends AngleSmoothMode {



    public FunTimeMode() {
        super("FunTimeMode");
    }
    // https://media.discordapp.net/attachments/1201306605799608421/1449798170908295330/lonygriefaura.gif?ex=694b6a03&is=694a1883&hm=5a903e54a2e6312e5fecee70f13a1883cb6da87443ece1ca2c1e412b582ef4fb&=https://media.discordapp.net/attachments/1201306605799608421/1449798170908295330/lonygriefaura.gif?ex=694b6a03&is=694a1883&hm=5a903e54a2e6312e5fecee70f13a1883cb6da87443ece1ca2c1e412b582ef4fb&=
    // https://media.discordapp.net/attachments/1423943180717527040/1424286051760996424/E63BFFCA-1CFB-490A-AB8A-4033D0B30225.gif?ex=694b8bc0&is=694a3a40&hm=4a880fe1b567d865aa1ec2c39a7268842b520fce7e12cdc32498adab3d99b23d&=
    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        AttackAura aura = KefirDLC.getInstance().getFunctionManager().getModule(AttackAura.class);
        if (!aura.isToggled()) {
            return AngleUtil.cameraAngle();
        }

        AttackHandler attackHandler = KefirDLC.getInstance().getAttackPerpetrator().getAttackHandler();
        TimerUtil attackTimer = attackHandler.getAttackTimer();
        int count = attackHandler.getCount();


        Angle delta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        float yawDelta = delta.getYaw();
        float pitchDelta = delta.getPitch();

        float newYaw;
        float newPitch;

        if (entity != null) {

            boolean canAttack = attackHandler.canAttack(aura.getConfig(), 0);
            float baseSpeed = canAttack ? 0.9F : 0.6F;
            float speed = randomLerp(baseSpeed, baseSpeed + 0.1F);


            float maxRotation = 73f;
            yawDelta = MathHelper.clamp(yawDelta, -maxRotation, maxRotation);
            pitchDelta = MathHelper.clamp(pitchDelta, -maxRotation, maxRotation);

            newYaw = currentAngle.getYaw() + (yawDelta * speed);
            newPitch = currentAngle.getPitch() + (pitchDelta * speed);

        } else {
            // --- ЛОГИКА ПРОСТОЯ (Вернул твою математику) ---
            int suck = count % 3;
            float speed = attackTimer.finished(400) ? (randomBool() ? 0.4F : 0.2F) : -0.2F;
            float random = attackTimer.elapsedTime() / 40F + (count % 7);

            Angle randomAngle = switch (suck) {
                case 0 -> new Angle((float) Math.cos(random % 2), (float) Math.sin(random % 2));
                case 1 -> new Angle((float) Math.sin(random % 1.4), (float) Math.cos(random % 1.4));
                case 2 -> new Angle((float) Math.sin(random % 3), (float) -Math.cos(random % 3));
                default -> new Angle((float) -Math.cos(random % 1.523231), (float) Math.sin(random % 1.661));
            };

            float yawOffset = !attackTimer.finished(2000) ? randomLerp(20, 30) * randomAngle.getYaw() : 0;

            // Немного оптимизировал pitch2 (Math.cos), чтобы не создавать лишние объекты
            float pitch2 = randomLerp(0, 2) * (float) Math.cos((double) System.currentTimeMillis() / 5000.0);
            float pitchOffset = !attackTimer.finished(2000) ? randomLerp(4, 10) * randomAngle.getPitch() + pitch2 : 0;

            // Расчет движения (сглаживание в простое)
            // Используем clamp(0, 1) для множителя lerp, чтобы не улетало в NaN
            float lerpFactor = MathHelper.clamp(randomLerp(speed, speed + 0.2F), 0f, 1f);

            newYaw = MathHelper.lerp(lerpFactor, currentAngle.getYaw(), currentAngle.getYaw() + yawDelta) + yawOffset;
            newPitch = MathHelper.lerp(lerpFactor, currentAngle.getPitch(), currentAngle.getPitch() + pitchDelta) + pitchOffset;
        }


        float sensitivity = (float) (mc.options.getMouseSensitivity().getValue() * 0.8F + 0.2F);
        float gcd = sensitivity * sensitivity * sensitivity * 1.2F;

        float smoothYawDelta = newYaw - currentAngle.getYaw();
        float smoothPitchDelta = newPitch - currentAngle.getPitch();

        // Округляем дельту по сетке GCD
        smoothYawDelta -= smoothYawDelta % gcd;
        smoothPitchDelta -= smoothPitchDelta % gcd;

        return new Angle(
                currentAngle.getYaw() + smoothYawDelta,
                currentAngle.getPitch() + smoothPitchDelta
        );
    }

    @Override
    public Vec3d randomValue() {
        // Увеличил разброс до +/- 0.5, как ты просил
        return new Vec3d(
                randomLerp(-0.5f, 0.5f),
                randomLerp(-0.5f, 0.5f),
                randomLerp(-0.5f, 0.5f)
        );
    }

    private float randomLerp(float min, float max) {
        return MathHelper.lerp(ThreadLocalRandom.current().nextFloat(), min, max);
    }

    private boolean randomBool() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}