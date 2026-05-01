package kefirdlc.dev.module.setting.api;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.api.Function;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class SettingManager {
    private final List<Setting<?>> settings = new ArrayList<>();

    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(Arrays.asList(settings));
    }

    public List<Setting<?>> getSettingsForModule(Function module) {
        return settings.stream()
                .filter(setting -> setting.getParent() == module)
                .filter(Setting::isVisible)
                .collect(Collectors.toList());
    }

    public void addSetting(Setting<?> setting) {
        settings.add(setting);
    }
}