package kefirdlc.dev.module.setting.impl;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.setting.api.Setting;
import lombok.Getter;

@Getter
public class StringSetting extends Setting<String> {

    public StringSetting(String name, Function parent, String defaultValue) {
        super(name, parent, defaultValue);
    }

    public StringSetting(String name, String defaultValue) {
        super(name, null, defaultValue);
    }
}