package kefirdlc.dev.module.impl.combat;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.event.api.EventType;
import kefirdlc.dev.event.impl.player.CameraEvent;
import kefirdlc.dev.event.impl.player.RotationUpdateEvent;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.module.impl.combat.furry.*;
import kefirdlc.dev.module.impl.combat.furry.modes.*;
import kefirdlc.dev.module.setting.impl.BooleanSetting;
import kefirdlc.dev.module.setting.impl.ModeSetting;
import kefirdlc.dev.module.setting.impl.MultiBoxSetting;
import kefirdlc.dev.module.setting.impl.NumberSetting;
import kefirdlc.dev.module.impl.combat.furry.attack.AttackHandler;
import kefirdlc.dev.module.impl.combat.furry.attack.AttackPerpetrator;
import kefirdlc.dev.module.impl.combat.furry.attack.PointFinder;
import kefirdlc.dev.module.impl.combat.furry.attack.TargetSelector;
import kefirdlc.dev.util.Script.TaskPriority;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.stream.Collectors;

@ModuleInfo(name = "AttackAura", category = Category.COMBAT, desc = "Пенить Пенить")
public class AttackAura  extends Function {

    public NumberSetting distance = new NumberSetting("Дистанция", this ,3.33f,1,6,0.1f);

    public MultiBoxSetting targetType = new MultiBoxSetting("Опции",
            new BooleanSetting("Players", true),
            new BooleanSetting("Mobs", false),
            new BooleanSetting("Animals", false),
            new BooleanSetting("Friends", false));



    public MultiBoxSetting attackSetting = new MultiBoxSetting("Опции",
            new BooleanSetting("Allows you to customize the attack", true),
            new BooleanSetting("Only Critical", true),
            new BooleanSetting("Dynamic Cooldown", false),
            new BooleanSetting("Break Shield", true),
            new BooleanSetting("UnPress Shield", true),
            new BooleanSetting("No Attack When Eat", true),
            new BooleanSetting("Ignore The Walls", false));

    public static ModeSetting correctionType = new ModeSetting("Correction Type", "Free","Free" ,"Focused");


    public static ModeSetting aimMode = new ModeSetting("Rotation Type","Snap" ,"FunTime", "Snap", "Matrix" ,"FunTime 2","Sloth");

    private long lastAttackTime = 0;

    private double prevPosY;
    private boolean canCritical;
    private long lastEspAttackAnimTime = 0;
    private int prevSwordSlot = -1;

    public AttackAura() {
        addSettings(distance,targetType, attackSetting,correctionType,aimMode);
    }

    public LivingEntity target, lastTarget;

    TargetSelector targetSelector = new TargetSelector();

    public LivingEntity getTarget() {
        return target;
    }

    Float maxDistance = distance.getValueFloat();
    PointFinder pointFinder = new PointFinder();
    @Override
    public void onDisable() {
        targetSelector.releaseTarget();
        target = null;
        super.onDisable();
    }


    @Subscribe
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (mc.player == null || mc.world == null) {
            target = null;
            return;
        }

        updateCriticalState();
        switch (e.getType()) {
            case EventType.PRE -> {
                target = updateTarget();
                if (target != null) {
                    AttackPerpetrator.AttackPerpetratorConfigurable config = getConfig();
                    if (config != null) {
                        rotateToTarget(config);
                        lastTarget = target;
                    }
                }
            }
            case EventType.POST -> {
                if (target != null) {
                    AttackPerpetrator.AttackPerpetratorConfigurable config = getConfig();
                    if (config != null) {
                        KefirDLC.getInstance().getAttackPerpetrator().performAttack(config);
                    }
                }
            }
        }
    }

    @Subscribe
    public void onCamera(CameraEvent e) {
        if (target != null && !correctionType.is("Free")) {
            AttackPerpetrator.AttackPerpetratorConfigurable config = getConfig();
            if (config != null) {
                e.setAngle(config.getAngle());
            }
        }
    }



    private LivingEntity updateTarget() {
        List<String> selectedNames = targetType.getSelectedOptions().stream()
                .map(BooleanSetting::getName)
                .collect(Collectors.toList());

        TargetSelector.EntityFilter filter = new TargetSelector.EntityFilter(selectedNames);
        assert mc.world != null;
        if (mc.player.isGliding()) {
            targetSelector.searchTargets(mc.world.getEntities(), maxDistance * 30 , 360, attackSetting.is("Ignore The Walls"));
        } else {
            targetSelector.searchTargets(mc.world.getEntities(), maxDistance, 360, attackSetting.is("Ignore The Walls"));
        }

        targetSelector.validateTarget(filter::isValid);
        return targetSelector.getCurrentTarget();
    }



    private void rotateToTarget(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        AttackHandler attackHandler = KefirDLC.getInstance().getAttackPerpetrator().getAttackHandler();
        RotationController controller = RotationController.INSTANCE;


        Angle targetAngle = config.getAngle();
        Angle finalAngle;



        if (mc.player.getAttackCooldownProgress(0.9f) >= 0.9f) {
            finalAngle = targetAngle;

            // https://media.discordapp.net/attachments/1201306605799608421/1449798170908295330/lonygriefaura.gif?ex=694b6a03&is=694a1883&hm=5a903e54a2e6312e5fecee70f13a1883cb6da87443ece1ca2c1e412b582ef4fb&=https://media.discordapp.net/attachments/1201306605799608421/1449798170908295330/lonygriefaura.gif?ex=694b6a03&is=694a1883&hm=5a903e54a2e6312e5fecee70f13a1883cb6da87443ece1ca2c1e412b582ef4fb&=
            // https://media.discordapp.net/attachments/1423943180717527040/1424286051760996424/E63BFFCA-1CFB-490A-AB8A-4033D0B30225.gif?ex=694b8bc0&is=694a3a40&hm=4a880fe1b567d865aa1ec2c39a7268842b520fce7e12cdc32498adab3d99b23d&=
            //тут должен быть байпас слотха но гифки выше но увы

        } else {
            finalAngle = targetAngle;
        }


        Angle.VecRotation rotation = new Angle.VecRotation(finalAngle, finalAngle.toVector());

        RotationConfig rotationConfig = getRotationConfig();

        switch (aimMode.getValue()) {
            case "Snap" -> {
                if (attackHandler.canAttack(config, 2) ) {
                    controller.clear();
                    controller.rotateTo(rotation, target, 3, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                }
            }
            case "Sloth" -> {

                controller.clear();
                controller.rotateTo(rotation, target, 3, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);

            }
            case "FunTime" -> {
                if (attackHandler.canAttack(config, 4)) {
                    controller.clear();
                    controller.rotateTo(rotation, target, 40, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                }
            }
            case "FunTime 2" -> {
                controller.clear();
                controller.rotateTo(rotation, target, 40, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
            }
            case "Matrix" -> controller.rotateTo(rotation, target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
        }
    }

    public AttackPerpetrator.AttackPerpetratorConfigurable getConfig() {
        if (target == null || mc.player == null || mc.world == null) {
            return null;
        }

        boolean ignoreWalls = attackSetting.is("Ignore The Walls");

        Pair<Vec3d, Box> point = pointFinder.computeVector(
                target,
                maxDistance ,
                RotationController.INSTANCE.getRotation(),
                getSmoothMode().randomValue(),
                ignoreWalls
        );
        if (point == null || point.getLeft() == null || point.getRight() == null) {
            return null;
        }

        Angle angle = AngleUtil.fromVec3d(point.getLeft().subtract(mc.player.getEyePos()));
        Box box = point.getRight();

        List<String> selectedOptionNames = attackSetting.getSelectedOptions().stream()
                .map(BooleanSetting::getName)
                .collect(Collectors.toList());

        return new AttackPerpetrator.AttackPerpetratorConfigurable(
                target,
                angle,
                maxDistance,
                selectedOptionNames,
                aimMode,
                box
        );
    }

    public RotationConfig getRotationConfig() {
        boolean isFree = correctionType.is("Free");
        return new RotationConfig(getSmoothMode(), !isFree, isFree);
    }


    public AngleSmoothMode getSmoothMode() {
        return switch (aimMode.getValue()) {
            case "FunTime" -> new FunTimeSmoothMode();
            case "Matrix" -> new MatrixSmoothMode();
            case "Snap" -> new SnapSmoothMode();
            case "Sloth" -> new SnapSmoothMode();
            case "FunTime 2" -> new FunTimeMode();
            default -> new LinearSmoothMode();
        };
    }

    private void updateCriticalState() {
        if ( fullNullCheck()) return ;
        double currentPosY = mc.player.getY();
        boolean onGround = mc.player.isOnGround();

        canCritical = !onGround && currentPosY < prevPosY;
        prevPosY = currentPosY;
    }

    private boolean shouldAttack() {

        long currentTime = System.currentTimeMillis();
        boolean isReady = (currentTime - lastAttackTime) >= 220  && mc.player.getAttackCooldownProgress(1.5F) >= 1.0F;

        if (!isReady) {
            return false;
        }


//        if (boostBind.isPressed()) {
//            applyTargetBoost(target);
//        }

        if (attackSetting.is("Only Critical")) {
            if (!shouldCritical()) {
                return false;
            }

            return canCritical;
        }

        return true;
    }

//    private void applyTargetBoost(LivingEntity target) {
//        if (target == null) return;
//
//        Vec3d playerPos = mc.player.getEntityPos();
//        Vec3d targetPos = target.getEntityPos();
//        Vec3d direction = targetPos.subtract(playerPos).normalize();
//
//        double speed = boostSpeed.getDoubleValue() * 0.02;
//
//
//
//
//        mc.player.addVelocity(
//                direction.x * speed,
//                0,
//                direction.z * speed
//        );
//
//    }

    private boolean shouldCritical() {

        boolean isDeBuffed = mc.player.hasStatusEffect(StatusEffects.LEVITATION) || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING) || mc.player.hasStatusEffect(StatusEffects.BLINDNESS);
        boolean isInLiquid = mc.player.isSubmergedInWater() || mc.player.isInLava();
        boolean isFlying = mc.player.getAbilities().flying || mc.player.isGliding();
        boolean isClimbing = mc.player.isClimbing();
        boolean isCantJump = mc.player.hasVehicle();

        return !(isDeBuffed || isInLiquid || isFlying || isClimbing || isCantJump);
    }




}
