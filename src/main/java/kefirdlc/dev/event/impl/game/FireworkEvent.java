package kefirdlc.dev.event.impl.game;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.event.api.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.Vec3d;

@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FireworkEvent extends Event {
    public Vec3d vector;

}
