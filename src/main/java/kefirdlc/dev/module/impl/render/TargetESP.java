package kefirdlc.dev.module.impl.render;
// coded by sitoku \\
// since 28.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.event.impl.render.RenderEvent;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.module.impl.combat.AttackAura;
import kefirdlc.dev.module.setting.impl.BooleanSetting;
import kefirdlc.dev.module.setting.impl.NumberSetting;
import kefirdlc.dev.util.math.MathUtil;
import kefirdlc.dev.util.math.ProjectionUtil;
import kefirdlc.dev.util.render.core.Renderer2D;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

@ModuleInfo(name = "TargetESP", category = Category.RENDER, desc = "Кристаллы вокруг таргета Aura")
public class TargetESP extends Function {

    private final NumberSetting crystalSize = new NumberSetting("Crystal Size", this, 0.8f, 0.2f, 2.0f, 0.1f);
    private final NumberSetting crystalCount = new NumberSetting("Crystal Count", this, 20.0f, 8.0f, 30.0f, 1.0f);
    private final BooleanSetting redOnHit = new BooleanSetting("Red On Hit", this, true);

    private LivingEntity lastTarget;
    private float anim;
    private float hitAnim;

    public TargetESP() {
        addSettings(crystalSize, crystalCount, redOnHit);
    }

    @Subscribe
    public void onRender(RenderEvent event) {
        AttackAura aura = KefirDLC.getInstance().getFunctionManager().getModule(AttackAura.class);
        LivingEntity target = aura != null ? aura.getTarget() : null;

        if (target != null && target.isAlive()) {
            lastTarget = target;
            anim = Math.min(1f, anim + 0.08f);
        } else {
            anim = Math.max(0f, anim - 0.08f);
            if (anim <= 0.001f) {
                lastTarget = null;
                return;
            }
        }

        hitAnim = Math.max(0f, hitAnim - 0.05f);

        if (lastTarget == null) return;
        renderCrystals(event.renderer(), lastTarget, anim);
    }

    public void onHit() {
        hitAnim = 1f;
    }

    public static void notifyHit() {
        TargetESP module = KefirDLC.getInstance().getFunctionManager().getModule(TargetESP.class);
        if (module != null && module.isToggled()) {
            module.onHit();
        }
    }

    private void renderCrystals(Renderer2D renderer, LivingEntity target, float alpha) {
        float tickDelta = MathUtil.getTickDelta();
        Vec3d center3d = ProjectionUtil.interpolateEntity(target, tickDelta).add(0.0, target.getHeight() * 0.6, 0.0);
        Vec3d screen = ProjectionUtil.toScreen(center3d);
        if (screen == null) return;

        float sx = (float) screen.x;
        float sy = (float) screen.y;

        int count = Math.max(1, crystalCount.getValueInt());
        float size = crystalSize.getValueFloat() * 4.0f * alpha;
        float radius = 14f + target.getWidth() * 18f;
        float time = (mc.player.age + tickDelta) * 4.2f;

        Color base = new Color(130, 190, 255, (int) (190 * alpha));
        if (redOnHit.getValue() && hitAnim > 0f) {
            float t = hitAnim;
            int r = (int) (255 * t + base.getRed() * (1 - t));
            int g = (int) (65 * t + base.getGreen() * (1 - t));
            int b = (int) (65 * t + base.getBlue() * (1 - t));
            base = new Color(r, g, b, (int) (200 * alpha));
        }

        for (int i = 0; i < count; i++) {
            float angle = time * 24f + i * (360f / count);
            float rad = (float) Math.toRadians(angle);
            float cx = sx + (float) Math.cos(rad) * radius;
            float cy = sy + (float) Math.sin(rad) * (radius * 0.38f);
            drawCrystal(renderer, cx, cy, size, angle, base);
        }
    }

    private void drawCrystal(Renderer2D renderer, float x, float y, float size, float angle, Color color) {
        renderer.pushTranslation(x, y);
        renderer.pushRotation(angle + 45f);

        int glow = new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, color.getAlpha() / 2)).getRGB();
        int core = color.getRGB();

        renderer.rect(-size * 0.85f, -size * 0.85f, size * 1.7f, size * 1.7f, 1.5f, glow);
        renderer.rect(-size * 0.5f, -size * 0.5f, size, size, 1f, core);
        renderer.rectOutline(-size * 0.5f, -size * 0.5f, size, size, 1f, new Color(255, 255, 255, 180).getRGB(), 0.6f);

        renderer.popRotation();
        renderer.popTransform();
    }
}
