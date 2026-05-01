package kefirdlc.dev.event.impl.game;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.event.api.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.Vec3d;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MoveEvent extends Event {
    Vec3d movement;
}