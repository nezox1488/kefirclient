package kefirdlc.dev.util.others.Lisener;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.event.impl.game.EventUpdate;
import kefirdlc.dev.event.impl.game.PacketEvent;
import kefirdlc.dev.event.impl.player.UsingItemEvent;
import kefirdlc.dev.util.Player.PlayerInventoryComponent;
import kefirdlc.dev.util.others.ServerUtil;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class EventListener implements Listener {
    public static boolean serverSprint;
    public static int selectedSlot;

    @Subscribe
    public void onTick(EventUpdate e) {
        ServerUtil.tick();
        KefirDLC.getInstance().getAttackPerpetrator().tick();
        PlayerInventoryComponent.tick();
    }

    @Subscribe
    public void onPacket(PacketEvent e) {
        switch (e.getPacket()) {
            case ClientCommandC2SPacket command -> serverSprint = switch (command.getMode()) {
                case ClientCommandC2SPacket.Mode.START_SPRINTING -> true;
                case ClientCommandC2SPacket.Mode.STOP_SPRINTING -> false;
                default -> serverSprint;
            };
            case UpdateSelectedSlotC2SPacket slot -> selectedSlot = slot.getSelectedSlot();
            default -> {}
        }

        KefirDLC.getInstance().getAttackPerpetrator().onPacket(e);
    }

    @Subscribe
    public void onUsingItemEvent(UsingItemEvent e) {
        KefirDLC.getInstance().getAttackPerpetrator().onUsingItem(e);
    }
}
