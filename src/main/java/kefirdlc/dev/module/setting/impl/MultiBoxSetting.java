package kefirdlc.dev.module.setting.impl;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.setting.api.Setting;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MultiBoxSetting extends Setting<Void> {

    private final List<BooleanSetting> settings;


    public MultiBoxSetting(String name, BooleanSetting... settings) {
        super(name, null, null);
        this.settings = Arrays.asList(settings);
    }

    public List<BooleanSetting> getSettings() {
        return settings;
    }



    public List<BooleanSetting> getSelectedOptions() {
        return settings.stream()
                .filter(BooleanSetting::getValue)
                .collect(Collectors.toList());
    }
    public List<BooleanSetting> getOptions() {
        return settings;
    }

    public boolean is(String name) {
        for (BooleanSetting s : settings) {
            if (s.getName().equalsIgnoreCase(name)) {
                return s.getValue();
            }
        }
        return false;
    }


    public BooleanSetting get(String name) {
        for (BooleanSetting s : settings) {
            if (s.getName().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }
}