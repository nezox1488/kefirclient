package kefirdlc.dev.module.impl.combat.furry.attack;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.event.api.EventType;
import kefirdlc.dev.event.impl.game.PacketEvent;
import kefirdlc.dev.event.impl.player.UsingItemEvent;
import kefirdlc.dev.module.impl.combat.furry.Angle;
import kefirdlc.dev.module.impl.combat.furry.AngleUtil;
import kefirdlc.dev.module.impl.combat.furry.RotationController;
import kefirdlc.dev.module.impl.render.TargetESP;
import kefirdlc.dev.util.Player.*;
import kefirdlc.dev.util.math.TimerUtil;
import kefirdlc.dev.util.others.Lisener.EventListener;
import kefirdlc.dev.util.wrapper.Wrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttackHandler implements Wrapper {
    private final TimerUtil attackTimer = new TimerUtil(), shieldWatch = new TimerUtil();
    private final ClickScheduler clickScheduler = new ClickScheduler();
    private int count = 0;

    void tick() {}

    void onPacket(PacketEvent e) {
        Packet<?> packet = e.getPacket();
        if (packet instanceof HandSwingC2SPacket || packet instanceof UpdateSelectedSlotC2SPacket) {
            clickScheduler.recalculate();
        }
    }

    void onUsingItem(UsingItemEvent e) {
        if (e.getType() == EventType.START && !shieldWatch.finished(50)) {
            e.cancel();
        }
    }

    void handleAttack(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        if (canAttack(config, 1)) preAttackEntity(config);
        if (RaytracingUtil.rayTrace(config) && canAttack(config, 0)) {
            attackEntity(config);
        }
    }

    void preAttackEntity(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        if (config.isShouldUnPressShield() && mc.player.isUsingItem() && mc.player.getActiveItem().getItem().equals(Items.SHIELD)) {
            mc.interactionManager.stopUsingItem(mc.player);
            shieldWatch.reset();
        }

        if (!mc.player.isSwimming()) {
            //  AutoSprint.getInstance().tickStop = MathUtil.getRandom(1, 2);
            mc.player.setSprinting(false);
        }
    }

    void attackEntity(AttackPerpetrator.AttackPerpetratorConfigurable config) {

        attack(config);
        breakShield(config);
        attackTimer.reset();
        count++;
    }

    private void breakShield(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        LivingEntity target = config.getTarget();
        Angle angleToPlayer = AngleUtil.fromVec3d(mc.player.getBoundingBox().getCenter().subtract(target.getEyePos()));
        boolean targetOnShield = target.isUsingItem() && target.getActiveItem().getItem().equals(Items.SHIELD);
        boolean angle = Math.abs(RotationController.computeAngleDifference(target.getYaw(), angleToPlayer.getYaw())) < 90;
        Slot axe = PlayerInventoryUtil.getSlot(s -> s.getStack().getItem() instanceof AxeItem);

        if (config.isShouldBreakShield() && targetOnShield && axe != null && angle && PlayerInventoryComponent.script.isFinished()) {
            PlayerInventoryUtil.swapHand(axe, Hand.MAIN_HAND, false);
            PlayerInventoryUtil.closeScreen(true);
            attack(config);
            // PenisMain.getInstance().getNotifyManager().add(new Notify(NotifyIcons.successIcon, "Щит был успешно сломан", target.getNameForScoreboard(), 1000, Color.WHITE, new Color(255, 255, 255)));

            PlayerInventoryUtil.swapHand(axe, Hand.MAIN_HAND, false, true);
            PlayerInventoryUtil.closeScreen(true);
        }
    }

    private void attack(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        mc.player.setSprinting(false);
        mc.interactionManager.attackEntity(mc.player, config.getTarget());
        TargetESP.notifyHit();
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean isSprinting() {
        return EventListener.serverSprint && !mc.player.isGliding() && !mc.player.isTouchingWater();
    }

    public boolean canAttack(AttackPerpetrator.AttackPerpetratorConfigurable config, int ticks) {
        if (config == null || config.getTarget() == null || mc.player == null) {
            return false;
        }
        for (int i = 0;i <= ticks;i++) {
            if (canCrit(config, i)) {
                return true;
            }
        }
        return false;
    }

    public boolean canCrit(AttackPerpetrator.AttackPerpetratorConfigurable config, int ticks) {
        if (mc.player.isUsingItem() && !mc.player.getActiveItem().getItem().equals(Items.SHIELD) && config.isEatAndAttack()) {
            return false;
        }


        boolean isMace = mc.player.getMainHandStack().getItem() == Items.MACE;

        double heightDifference = mc.player.getY() - config.getTarget().getY();


        boolean isMaceSmash = isMace && heightDifference >= 2.0;


        if (!isMaceSmash && !clickScheduler.isCooldownComplete(config.isUseDynamicCooldown(), ticks, config.isSyncTps(), config.isSyncPing())) {
            return false;
        }


        SimulatedPlayer simulated = SimulatedPlayer.simulateLocalPlayer(ticks);
        if (config.isOnlyCritical() && !hasMovementRestrictions(simulated)) {
            return isPlayerInCriticalState(config, simulated, ticks);
        }

        return true;
    }

    private boolean hasMovementRestrictions(SimulatedPlayer simulated) {
        return simulated.hasStatusEffect(StatusEffects.BLINDNESS)
                || simulated.hasStatusEffect(StatusEffects.LEVITATION)
                || PlayerIntersectionUtil.isBoxInBlock(simulated.boundingBox.expand(-1e-3), Blocks.COBWEB)
                || simulated.isSubmergedInWater()
                || simulated.isInLava()
                || simulated.isClimbing()
                || !PlayerIntersectionUtil.canChangeIntoPose(EntityPose.STANDING, simulated.pos)
                || simulated.player.getAbilities().flying;
    }

    private boolean isPlayerInCriticalState(AttackPerpetrator.AttackPerpetratorConfigurable config, SimulatedPlayer simulated, int ticks) {
        boolean fall = simulated.fallDistance > 0 && (simulated.fallDistance < 0.08 || !SimulatedPlayer.simulateLocalPlayer(ticks + 1).onGround);
        boolean knockbackWindow = simulated.player.hurtTime > 0 && simulated.velocity.y > 0.02;
        if (config.isKbCritical() && knockbackWindow && !simulated.onGround) {
            return true;
        }
        return !simulated.onGround && fall;
    }
}
