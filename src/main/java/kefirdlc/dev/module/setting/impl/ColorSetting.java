package kefirdlc.dev.module.setting.impl;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.setting.api.Setting;
import lombok.Getter;
import java.awt.Color;

@Getter
public class ColorSetting extends Setting<Color> {
    private float[] hsb = new float[3];

    public ColorSetting(String name, Function parent, Color defaultValue) {
        super(name, parent, defaultValue);
        Color.RGBtoHSB(defaultValue.getRed(), defaultValue.getGreen(), defaultValue.getBlue(), hsb);
    }

    public void setColor(Color c) {
        setValue(c);
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
    }



    public float[] getHsb() {
        return hsb;
    }

    public void setHue(float hue) {
        hsb[0] = hue;
        updateColor();
    }

    public void setSaturation(float sat) {
        hsb[1] = sat;
        updateColor();
    }

    public void setBrightness(float bri) {
        hsb[2] = bri;
        updateColor();
    }

    private void updateColor() {

        int alpha = getValue().getAlpha();
        Color c = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);

        setValue(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
    }

    @Override
    public void setValue(Color value) {
        super.setValue(value);
         float[] newHsb = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
        hsb[1] = newHsb[1];
        hsb[2] = newHsb[2];
         if (newHsb[1] > 0.01f) {
            hsb[0] = newHsb[0];
        }
    }


}