package kefirdlc.dev.module.impl.render;
// coded by sitoku \\
// since 28.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.event.impl.game.EventUpdate;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.module.setting.impl.BooleanSetting;
import net.minecraft.entity.effect.StatusEffects;

@ModuleInfo(name = "NoRender", category = Category.RENDER, desc = "Скрывает лишние эффекты")
public class NoRender extends Function {

    public final BooleanSetting fireOverlay = new BooleanSetting("Fire Overlay", this, true);
    public final BooleanSetting badEffects = new BooleanSetting("Bad Effects", this, true);
    public final BooleanSetting glow = new BooleanSetting("Glow", this, true);
    public final BooleanSetting hurtCam = new BooleanSetting("Hurt Cam", this, true);

    public NoRender() {
        addSettings(fireOverlay, badEffects, glow, hurtCam);
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.player == null) return;

        if (fireOverlay.getValue() && mc.player.isOnFire()) {
            mc.player.extinguish();
        }

        if (badEffects.getValue()) {
            mc.player.removeStatusEffect(StatusEffects.NAUSEA);
            mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
            mc.player.removeStatusEffect(StatusEffects.DARKNESS);
        }

        if (glow.getValue()) {
            mc.player.removeStatusEffect(StatusEffects.GLOWING);
        }
    }
}
