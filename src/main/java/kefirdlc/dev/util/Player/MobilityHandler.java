package kefirdlc.dev.util.Player;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.util.wrapper.Wrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

@UtilityClass
public class MobilityHandler implements Wrapper {
    public static boolean isMoving() {
        Vec2f inputVector =  mc.player.input.movementVector;

        float forward = inputVector.y;
        float strafe = inputVector.x;
        return forward != 0.0 || strafe != 0.0;
    }

    public static double getSpeed() {
        return Math.hypot(mc.player.getVelocity().x, mc.player.getVelocity().z);
    }

    public static Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }
    public boolean hasPlayerMovement() {
        Vec2f inputVector =  mc.player.input.movementVector;

        float forward = inputVector.y;
        float strafe = inputVector.x;
        return forward != 0f || strafe != 0f;
    }

    public double[] calculateDirection(double distance) {
        Vec2f inputVector =  mc.player.input.movementVector;

        float forward = inputVector.y;
        float strafe = inputVector.x;
        return calculateDirection(forward, strafe, distance);
    }

    public double[] calculateDirection(float forward, float sideways, double distance) {
        float yaw = mc.player.getYaw();
        if (forward != 0.0f) {
            if (sideways > 0.0f) {
                yaw += (forward > 0.0f) ? -45 : 45;
            } else if (sideways < 0.0f) {
                yaw += (forward > 0.0f) ? 45 : -45;
            }
            sideways = 0.0f;
            forward = (forward > 0.0f) ? 1.0f : -1.0f;
        }

        double sinYaw = Math.sin(Math.toRadians(yaw + 90.0f));
        double cosYaw = Math.cos(Math.toRadians(yaw + 90.0f));
        double xMovement = forward * distance * cosYaw + sideways * distance * sinYaw;
        double zMovement = forward * distance * sinYaw - sideways * distance * cosYaw;

        return new double[]{xMovement, zMovement};
    }

    public double getSpeedSqrt(Entity entity) {
        return Math.sqrt(entity.squaredDistanceTo(new Vec3d(entity.lastX, entity.lastY, entity.lastZ)));
    }

    public void setVelocity(double velocity) {
        final double[] direction = MobilityHandler.calculateDirection(velocity);
        Objects.requireNonNull(mc.player).setVelocity(direction[0], mc.player.getVelocity().getY(), direction[1]);
    }

    public void setVelocity(double velocity, double y) {
        final double[] direction = MobilityHandler.calculateDirection(velocity);
        Objects.requireNonNull(mc.player).setVelocity(direction[0], y, direction[1]);
    }

    public double getDegreesRelativeToView(
            Vec3d positionRelativeToPlayer,
            float yaw) {

        float optimalYaw =
                (float) Math.atan2(-positionRelativeToPlayer.x, positionRelativeToPlayer.z);
        double currentYaw = Math.toRadians(MathHelper.wrapDegrees(yaw));

        return Math.toDegrees(MathHelper.wrapDegrees((optimalYaw - currentYaw)));
    }

    public PlayerInput getDirectionalInputForDegrees(PlayerInput input, double dgs, float deadAngle) {
        boolean forwards = input.forward();
        boolean backwards = input.backward();
        boolean left = input.left();
        boolean right = input.right();

        if (dgs >= (-90.0F + deadAngle) && dgs <= (90.0F - deadAngle)) {
            forwards = true;
        } else if (dgs < (-90.0F - deadAngle) || dgs > (90.0F + deadAngle)) {
            backwards = true;
        }

        if (dgs >= (0.0F + deadAngle) && dgs <= (180.0F - deadAngle)) {
            right = true;
        } else if (dgs >= (-180.0F + deadAngle) && dgs <= (0.0F - deadAngle)) {
            left = true;
        }

        return new PlayerInput(forwards, backwards, left, right, input.jump(), input.sneak(), input.sprint());
    }

    public PlayerInput getDirectionalInputForDegrees(PlayerInput input, double dgs) {
        return getDirectionalInputForDegrees(input, dgs, 20.0F);
    }
    public static double[] forward(final double speed) {

        var player = mc.player;
        if (player == null) return new double[]{0, 0};


        Vec2f inputVector = player.input.movementVector;

        float forward = inputVector.y;
        float strafe = inputVector.x;
        float yaw = player.getYaw();

        if (forward != 0.0f) {
            if (strafe > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (strafe < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            strafe = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }

        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));

        final double motionX = forward * speed * cos + strafe * speed * sin;
        final double motionZ = forward * speed * sin - strafe * speed * cos;

        return new double[]{motionX, motionZ};
    }

    public static void setMotion(double speed) {




        Vec2f inputVector =  mc.player.input.movementVector;

        float forward = inputVector.y;
        float strafe = inputVector.x;
        float yaw = mc.player.getYaw();
        if (forward == 0 && strafe == 0) {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
        } else {
            if (forward != 0) {
                if (strafe > 0) {
                    yaw += (float) (forward > 0 ? -45 : 45);
                } else if (strafe < 0) {
                    yaw += (float) (forward > 0 ? 45 : -45);
                }
                strafe = 0;
                if (forward > 0) {
                    forward = 1;
                } else if (forward < 0) {
                    forward = -1;
                }
            }
            double sin = MathHelper.sin((float) Math.toRadians(yaw + 90));
            double cos = MathHelper.cos((float) Math.toRadians(yaw + 90));
            mc.player.setVelocity(forward * speed * cos + strafe * speed * sin, mc.player.getVelocity().y, forward * speed * sin - strafe * speed * cos);
        }
    }

    public static float getMoveDirection() {
        Vec2f inputVector =  mc.player.input.movementVector;

        float forward = inputVector.y;
        float strafe = inputVector.x;

        if (strafe > 0) {
            strafe = 1;
        } else if (strafe < 0) {
            strafe = -1;
        }

        float yaw = mc.player.getYaw();
        if (forward == 0 && strafe == 0) {
            return yaw;
        } else {
            if (forward != 0) {
                if (strafe > 0)
                    yaw += forward > 0 ? -45f : -135f;
                else if (strafe < 0)
                    yaw += forward > 0 ? 45f : 135f;
                else if (forward < 0) {
                    yaw += 180f;
                }
            }
            if (forward == 0) {
                if (strafe > 0)
                    yaw -= 90f;
                else if (strafe < 0)
                    yaw += 90f;
            }
        }

        return yaw;
    }

    public static double getJumpSpeed() {
        double jumpSpeed = 0.3999999463558197;
        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            double amplifier = mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier();
            jumpSpeed += (amplifier + 1) * 0.1;
        }
        return jumpSpeed;
    }

    public double getBoost() {
        float countableSpeed;
        int[] vectors = {-45, 45, 135, -135};
        int[] addVectors = {-90, 90, 180, -180, 0};
        int[] pitchVectors = {-45, 45};

        float lastYaw = mc.player.lastYaw;
        float lastPitch = mc.player.lastPitch;
        int minDist = findClosestVector(lastYaw, vectors);
        float maxDist = Math.abs(MathHelper.wrapDegrees(lastYaw) - vectors[minDist]);
        int addMinDist = findClosestVector(lastYaw, addVectors);
        float addMaxDist = Math.abs(MathHelper.wrapDegrees(lastYaw) - addVectors[addMinDist]);
        countableSpeed = (minDist == -1) ? 1.5f : 2.06f - maxDist * 0.56F / 45F;
        if (addMaxDist < 10) countableSpeed += 0.1f - 0.1f * addMaxDist / 10F;
        int pitchMinDist = findClosestVector(lastPitch, pitchVectors);
        float pitchMaxDist = Math.abs(Math.abs(lastPitch) - Math.abs(pitchVectors[pitchMinDist]));

        if (pitchMaxDist < 26) {
            countableSpeed = Math.max(1.94f, countableSpeed);
            countableSpeed += 0.05f - pitchMaxDist * 0.05F / 26F;
        }

        countableSpeed = Math.min(2.045f, countableSpeed);
        if (mc.player.lastPitch > -55 && mc.player.lastPitch < -19f) countableSpeed = 1.91f;
        else if (mc.player.lastPitch < -55) countableSpeed = 1.54f;
        if (mc.player.lastPitch > 19f && mc.player.lastPitch < 55) countableSpeed = 1.8f;
        else if (mc.player.lastPitch > 55) countableSpeed = 1.54f;

        return countableSpeed;
    }

    private int findClosestVector(float lastYaw, int[] vectors) {
        int index = 0;
        int minDistIndex = -1;
        float minDist = Float.MAX_VALUE;

        for (int vector : vectors) {
            float dist = Math.abs(MathHelper.wrapDegrees(lastYaw) - vector);
            if (dist < minDist) {
                minDist = dist;
                minDistIndex = index;
            }

            index++;
        }

        return minDistIndex;
    }

}
