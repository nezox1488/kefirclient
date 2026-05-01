package kefirdlc.dev.mixin.game;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.util.others.Friends;
import kefirdlc.dev.util.others.ServerUtil;
import kefirdlc.dev.util.render.utils.ChatUtils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Unique
    private static final Set<String> problem = Set.of(
            "hub", "lobby", "рги", "дщиин", "дуфм", "дуфму", "leave", "leav", "logout"
    );

    @Unique
    private String last= null;
    @Unique
    private long time = 0L;



    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void onSendChatMessage(String message, CallbackInfo ci) {
        if (message == null) return;
        String text = message.trim();
        if (!text.startsWith(".friend")) return;

        String[] args = text.split("\\s+");
        if (args.length < 3 || !args[1].equalsIgnoreCase("add")) {
            ChatUtils.sendMessage(Formatting.GRAY + "Использование: " + Formatting.YELLOW + ".friend add Ник");
            ci.cancel();
            return;
        }

        String nick = args[2].trim();
        if (nick.isEmpty()) {
            ChatUtils.sendMessage(Formatting.RED + "Ник не может быть пустым");
            ci.cancel();
            return;
        }

        if (Friends.isFriend(nick)) {
            ChatUtils.sendMessage(Formatting.YELLOW + nick + Formatting.GRAY + " уже в друзьях");
            ci.cancel();
            return;
        }

        Friends.addFriend(nick);
        ChatUtils.sendMessage(Formatting.GREEN + "Добавлен друг: " + Formatting.WHITE + nick);
        ci.cancel();
    }

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    public void onSendChatCommand(String command, CallbackInfo ci) {
        String fullCommand = command.trim();
        if (fullCommand.isEmpty()) return;
        String baseCommand = fullCommand.split(" ")[0].toLowerCase();

        if (problem.contains(baseCommand) && ServerUtil.inPvp()) {
            long now = System.currentTimeMillis();
            if (!fullCommand.equalsIgnoreCase(last) || (now - time) > 3000) {
                last = fullCommand;
                time = now;
                ChatUtils.sendMessage(Formatting.RED + "Вы в PvP! " + Formatting.GRAY + "Введите команду ещё раз для подтверждения: " + Formatting.YELLOW + "/" + fullCommand);
                ci.cancel();
            } else {
                last = null;
                time = 0L;
            }
        }
    }
}
