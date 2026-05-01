package kefirdlc.dev.module.impl.render;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.event.impl.presss.EventMouseButton;
import kefirdlc.dev.event.impl.render.RenderEvent;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.module.impl.combat.AttackAura;
import kefirdlc.dev.module.impl.render.hud.*;
import kefirdlc.dev.module.setting.impl.BooleanSetting;
import kefirdlc.dev.module.setting.impl.MultiBoxSetting;
import kefirdlc.dev.module.setting.impl.NumberSetting;
import kefirdlc.dev.util.input.KeyNameUtil;
import kefirdlc.dev.ui.clickgui.ClickGuiScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ModuleInfo(name = "Interface", category = Category.RENDER, desc = "Управление HUD", visual = true)
public class Interface extends Function {

    public final BooleanSetting watermark = new BooleanSetting("Watermark", this, true);
    public final BooleanSetting targetHud = new BooleanSetting("TargetHud", this, true);
    public final BooleanSetting hotkeys = new BooleanSetting("Hotkeys", this, true);
    public final BooleanSetting potions = new BooleanSetting("Potions", this, true);
    public final BooleanSetting targetByHover = new BooleanSetting("TargetHud On Hover", this, true);

    public final MultiBoxSetting elements = new MultiBoxSetting("Elements", watermark, targetHud, hotkeys, potions);
    public final NumberSetting alpha = new NumberSetting("Transparency", this, 180, 30, 255, 1);
    public final NumberSetting blur = new NumberSetting("Blur", this, 120, 0, 255, 1);

    private final WatermarkScreen watermarkScreen = new WatermarkScreen();
    private final TargetHudScreen targetHudScreen = new TargetHudScreen();
    private final HotkeysScreen hotkeysScreen = new HotkeysScreen();
    private final PotionsScreen potionsScreen = new PotionsScreen();

    private final List<HudElementScreen> draggableElements = new ArrayList<>();

    private HudElementScreen draggingElement;

    private float watermarkAnim;
    private float targetAnim;
    private float hotkeysAnim;
    private float potionsAnim;

    public Interface() {
        addSettings(elements, targetByHover, alpha, blur);
        draggableElements.add(watermarkScreen);
        draggableElements.add(targetHudScreen);
        draggableElements.add(hotkeysScreen);
        draggableElements.add(potionsScreen);
        setState(true);
    }

    @Subscribe
    public void onRender(RenderEvent event) {
        boolean editorMode = isChatEditorMode();
        boolean clickGuiOpen = isClickGuiOpen();
        int baseAlpha = alpha.getValueInt();
        int baseBlur = blur.getValueInt();

        LivingEntity auraTarget = getAuraTarget();
        LivingEntity hoverTarget = getHoveredTarget();
        LivingEntity hudTarget = auraTarget != null ? auraTarget : (targetByHover.getValue() ? hoverTarget : null);

        List<String> activeBinds = getActiveBinds();
        List<PotionsScreen.PotionLine> potionLines = getPotionLines();

        boolean watermarkVisible = watermark.getValue();
        boolean hotkeysVisible = editorMode || clickGuiOpen || (hotkeys.getValue() && !activeBinds.isEmpty());
        boolean potionsVisible = editorMode || clickGuiOpen || (potions.getValue() && !potionLines.isEmpty());
        boolean targetVisible = editorMode || clickGuiOpen || (targetHud.getValue() && hudTarget != null);

        watermarkAnim = animate(watermarkAnim, watermarkVisible ? 1f : 0f, 0.24f);
        hotkeysAnim = animate(hotkeysAnim, hotkeysVisible ? 1f : 0f, 0.22f);
        potionsAnim = animate(potionsAnim, potionsVisible ? 1f : 0f, 0.22f);
        targetAnim = animate(targetAnim, targetVisible ? 1f : 0f, 0.20f);

        int passiveOverlayAlpha = clickGuiOpen ? Math.max(18, baseAlpha / 8) : baseAlpha;
        int passiveOverlayBlur = clickGuiOpen ? Math.min(18, baseBlur / 8) : baseBlur;

        renderWatermark(event, baseAlpha, baseBlur);
        renderTargetHud(event, passiveOverlayAlpha, passiveOverlayBlur, hudTarget);
        renderHotkeys(event, passiveOverlayAlpha, passiveOverlayBlur, activeBinds);
        renderPotions(event, passiveOverlayAlpha, passiveOverlayBlur, potionLines);

        if (editorMode) {
            handleDragTick();
            renderEditorHints(event);
        } else {
            stopDragging();
        }
    }

    @Subscribe
    public void onMouse(EventMouseButton event) {
        if (!isChatEditorMode()) return;
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {
            float mouseX = getMouseX();
            float mouseY = getMouseY();
            draggingElement = findHoveredElement(mouseX, mouseY);
            if (draggingElement != null) {
                draggingElement.startDrag(mouseX, mouseY);
            }
        }

        if (event.getAction() == GLFW.GLFW_RELEASE) {
            stopDragging();
        }
    }

    private void handleDragTick() {
        boolean lmbHeld = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        float mouseX = getMouseX();
        float mouseY = getMouseY();

        if (!lmbHeld) {
            stopDragging();
            return;
        }

        if (draggingElement == null) {
            draggingElement = findHoveredElement(mouseX, mouseY);
            if (draggingElement != null) {
                draggingElement.startDrag(mouseX, mouseY);
            }
        }

        if (draggingElement != null) {
            draggingElement.dragTo(mouseX, mouseY);
        }
    }

    private void renderWatermark(RenderEvent event, int baseAlpha, int baseBlur) {
        int a = (int) (baseAlpha * watermarkAnim);
        int b = (int) (baseBlur * watermarkAnim);
        if (a <= 3) return;
        watermarkScreen.render(event.renderer(), a, b, "Kefir", mc.getCurrentFps() + " fps", "Developer");
    }

    private void renderTargetHud(RenderEvent event, int baseAlpha, int baseBlur, LivingEntity target) {
        int a = (int) (baseAlpha * targetAnim);
        int b = (int) (baseBlur * targetAnim);
        if (a <= 3) return;
        targetHudScreen.render(event.renderer(), a, b, target);
    }

    private void renderHotkeys(RenderEvent event, int baseAlpha, int baseBlur, List<String> binds) {
        int a = (int) (baseAlpha * hotkeysAnim);
        int b = (int) (baseBlur * hotkeysAnim);
        if (a <= 3) return;
        hotkeysScreen.render(event.renderer(), a, b, binds);
    }

    private void renderPotions(RenderEvent event, int baseAlpha, int baseBlur, List<PotionsScreen.PotionLine> potionLines) {
        int a = (int) (baseAlpha * potionsAnim);
        int b = (int) (baseBlur * potionsAnim);
        if (a <= 3) return;
        potionsScreen.render(event.renderer(), a, b, potionLines);
    }

    private List<String> getActiveBinds() {
        List<String> result = new ArrayList<>();
        for (Function module : KefirDLC.getInstance().getFunctionManager().getModules()) {
            if (!module.isToggled()) continue;
            if (module.getKey() == -1) continue;
            String keyName = KeyNameUtil.getKeyName(module.getKey());
            if (keyName == null || keyName.isEmpty() || "None".equalsIgnoreCase(keyName)) continue;
            result.add(module.getName() + " [" + keyName + "]");
        }
        return result;
    }

    private List<PotionsScreen.PotionLine> getPotionLines() {
        List<PotionsScreen.PotionLine> result = new ArrayList<>();
        if (mc.player == null) return result;

        List<StatusEffectInstance> sorted = mc.player.getStatusEffects().stream()
                .sorted(Comparator.comparingInt(StatusEffectInstance::getDuration).reversed())
                .toList();

        for (StatusEffectInstance effect : sorted) {
            String name = net.minecraft.text.Text.translatable(effect.getEffectType().value().getTranslationKey()).getString();
            int amp = effect.getAmplifier() + 1;
            String duration = StatusEffectUtil.durationToString(effect, 1f, mc.world.getTickManager().getTickRate());
            String line = name + " " + amp + " " + duration;
            boolean negative = effect.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL;
            result.add(new PotionsScreen.PotionLine(line, negative));
        }
        return result;
    }

    private LivingEntity getAuraTarget() {
        AttackAura aura = KefirDLC.getInstance().getFunctionManager().getModule(AttackAura.class);
        if (aura == null || !aura.isToggled()) return null;
        return aura.getTarget();
    }

    private LivingEntity getHoveredTarget() {
        if (!(mc.crosshairTarget instanceof EntityHitResult entityHitResult)) return null;
        if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }

    private void renderEditorHints(RenderEvent event) {
        for (HudElementScreen element : draggableElements) {
            element.renderEditorBounds(event.renderer());
        }
    }

    private HudElementScreen findHoveredElement(float mouseX, float mouseY) {
        for (HudElementScreen element : draggableElements) {
            if (element.contains(mouseX, mouseY)) {
                return element;
            }
        }
        return null;
    }

    private void stopDragging() {
        if (draggingElement != null) {
            draggingElement.setDragging(false);
            draggingElement = null;
        }
    }

    private boolean isChatEditorMode() {
        return mc.currentScreen instanceof ChatScreen;
    }

    private boolean isClickGuiOpen() {
        ClickGuiScreen clickGui = KefirDLC.getInstance().getFunctionManager().getModule(ClickGuiScreen.class);
        return clickGui != null && clickGui.isOpen();
    }

    private float animate(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    private float getMouseX() {
        return (float) (mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth());
    }

    private float getMouseY() {
        return (float) (mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight());
    }

    private static class StatusEffectUtil {
        private static String durationToString(StatusEffectInstance effect, float multiplier, float tickRate) {
            int ticks = Math.max(0, (int) (effect.getDuration() * multiplier));
            int seconds = (int) (ticks / Math.max(1f, tickRate));
            int minutes = seconds / 60;
            int remain = seconds % 60;
            return String.format("%02d:%02d", minutes, remain);
        }
    }
}
