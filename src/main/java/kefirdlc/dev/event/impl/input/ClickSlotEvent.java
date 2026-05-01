package kefirdlc.dev.event.impl.input;
// coded by sitoku \\
// since 27.04.2026 \\


import kefirdlc.dev.event.api.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.screen.slot.SlotActionType;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClickSlotEvent extends Event {
    int windowId, slotId, button;
    SlotActionType actionType;
}

