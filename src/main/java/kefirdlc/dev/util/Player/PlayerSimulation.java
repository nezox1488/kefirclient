package kefirdlc.dev.util.Player;
// coded by sitoku \\
// since 27.04.2026 \\


import net.minecraft.util.math.Vec3d;

public interface PlayerSimulation {
    Vec3d pos();

    void tick();
}