package kefirdlc.dev.module.impl.render;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.event.impl.render.RenderEvent;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.ui.notification.NotificationManager;


@ModuleInfo(
        name = "Notifications",
        category = Category.RENDER,
        visual = true
)
public class NotificationModule extends Function {

    public NotificationModule() {

    }

    @Subscribe
    public void onRender(RenderEvent e) {
        NotificationManager.render(e.renderer());
    }
}