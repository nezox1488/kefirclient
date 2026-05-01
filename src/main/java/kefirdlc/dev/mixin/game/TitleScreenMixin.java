package kefirdlc.dev.mixin.game;

import kefirdlc.dev.ui.accounts.AccountManagerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void kefir$addAccountsButton(CallbackInfo ci) {
        int buttonWidth = 98;
        int buttonHeight = 20;
        int x = this.width - buttonWidth - 6;
        int y = 6;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Accounts"), button ->
                this.client.setScreen(new AccountManagerScreen(this)))
                .dimensions(x, y, buttonWidth, buttonHeight)
                .build());
    }
}
