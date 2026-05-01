package kefirdlc.dev.module.impl.combat.furry.attack;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.util.others.ServerUtil;
import kefirdlc.dev.util.wrapper.Wrapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClickScheduler implements Wrapper {
    private final int[] funTimeTicks = new int[]{10, 11, 10, 13}, spookyTicks = new int[]{11, 10, 13, 10, 12, 11, 12}, defaultTicks = new int[]{10, 11};
    long lastClickTime = System.currentTimeMillis();

    public boolean isCooldownComplete(boolean dynamicCooldown, int ticks, boolean syncTps, boolean syncPing) {
        boolean dynamic = hasTicksElapsedSinceLastClick(tickCount() - ticks, syncTps, syncPing) || !dynamicCooldown;
        return dynamic && mc.player.getAttackCooldownProgress(ticks) > 0.9F;
    }

    public boolean hasTicksElapsedSinceLastClick(int ticks, boolean syncTps, boolean syncPing) {
        float tpsScale = syncTps ? (20F / Math.max(ServerUtil.TPS, 1F)) : 1F;
        float pingScale = 1F;
        if (syncPing && mc.getNetworkHandler() != null && mc.player != null) {
            var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) {
                pingScale += Math.min(entry.getLatency(), 300) / 1000F;
            }
        }
        return lastClickPassed() >= (long) (ticks * 50L * tpsScale * pingScale);
    }

    public long lastClickPassed() {
        return System.currentTimeMillis() - lastClickTime;
    }

    public void recalculate() {
        lastClickTime = System.currentTimeMillis();
    }

    int tickCount() {
        int count = KefirDLC.getInstance().getAttackPerpetrator().getAttackHandler().getCount();
        return switch (ServerUtil.server) {
            case "FunTime" -> funTimeTicks[count % funTimeTicks.length];
            case "SpookyTime" -> spookyTicks[count % spookyTicks.length];
            default -> defaultTicks[count % defaultTicks.length];
        };
    }
}