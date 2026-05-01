package kefirdlc.dev.module.impl.movement;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.event.impl.game.EventUpdate;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;

@ModuleInfo(name = "AutoSprint", category = Category.MOVEMENT, desc = "бежать бежать бежать бежать бежать бежать сасать бежать бежать бежать бежать бежать бежать бежать бежать бежать бежать")
public class AutoSprint extends Function {

    public AutoSprint() {

    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (fullNullCheck()) return;
        if (mc.options.forwardKey.isPressed()) {
            mc.options.sprintKey.setPressed(true);
        } else {
            mc.options.sprintKey.setPressed(false);
        }
    }
}
