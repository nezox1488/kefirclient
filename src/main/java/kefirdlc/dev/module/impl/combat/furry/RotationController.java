package kefirdlc.dev.module.impl.combat.furry;
// coded by sitoku \\
// since 27.04.2026 \\


import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.event.api.EventType;
import kefirdlc.dev.event.impl.game.EventUpdate;
import kefirdlc.dev.event.impl.game.PacketEvent;
import kefirdlc.dev.event.impl.player.PlayerVelocityStrafeEvent;
import kefirdlc.dev.event.impl.player.RotationUpdateEvent;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.util.Script.TaskPriority;
import kefirdlc.dev.util.Script.TaskProcessor;
import kefirdlc.dev.util.wrapper.Wrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RotationController implements Wrapper {
    public static RotationController INSTANCE = new RotationController();

    RotationPlan lastRotationPlan;
    final TaskProcessor<RotationPlan> rotationPlanTaskProcessor = new TaskProcessor<>();
    Angle currentAngle, previousAngle, serverAngle = Angle.DEFAULT;

    public RotationController() {
        KefirDLC.getInstance().getEventBus().register(this);
    }

    public void setRotation(Angle value) {
        if (value == null) {
            this.previousAngle = this.currentAngle != null ? this.currentAngle : AngleUtil.cameraAngle();
        } else {
            this.previousAngle = this.currentAngle;
        }
        this.currentAngle = value;
    }

    public Angle getCurrentAngle() {
        return currentAngle;
    }

    public Angle getRotation() {
        return currentAngle != null ? currentAngle : AngleUtil.cameraAngle();
    }

    public Angle getPreviousRotation() {
        return currentAngle != null && previousAngle != null ? previousAngle : new Angle(mc.player.lastYaw, mc.player.lastPitch);
    }

    public Angle getMoveRotation() {
        RotationPlan rotationPlan = getCurrentRotationPlan();
        return currentAngle != null && rotationPlan != null && rotationPlan.isMoveCorrection() ? currentAngle : AngleUtil.cameraAngle();
    }

    public RotationPlan getCurrentRotationPlan() {
        return rotationPlanTaskProcessor.fetchActiveTaskValue() != null ? rotationPlanTaskProcessor.fetchActiveTaskValue() : lastRotationPlan;
    }

    public void rotateTo(Angle.VecRotation vecRotation, LivingEntity entity, int reset, RotationConfig configurable, TaskPriority taskPriority, Function provider) {
        rotateTo(configurable.createRotationPlan(vecRotation.getAngle(), vecRotation.getVec(), entity, reset), taskPriority, provider);
    }

    public void rotateTo(Angle angle, int reset, RotationConfig configurable, TaskPriority taskPriority, Function provider) {
        rotateTo(configurable.createRotationPlan(angle,angle.toVector(),null, reset), taskPriority, provider);
    }

    public void rotateTo(Angle angle, RotationConfig configurable, TaskPriority taskPriority, Function provider) {
        rotateTo(configurable.createRotationPlan(angle,angle.toVector(),null,1), taskPriority, provider);
    }

    public void rotateTo(RotationPlan plan, TaskPriority taskPriority, Function provider) {
        rotationPlanTaskProcessor.addTask(new TaskProcessor.Task<>(1, taskPriority.getPriority(), provider, plan));
    }

    public void update() {
        RotationPlan activePlan = getCurrentRotationPlan();
        if (activePlan == null) return;
        if (mc.player == null || mc.world == null) return;
        Angle clientAngle = AngleUtil.cameraAngle();
        if (lastRotationPlan != null) {
            double differenceFromCurrentToPlayer = computeRotationDifference(serverAngle, clientAngle);
            if (activePlan.getTicksUntilReset() <= rotationPlanTaskProcessor.tickCounter && differenceFromCurrentToPlayer < activePlan.getResetThreshold()) {
                setRotation(null);
                lastRotationPlan = null;
                rotationPlanTaskProcessor.tickCounter = 0;
                return;
            }
        }

        Angle newAngle = activePlan.nextRotation(currentAngle != null ? currentAngle : clientAngle, rotationPlanTaskProcessor.fetchActiveTaskValue() == null).adjustSensitivity();
        setRotation(newAngle);
        lastRotationPlan = activePlan;
        rotationPlanTaskProcessor.tick(1);
    }

    public static double computeRotationDifference(Angle a, Angle b) {
        return Math.hypot(Math.abs(computeAngleDifference(a.getYaw(), b.getYaw())), Math.abs(a.getPitch() - b.getPitch()));
    }

    public static float computeAngleDifference(float a, float b) {
        return MathHelper.wrapDegrees(a - b);
    }

    private Vec3d fixVelocity(Vec3d currVelocity, Vec3d movementInput, float speed) {
        if (currentAngle != null) {
            float yaw = currentAngle.getYaw();
            double d = movementInput.lengthSquared();

            if (d < 1.0E-7) {
                return Vec3d.ZERO;
            } else {
                Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);

                float f = MathHelper.sin(yaw * 0.017453292f);
                float g = MathHelper.cos(yaw * 0.017453292f);

                return new Vec3d(vec3d.getX() * g - vec3d.getZ() * f, vec3d.getY(), vec3d.getZ() * g + vec3d.getX() * f);
            }
        }
        return currVelocity;
    }

    public void clear() {
        rotationPlanTaskProcessor.activeTasks.clear();
    }

    @Subscribe
    public void onPlayerVelocityStrafe(PlayerVelocityStrafeEvent e) {
        RotationPlan currentRotationPlan = getCurrentRotationPlan();
        if (currentRotationPlan != null && currentRotationPlan.isMoveCorrection()) {
            e.setVelocity(fixVelocity(e.getVelocity(), e.getMovementInput(), e.getSpeed()));
        }
    }

    @Subscribe
    public void onTick(EventUpdate e) {


        KefirDLC.getInstance().getEventBus().post(new RotationUpdateEvent(EventType.PRE));
        update();
        KefirDLC.getInstance().getEventBus().post(new RotationUpdateEvent(EventType.POST));
    }

    @Subscribe
    public void onPacket(PacketEvent event) {
        if (!event.isCanceled()) switch (event.getPacket()) {
            case PlayerMoveC2SPacket player when player.changesLook() -> serverAngle = new Angle(player.getYaw(1), player.getPitch(1));
            case PlayerPositionLookS2CPacket player -> serverAngle = new Angle(player.change().yaw(), player.change().pitch());
            default -> {}
        }
    }
}

