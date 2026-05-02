package kefirdlc.dev.module.impl.movement;

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.event.impl.game.EventUpdate;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.module.setting.impl.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "HighJump", category = Category.MOVEMENT, desc = "Water bucket assisted jump boost")
public class HighJump extends Function {
    private final NumberSetting boost = new NumberSetting("Boost", this, 0.42f, 0.2f, 1.2f, 0.01f);
    private long lastUse;

    public HighJump() {
        addSettings(boost);
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (fullNullCheck()) return;
        if (!mc.player.isOnGround()) return;
        if (System.currentTimeMillis() - lastUse < 250L) return;

        int bucketSlot = findWaterBucketHotbarSlot();
        if (bucketSlot == -1) {
            setState(false);
            return;
        }

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = bucketSlot;

        BlockPos under = mc.player.getBlockPos().down();
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(under), Direction.UP, under, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);

        mc.player.jump();
        Vec3d velocity = mc.player.getVelocity();
        mc.player.setVelocity(velocity.x, Math.max(velocity.y, boost.getValueFloat()), velocity.z);

        mc.player.getInventory().selectedSlot = prevSlot;
        lastUse = System.currentTimeMillis();
        setState(false);
    }

    private int findWaterBucketHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.WATER_BUCKET)) {
                return i;
            }
        }
        return -1;
    }
}
