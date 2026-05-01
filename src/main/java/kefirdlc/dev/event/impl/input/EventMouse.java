package kefirdlc.dev.event.impl.input;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.event.api.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventMouse extends Event {
    private final double mouseX;
    private final double mouseY;
    private final int button;
    private final int action;
}