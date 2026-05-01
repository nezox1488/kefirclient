package kefirdlc.dev.module.api;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.event.impl.input.EventMouseScroll;
import kefirdlc.dev.event.impl.presss.EventMouseButton;
import kefirdlc.dev.event.impl.presss.EventPress;
import kefirdlc.dev.module.setting.api.Setting;
import kefirdlc.dev.ui.notification.NotificationManager;
import kefirdlc.dev.ui.notification.NotificationType;
import kefirdlc.dev.util.wrapper.Wrapper;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
public class Function implements Wrapper {
    private final String name,desc;
    private final Category category;
    private int key = -1;
    private boolean state;
    private boolean binding;
    private BindMode bindMode = BindMode.TOGGLE;
    private final boolean isVisual;
    private final List<Setting<?>> settings = new ArrayList<>();

    public Function() {
        ModuleInfo info = getClass().getAnnotation(ModuleInfo.class);
        Objects.requireNonNull(info, "ModuleInfo annotation is required");
        name = info.name();
        desc = info.desc();
        category = info.category();
        isVisual = info.visual();
    }

    public enum BindMode {
        TOGGLE, HOLD
    }

    public void onEnable() {
        KefirDLC.getInstance().getEventBus().register(this);
    }

    public void onDisable() {
        KefirDLC.getInstance().getEventBus().unregister(this);}

    public void toggle() {
        setState(!state);
    }



    public void setState(boolean newState) {
        if (this.state == newState) return;
        this.state = newState;

        if (state) {
            onEnable();
            NotificationManager.add(this.getName(), "Enable", NotificationType.SUCCESS);
        } else {
            onDisable();
            NotificationManager.add(this.getName(), "Disabled", NotificationType.DISABLE);
        }
    }



    @Subscribe
    public void onKey(EventPress e) {
        if (binding && e.getAction() == GLFW.GLFW_PRESS) {
            key = e.getKey();
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_DELETE) {
                key = -1;
            }
            binding = false;
            return;
        }

        if (binding) return;


        if (key == -1) return;

        if (e.getKey() == key) {
            if (bindMode == BindMode.TOGGLE) {
                if (e.getAction() == GLFW.GLFW_PRESS) {
                    toggle();
                }
            } else {
                if (e.getAction() == GLFW.GLFW_PRESS) {
                    setState(true);
                } else if (e.getAction() == GLFW.GLFW_RELEASE) {
                    setState(false);
                }
            }
        }
    }
    @Subscribe
    public void onMouse(EventMouseButton e) {
        if (binding && e.getAction() == GLFW.GLFW_PRESS) {
            key = 1000 + e.getButton();
            binding = false;
            return;
        }

        if (!binding && key == 1000 + e.getButton()) {
            if (bindMode == BindMode.TOGGLE) {
                if (e.getAction() == GLFW.GLFW_PRESS) toggle();
            } else {
                if (e.getAction() == GLFW.GLFW_PRESS) setState(true);
                else if (e.getAction() == GLFW.GLFW_RELEASE) setState(false);
            }
        }
    }
    @Subscribe
    public void onScroll(EventMouseScroll e) {
        if (!binding) return;

        key = e.getDelta() > 0 ? 2000 : 2001;
        binding = false;
    }
    @Subscribe
    public void onKeyToggle(EventPress e) {
        if (binding) return;

        if (e.getAction() == GLFW.GLFW_PRESS && e.getKey() == key) {
            toggle();
        }
    }
    @Subscribe
    public void onMouseToggle(EventMouseButton e) {
        if (binding) return;

        if (e.getAction() != GLFW.GLFW_PRESS) return;

        if (key >= 1000 && key == 1000 + e.getButton()) {
            toggle();
        }
    }

    @Subscribe
    public void onScrollToggle(EventMouseScroll e) {
        if (binding) return;

        if (key == 2000 && e.getDelta() > 0) toggle();
        if (key == 2001 && e.getDelta() < 0) toggle();
    }

    public void startBinding() {
        binding = true;
    }

    public void stopBinding() {
        binding = false;
    }
    public void setKey(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public boolean isBinding() {
        return binding;
    }
    public boolean isToggled() {
        return state;
    }

    public boolean canBindToKey(int key) {
        return key != GLFW.GLFW_KEY_ESCAPE;
    }

    protected void addSettings(Setting<?>... settings) {
        this.settings.addAll(Arrays.asList(settings));
        KefirDLC.getInstance().getSettingManager().addSettings(settings);
    }


    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Category getCategory() {
        return category;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }
}
