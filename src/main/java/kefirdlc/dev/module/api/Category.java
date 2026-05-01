package kefirdlc.dev.module.api;
// coded by sitoku \\
// since 27.04.2026 \\

import lombok.Getter;

public enum Category {
    COMBAT("Combat", "B"),
    MOVEMENT("Movement","C"),
    RENDER("Render","b"),
    PLAYER("Player","E"),
    MISC("Misc","D"),
    THEME("Themes","G"),
    SCRIPT("Script","R"),
    CONFIG("Config","j");

    @Getter
    private final String name;
    @Getter
    private final String icon;
    Category(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }
}
