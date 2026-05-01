package kefirdlc.dev.event.impl.game;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.event.api.Event;
import kefirdlc.dev.util.wrapper.Wrapper;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;

@Getter
public class KeyEvent extends Event implements Wrapper {

    private final Screen screen;
    private final InputUtil.Type type;
    private final int key;
    private final int action;

    public KeyEvent(Screen screen, InputUtil.Type type, int key, int action) {
        this.screen = screen;
        this.type = type;
        this.key = key;
        this.action = action;
    }

    public boolean isKeyDown(int key) {
        return isKeyDown(key, mc.currentScreen == null);
    }

    public boolean isKeyDown(int key, boolean screen) {
        return this.key == key && action == 1 && screen;
    }

    public boolean isKeyReleased(int key) {
        return isKeyReleased(key, mc.currentScreen == null);
    }

    public boolean isKeyReleased(int key, boolean screen) {
        return this.key == key && action == 0 && screen;
    }

}