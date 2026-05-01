package kefirdlc.dev.module.impl.combat;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.module.setting.impl.BooleanSetting;
import kefirdlc.dev.module.setting.impl.ModeSetting;
import kefirdlc.dev.module.setting.impl.NumberSetting;

@ModuleInfo(name = "sss", category = Category.COMBAT, desc = "бежать бежать бежать бежать бежать бежать сасать бежать бежать бежать бежать бежать бежать бежать бежать бежать бежать")
public class sss  extends Function {

    private final BooleanSetting rotate = new BooleanSetting("Rotate", this, true);
    private final NumberSetting range = new NumberSetting("Range", null, 3.0, 1.0, 6.0, 0.1);
    private final ModeSetting mode = new ModeSetting("Mode", this, "Switch", "Switch", "Single", "Multi");

    public sss() {


        addSettings(rotate, range, mode);
    }
}