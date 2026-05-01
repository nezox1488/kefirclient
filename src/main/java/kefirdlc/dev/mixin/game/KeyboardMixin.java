package kefirdlc.dev.mixin.game;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.event.impl.game.KeyEvent;
import kefirdlc.dev.event.impl.presss.EventPress;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Final
    @Shadow
    private MinecraftClient client;
    @Inject(at = @At("HEAD"), method = "onKey")
    private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        if (!(client.currentScreen instanceof ChatScreen)) {
            EventPress event = new EventPress(input.key(), action);
            event.call();
        }
        KefirDLC.getInstance().getEventBus().post(new KeyEvent(client.currentScreen, InputUtil.Type.KEYSYM, input.key(), action));
    }


}
