package kefirdlc.dev.module.impl.movement;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.event.impl.game.CloseScreenEvent;
import kefirdlc.dev.event.impl.game.EventUpdate;
import kefirdlc.dev.event.impl.game.PacketEvent;
import kefirdlc.dev.event.impl.input.ClickSlotEvent;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.util.Player.MobilityHandler;
import kefirdlc.dev.util.Player.PlayerIntersectionUtil;
import kefirdlc.dev.util.Player.PlayerInventoryComponent;
import kefirdlc.dev.util.Player.PlayerInventoryUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "GuiMove", category = Category.MOVEMENT, desc = "Можно ходить в инвенторе")
public class GuiMove extends Function {

    private final List<Packet<?>> packets = new ArrayList<>();

    public GuiMove() {

    }

    @Subscribe
    public void onPacket(PacketEvent e) {
        switch (e.getPacket()) {
            case ClickSlotC2SPacket slot when (!packets.isEmpty() || MobilityHandler.hasPlayerMovement()) && PlayerInventoryComponent.shouldSkipExecution() -> {
                packets.add(slot);
                e.cancel();
            }
            case CloseScreenS2CPacket screen when screen.getSyncId() == 0 -> e.cancel();
            default -> {
            }
        }
    }

    @Subscribe
    public void onTick(EventUpdate e) {
        if (mc.player == null) return;
        if (!PlayerInventoryUtil.isServerScreen() && PlayerInventoryComponent.shouldSkipExecution() && (!packets.isEmpty() || mc.player.currentScreenHandler.getCursorStack().isEmpty())) {
            PlayerInventoryComponent.updateMoveKeys();
        }
    }

    @Subscribe
    public void onClickSlot(ClickSlotEvent e) {
        SlotActionType actionType = e.getActionType();
        if ((!packets.isEmpty() || MobilityHandler.hasPlayerMovement()) && ((e.getButton() == 1 && !actionType.equals(SlotActionType.SWAP) && !actionType.equals(SlotActionType.THROW)) || actionType.equals(SlotActionType.PICKUP_ALL))) {
            e.cancel();
        }
    }

    @Subscribe
    public void onCloseScreen(CloseScreenEvent e) {
        if (!packets.isEmpty()) PlayerInventoryComponent.addTask(() -> {
            packets.forEach(PlayerIntersectionUtil::sendPacketWithOutEvent);
            packets.clear();
            PlayerInventoryUtil.updateSlots();
        });
    }
}
