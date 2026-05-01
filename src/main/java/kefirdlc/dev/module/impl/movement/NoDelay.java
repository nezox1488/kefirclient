package kefirdlc.dev.module.impl.movement;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.event.impl.game.EventUpdate;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.module.setting.impl.BooleanSetting;

@ModuleInfo(name = "NoDelay", category = Category.MOVEMENT, desc = "- Задержка")
public class NoDelay extends Function {
    public final BooleanSetting BreakCoolDown = new BooleanSetting("BreakCoolDown",  false);

    public final BooleanSetting RightClick = new BooleanSetting("RightClick",  false);

    public final BooleanSetting Jump = new BooleanSetting("Jump",  false);
    public NoDelay() {
    addSettings(BreakCoolDown, RightClick, Jump);
    }
    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (BreakCoolDown.getValue()) {
            assert mc.interactionManager != null;
            mc.interactionManager.blockBreakingCooldown = 0;
        }
        if (Jump.getValue()) {
            assert mc.player != null;
            mc.player.jumpingCooldown = 0;
        }
        if (RightClick.getValue()) mc.itemUseCooldown = 0;
    }
}