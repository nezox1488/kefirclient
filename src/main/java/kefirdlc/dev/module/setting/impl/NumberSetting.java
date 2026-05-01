package kefirdlc.dev.module.setting.impl;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.setting.api.Setting;
import lombok.Getter;

@Getter
public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double increment;

    public NumberSetting(String name, Function parent, double defaultValue, double min, double max, double increment) {
        super(name, parent, defaultValue);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }
    public int getValueInt() {
        return getValue().intValue();
    }

    public float getValueFloat() {
        return getValue().floatValue();
    }

    public long getValueLong() {
        return getValue().longValue();
    }

    public void setValueNumber(double value) {
        double precision = 1.0 / increment;
        setValue(Math.round(Math.max(min, Math.min(max, value)) * precision) / precision);
    }
}