package kefirdlc.dev;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.event.impl.presss.EventPress;
import kefirdlc.dev.event.impl.render.RenderEvent;
import kefirdlc.dev.module.FunctionManager;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.impl.combat.furry.attack.AttackPerpetrator;
import kefirdlc.dev.module.setting.api.SettingManager;
import kefirdlc.dev.module.setting.impl.BooleanSetting;
import kefirdlc.dev.util.Player.PlayerServis;
import kefirdlc.dev.util.others.Lisener.ListenerRepository;
import kefirdlc.dev.util.render.animation.AnimationSystem;
import kefirdlc.dev.util.render.backends.gl.GlBackend;
import kefirdlc.dev.util.render.backends.gl.GlState;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;
import kefirdlc.dev.util.render.text.FontRegistry;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;

@Getter
public class KefirDLC implements ModInitializer {
    // этот клиент спонсировал google openAI участие принимали dickpeeck vovanpuvan chatgpt claude google ai qwen kimi grock gemeni nanobanano perpleity
    public static KefirDLC instance;
    public EventBus eventBus;
    public SettingManager settingManager;
    public FunctionManager functionManager;
    public static GlBackend backend;
    public static Renderer2D renderer;
    public static FontObject uiFont;
    private  PlayerServis playerServis;
    public static boolean initialized = false;
    ListenerRepository listenerRepository;
    AttackPerpetrator attackPerpetrator = new AttackPerpetrator();
    private static synchronized void onInit() {
        if (initialized) {
            return;
        }
        backend = new GlBackend();
        renderer = new Renderer2D(backend);

        FontRegistry.initialize(backend, renderer);
        uiFont = FontRegistry.INTER_MEDIUM;
        initialized = true;
    }

    @Setter
    public boolean panic;

    public KefirDLC() {
        instance = this;
    }

    @Override
    public void onInitialize() {
        eventBus = new EventBus();
        settingManager = new SettingManager();
        functionManager = new FunctionManager();
        playerServis = new PlayerServis();
        initListeners();
        eventBus.register(this);
    }

    @Subscribe
    public void onPresss(EventPress event) {
        if (MinecraftClient.getInstance().currentScreen instanceof ChatScreen) return;
        if (event.getAction() == 1) {

            for (Function module : functionManager.getModules()) {
                if (module.getKey() == event.getKey()) {
                    module.toggle();
                }


                for (var setting : module.getSettings()) {
                    if (setting instanceof BooleanSetting boolSetting) {
                        if (boolSetting.getKey() == event.getKey()) {
                            boolSetting.toggle();
                        }
                    }
                }
            }
        }
    }




    private void initListeners() {
        listenerRepository = new ListenerRepository();
        listenerRepository.setup();
    }


    public static KefirDLC getInstance() {
        return instance;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public SettingManager getSettingManager() {
        return settingManager;
    }

    public FunctionManager getFunctionManager() {
        return functionManager;
    }

    public AttackPerpetrator getAttackPerpetrator() {
        return attackPerpetrator;
    }

    public boolean isPanic() {
        return panic;
    }

    public static void onRender() {
        GlState.Snapshot snapshot = GlState.push();
        try {
            if (!initialized) {
                onInit();
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.getWindow() == null || client.player == null || client.world == null) {
                return;
            }

            int width = client.getWindow().getFramebufferWidth();
            int height = client.getWindow().getFramebufferHeight();
            if (width <= 0 || height <= 0) {
                return;
            }

            AnimationSystem.getInstance().tick();

//            if (!Events.RENDER.hasSubscribers()) {
//                return;
//            }

            try {
                renderer.begin(width, height);
                try {
                    RenderEvent renderEvent = new RenderEvent(client, renderer, uiFont, width, height);
                    renderEvent.call();
                } finally {
                    renderer.end();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            GlState.pop(snapshot);
        }
    }
}
