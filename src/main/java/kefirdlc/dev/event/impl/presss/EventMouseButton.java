package kefirdlc.dev.event.impl.presss;
// coded by sitoku \\
// since 27.04.2026 \\


import kefirdlc.dev.event.api.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventMouseButton extends Event {
    private final int button;
    private final int action;
}
