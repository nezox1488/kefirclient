package kefirdlc.dev.event.impl.render;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.event.api.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TextFactoryEvent extends Event {
    String text;

    public void replaceText(String protect, String replaced) {
        if (text == null || text.isEmpty()) return;

        if (text.contains(protect)) {
            if (text.equalsIgnoreCase(protect) || text.contains(protect + " ") || text.contains(" " + protect) || text.contains("⏏" + protect) || text.contains(protect + "§")) {
                text = text.replace(protect, replaced);
            }
        }
    }
}
