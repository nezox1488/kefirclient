package kefirdlc.dev.module.impl.combat.furry.attack;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.event.impl.game.PacketEvent;
import kefirdlc.dev.event.impl.player.UsingItemEvent;
import kefirdlc.dev.module.impl.combat.furry.Angle;
import kefirdlc.dev.module.setting.impl.ModeSetting;
import kefirdlc.dev.util.wrapper.Wrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;

import java.util.List;

@Getter
public class AttackPerpetrator implements Wrapper {
    AttackHandler attackHandler = new AttackHandler();

    public void tick() {
        attackHandler.tick();
    }

    public void onPacket(PacketEvent e) {
        attackHandler.onPacket(e);
    }

    public void onUsingItem(UsingItemEvent e) {
        attackHandler.onUsingItem(e);
    }

    public void performAttack(AttackPerpetratorConfigurable configurable) {
        attackHandler.handleAttack(configurable);
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class AttackPerpetratorConfigurable {
        LivingEntity target;
        Angle angle;
        float maximumRange;
        boolean onlyCritical, shouldBreakShield, shouldUnPressShield, useDynamicCooldown, eatAndAttack;
        Box box;
        ModeSetting aimMode;

        public AttackPerpetratorConfigurable(LivingEntity target, Angle angle, float maximumRange, List<String> options, ModeSetting aimMode, Box box) {
            this.target = target;
            this.angle = angle;
            this.maximumRange = maximumRange;
            this.onlyCritical = options.contains("Only Critical");
            this.shouldBreakShield = options.contains("Break Shield");
            this.shouldUnPressShield = options.contains("UnPress Shield");
            this.useDynamicCooldown = options.contains("Dynamic Cooldown");
            this.eatAndAttack = options.contains("No Attack When Eat");
            this.box = box;
            this.aimMode = aimMode;
        }
    }
}