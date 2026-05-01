package kefirdlc.dev.module.impl.combat.furry.attack;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.impl.combat.furry.AngleUtil;
import kefirdlc.dev.module.impl.combat.furry.RotationController;
import kefirdlc.dev.module.impl.combat.furry.modes.LinearSmoothMode;
import kefirdlc.dev.util.Player.RaytracingUtil;
import kefirdlc.dev.util.others.Friends;
import kefirdlc.dev.util.wrapper.Wrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TargetSelector implements Wrapper {
    final PointFinder pointFinder = new PointFinder();
    LivingEntity currentTarget;
    Stream<LivingEntity> potentialTargets;

    public TargetSelector() {
        this.currentTarget = null;
    }

    public void lockTarget(LivingEntity target) {
        if (this.currentTarget == null) {
            this.currentTarget = target;
        }
    }

    public void releaseTarget() {
        this.currentTarget = null;
    }

    public void validateTarget(Predicate<LivingEntity> predicate) {
        findFirstMatch(predicate).ifPresent(this::lockTarget);

        if (this.currentTarget != null && !predicate.test(this.currentTarget)) {
            releaseTarget();
        }
    }

    public void searchTargets(Iterable<Entity> entities, float maxDistance, float maxFov, boolean ignoreWalls) {
        if (currentTarget != null && (!pointFinder.hasValidPoint(currentTarget, maxDistance, ignoreWalls) || getFov(currentTarget, maxDistance, ignoreWalls) > maxFov)) {
            releaseTarget();
        }

        this.potentialTargets = createStreamFromEntities(entities, maxDistance, maxFov, ignoreWalls);
    }

    private double getFov(LivingEntity entity, float maxDistance, boolean ignoreWalls) {
        Vec3d attackVector = pointFinder.computeVector(entity, maxDistance, RotationController.INSTANCE.getRotation(), new LinearSmoothMode().randomValue(), ignoreWalls).getLeft();
        return RaytracingUtil.rayTrace(maxDistance, entity.getBoundingBox()) ? 0 : RotationController.computeRotationDifference(AngleUtil.cameraAngle(), AngleUtil.calculateAngle(attackVector));
    }

    private Stream<LivingEntity> createStreamFromEntities(Iterable<Entity> entities, float maxDistance, float maxFov, boolean ignoreWalls) {
        return StreamSupport.stream(entities.spliterator(), false)
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(entity -> pointFinder.hasValidPoint(entity, maxDistance, ignoreWalls) && getFov(entity, maxDistance, ignoreWalls) < maxFov)
                .sorted(Comparator.comparingDouble(entity -> entity.distanceTo(mc.player)));
    }

    private Optional<LivingEntity> findFirstMatch(Predicate<LivingEntity> predicate) {
        return this.potentialTargets.filter(predicate).findFirst();
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class EntityFilter {
        List<String> targetSettings;

        public EntityFilter(List<String> targetSettings) {
            this.targetSettings = targetSettings;
        }

        public boolean isValid(LivingEntity entity) {
            if (isLocalPlayer(entity)) return false;
            if (isInvalidHealth(entity)) return false;
          //  if (isBotPlayer(entity)) return false;
            return isValidEntityType(entity);
        }

        private boolean isLocalPlayer(LivingEntity entity) {
            return entity == mc.player;
        }

        private boolean isInvalidHealth(LivingEntity entity) {
            return !entity.isAlive() || entity.getHealth() <= 0;
        }

//        private boolean isBotPlayer(LivingEntity entity) {
//            return entity instanceof PlayerEntity player /*&& ServerUtil.isBot(player)*/;
//        }

        private boolean isNakedPlayer(LivingEntity entity) {
            return entity.isPlayer();
        }

        private boolean isValidEntityType(LivingEntity entity) {
            return switch (entity) {
                case PlayerEntity player -> {
                    if (!targetSettings.contains("Players")) {
                        yield false;
                    }
                    boolean isFriend = Friends.isFriend(player.getName().getString());
                    if (isFriend && !targetSettings.contains("Friends")) {
                        yield false;
                    }
                    yield true;
                }
                case AnimalEntity animal -> targetSettings.contains("Animals");
                case MobEntity mob -> targetSettings.contains("Mobs");
                default -> false;
            };
        }
    }
}