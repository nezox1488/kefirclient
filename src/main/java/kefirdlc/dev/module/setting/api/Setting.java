package kefirdlc.dev.module.setting.api;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.api.Function;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

@Getter
@Setter
public abstract class Setting<T> {
    private final String name;
    private Supplier<String> desc;
    private final Function parent;
    private T value;
    private Supplier<Boolean> visible = () -> true;

    public Setting(String name, Function parent, T defaultValue) {
        this.name = name;
        this.parent = parent;
        this.value = defaultValue;
    }

    public Setting<T> setDesc(Supplier<String> description) {
        this.desc = description;
        return this;
    }

    public Setting<T> setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
        return this;
    }

    public boolean isVisible() {
        return visible.get();
    }
}