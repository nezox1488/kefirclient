package kefirdlc.dev.event.impl.player;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.event.api.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

@AllArgsConstructor
@Getter
public class EventAttackEntity extends Event {
    private final PlayerEntity player;
    private final Entity target;
}
