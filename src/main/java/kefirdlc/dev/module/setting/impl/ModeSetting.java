package kefirdlc.dev.module.setting.impl;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.setting.api.Setting;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class ModeSetting extends Setting<String> {
    private final List<String> modes;
    private int index;


    public ModeSetting(String name, Function parent, String current, List<String> modes) {
        super(name, parent, current);
        this.modes = modes;
        this.index = modes.indexOf(current);
    }


    public ModeSetting(String name, Function parent, String current, String... modes) {
        this(name, parent, current, Arrays.asList(modes));
    }


    public ModeSetting(String name, String current, String... modes) {
        super(name, null, current);
        this.modes = Arrays.asList(modes);
        this.index = this.modes.indexOf(current);

        if (this.index == -1 && !this.modes.isEmpty()) {
            this.index = 0;
            this.setValue(this.modes.get(0));
        }
    }


    public boolean is(String modeName) {
        return this.getValue().equalsIgnoreCase(modeName);
    }

    public void cycle() {
        if (index < modes.size() - 1) {
            index++;
        } else {
            index = 0;
        }
        setValue(modes.get(index));
    }

    @Override
    public void setValue(String value) {
        // Проверяем, есть ли такой режим в списке
        if (modes.contains(value)) {
            this.index = modes.indexOf(value);
            super.setValue(value);
        }
    }
}