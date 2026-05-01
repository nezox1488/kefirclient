package kefirdlc.dev.module;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.impl.combat.AttackAura;
import kefirdlc.dev.module.impl.misc.DebugPanelModule;
import kefirdlc.dev.module.impl.misc.NameProtect;
import kefirdlc.dev.module.impl.movement.AutoSprint;
import kefirdlc.dev.module.impl.movement.GuiMove;
import kefirdlc.dev.module.impl.movement.NoDelay;
import kefirdlc.dev.module.impl.movement.Speed;
import kefirdlc.dev.module.impl.render.Interface;
import kefirdlc.dev.module.impl.render.NameTags;
import kefirdlc.dev.module.impl.render.NoRender;
import kefirdlc.dev.module.impl.render.NotificationModule;
import kefirdlc.dev.module.impl.render.TargetESP;
import kefirdlc.dev.ui.clickgui.ClickGuiScreen;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class FunctionManager {
    private final List<Function> modules = new ArrayList<>();

    public FunctionManager() {
        modules.addAll(Arrays.asList(
                new AutoSprint(),
                new Interface(),
                new TargetESP(),
                new NameTags(),
                new NoRender(),
                new DebugPanelModule(),
                new NoDelay(),
                new Speed(),
                new NotificationModule(),
                new AttackAura(),
                new GuiMove(),

                new NameProtect(),
                new ClickGuiScreen()
        ));
    }

    public List<Function> getModules() {
        return modules;
    }

    public List<Function> getModules(Category category) {
        return modules.stream()
                .filter(module -> module.getCategory() == category)
                .toList();
    }

    @SuppressWarnings("unchecked")
    public <T extends Function> T getModule(Class<T> tClass) {
        return (T) modules.stream()
                .filter(module -> module.getClass() == tClass)
                .findFirst()
                .orElse(null);
    }
}
