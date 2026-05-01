package kefirdlc.dev.module.impl.render;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.event.impl.render.RenderEvent;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.util.render.core.Renderer2D;

@ModuleInfo(name = "TestModule", category = Category.RENDER)
public class TestModule extends Function {
    
    @Override
    public void onEnable() {
        super.onEnable();
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Subscribe
    public void onRender(RenderEvent event) {
        // Получаем рендерер и размеры экрана прямо из вашего ивента
        Renderer2D renderer = event.renderer();
        int width = event.viewportWidth();
        int height = event.viewportHeight();

        // Передаем их в менеджер уведомлений

    }
}