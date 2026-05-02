package kefirdlc.dev.module.impl.movement;
// coded by sitoku \\
// since 01.05.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.event.impl.game.EventUpdate;
import kefirdlc.dev.event.impl.game.MoveEvent;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.module.setting.impl.ModeSetting;
import kefirdlc.dev.module.setting.impl.NumberSetting;
import kefirdlc.dev.util.Player.MobilityHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Speed", category = Category.MOVEMENT, desc = "Скорость движения")
public class Speed extends Function {

    private final ModeSetting mode = new ModeSetting("Mode", this, "HollyWorld", "HollyWorld", "CeilingHop");
    private final NumberSetting boostStrength = new NumberSetting("Boost Strength", this, 0.22, 0.0, 1.5, 0.01);
    private final NumberSetting collideMultiplier = new NumberSetting("Collide Multiplier", this, 0.45, 0.0, 1.0, 0.01);
    private final NumberSetting miniJumpVelocity = new NumberSetting("MiniJump Velocity", this, 0.18, 0.08, 0.42, 0.01);
    private final NumberSetting headBoost = new NumberSetting("HeadBoost", this, 1.12, 1.0, 1.6, 0.01);

    public Speed() {
        addSettings(mode, boostStrength, collideMultiplier, miniJumpVelocity, headBoost);
    }

    @Subscribe
    public void onMove(MoveEvent e) {
        if (!mode.is("HollyWorld")) return;
        speedHollyWorld(e);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (!mode.is("CeilingHop")) return;
        speedCeilingHop();
    }

    private void speedHollyWorld(MoveEvent e) {
        if (mc.player == null || mc.world == null) return;
        if (!MobilityHandler.hasPlayerMovement()) return;

        boolean collide = mc.player.horizontalCollision || mc.player.isTouchingWater() || mc.player.isSneaking();

        Box near = mc.player.getBoundingBox().expand(0.3, 0.15, 0.3);
        int playersNearby = mc.world.getEntitiesByClass(
                PlayerEntity.class,
                near,
                entity -> entity != mc.player && entity.isAlive()
        ).size();
        if (playersNearby <= 0) return;

        double boost = Math.max(0.0, boostStrength.getValue());
        double[] dir = MobilityHandler.calculateDirection(boost);

        Vec3d m = e.getMovement();
        if (m == null) m = Vec3d.ZERO;

        if (!collide) {
            e.setMovement(new Vec3d(m.x + dir[0], m.y, m.z + dir[1]));
        } else {
            double mul = Math.max(0.0, collideMultiplier.getValue());
            e.setMovement(new Vec3d(m.x + dir[0] * mul, m.y, m.z + dir[1] * mul));
        }
    }

    private void speedCeilingHop() {
        if (mc.player == null || mc.world == null) return;
        if (!MobilityHandler.hasPlayerMovement()) return;
        if (mc.player.isTouchingWater() || mc.player.isSneaking() || mc.player.isClimbing()) return;

        mc.player.jumpingCooldown = 0;

        double miniJump = miniJumpVelocity.getValue();

        // Replace the vanilla jump arc with a forced mini-hop.
        if (mc.player.getVelocity().y > miniJump) {
            Vec3d velocity = mc.player.getVelocity();
            mc.player.setVelocity(velocity.x, miniJump, velocity.z);
        }

        if (mc.player.isOnGround()) {
            Vec3d velocity = mc.player.getVelocity();
            mc.player.setVelocity(velocity.x, miniJump, velocity.z);
        }

        // Apply horizontal acceleration while ascending from mini-hop.
        if (mc.player.getVelocity().y > 0.0) {
            Vec3d velocity = mc.player.getVelocity();
            double multiplier = headBoost.getValue();
            mc.player.setVelocity(velocity.x * multiplier, velocity.y, velocity.z * multiplier);
        }
    }
}
