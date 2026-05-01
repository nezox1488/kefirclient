package kefirdlc.dev.util.Player;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.KefirDLC;
import kefirdlc.dev.module.impl.combat.furry.Angle;
import kefirdlc.dev.module.impl.combat.furry.AngleUtil;
import kefirdlc.dev.util.math.MathUtil;
import kefirdlc.dev.util.others.ItemUtil;
import kefirdlc.dev.util.wrapper.Wrapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntPredicate;
import lombok.experimental.UtilityClass;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@UtilityClass
public class PlayerInventoryUtil implements Wrapper {

    public void moveItem(Slot from, int to) {
        if (from != null) moveItem(from.id, to, false, false);
    }

    public void moveItem(Slot from, int to, boolean task) {
        moveItem(from, to, task, false);
    }

    public void moveItem(Slot from, int to, boolean task, boolean updateInventory) {
        if (from != null) moveItem(from.id, to, task, updateInventory);
    }

    public void moveItem(int from, int to, boolean task, boolean updateInventory) {
        if (from == to || from == -1) return;

        int count = Math.toIntExact(slots().count()) - 10;
        if (from >= count && count == 36) {
            if (task) PlayerInventoryComponent.addTask(() -> clickSlot(to, from - count, SlotActionType.SWAP, false));
            else clickSlot(to, from - count, SlotActionType.SWAP, false);
            return;
        }

        if (task) PlayerInventoryComponent.addTask(() -> moveItem(from, to, updateInventory));
        else moveItem(from, to, updateInventory);
    }

    public void moveItem(int from, int to, boolean updateInventory) {
        clickSlot(from, 0, SlotActionType.SWAP, false);
        clickSlot(to, 0, SlotActionType.SWAP, false);
        clickSlot(from, 0, SlotActionType.SWAP, false);
        if (updateInventory) updateSlots();
    }

    public void swapHand(Slot slot, Hand hand, boolean task) {
        swapHand(slot, hand, task,false);
    }

    public void swapHand(Slot slot, Hand hand, boolean task, boolean updateInventory) {
        if (slot == null || slot.id == -1 || (hand.equals(Hand.OFF_HAND) && !(slot.inventory instanceof PlayerInventory || slot.inventory instanceof EnderChestInventory))) return;
        int button = hand.equals(Hand.MAIN_HAND) ? mc.player.getInventory().selectedSlot : 40;
        if (task) PlayerInventoryComponent.addTask(() -> swap(slot, button, updateInventory));
        else swap(slot, button, updateInventory);
    }

    public void swap(Slot slot, int button, boolean updateInventory) {
        clickSlot(slot, button, SlotActionType.SWAP, false);
        if (updateInventory) PlayerInventoryUtil.updateSlots();
    }

    public void swapAndUse(Slot slot, String text, boolean task) {
        if (slot == null) {


            return;
        }
        if (task) PlayerInventoryComponent.addTask(() -> swapAndUse(slot, AngleUtil.cameraAngle()));
        else swapAndUse(slot, AngleUtil.cameraAngle());
    }

    public void swapAndUse(Item item) {
        swapAndUse(item, AngleUtil.cameraAngle(), true);
    }

    public void swapAndUse(Item item, Angle angle, boolean task) {
        float cooldownProgress = ItemUtil.getCooldownProgress(item);

        if (cooldownProgress > 0) {
            String time = MathUtil.round(cooldownProgress, 0.1) + "с";


            return;
        }

        Slot slot = getSlot(item);
        if (slot == null) {


            return;
        }

        if (task) PlayerInventoryComponent.addTask(() -> swapAndUse(slot, angle));
        else swapAndUse(slot, angle);
    }

    public void swapAndUse(Slot slot, Angle angle) {
        swapHand(slot, Hand.MAIN_HAND, false);
        PlayerIntersectionUtil.interactItem(Hand.MAIN_HAND, angle);
        swapHand(slot, Hand.MAIN_HAND, false,true);
    }

    public void updateSlots() {
        ScreenHandler screenHandler = mc.player.currentScreenHandler;


        mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(
                screenHandler.syncId,
                screenHandler.getRevision(),
                (short) 0,
                (byte) 0,
                SlotActionType.PICKUP_ALL,
                Int2ObjectMaps.emptyMap(),
                null
        ));
    }


    public void closeScreen(boolean packet) {
        if (packet) mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        else mc.player.closeHandledScreen();
    }

    public void clickSlot(Slot slot, int button, SlotActionType clickType, boolean silent) {
        if (slot != null) clickSlot(slot.id, button, clickType, silent);
    }

    public void clickSlot(int slotId, int buttonId, SlotActionType clickType, boolean silent) {
        clickSlot(mc.player.currentScreenHandler.syncId, slotId, buttonId, clickType, silent);
    }

    public void clickSlot(int windowId, int slotId, int buttonId, SlotActionType clickType, boolean silent) {
        mc.interactionManager.clickSlot(windowId, slotId, buttonId, clickType, mc.player);
        if (silent) mc.player.currentScreenHandler.onSlotClick(slotId, buttonId, clickType, mc.player);
    }

    public Slot getSlot(Item item) {
        return getSlot(item,s -> true);
    }

    public Slot getSlot(Item item, Predicate<Slot> filter) {
        return getSlot(item, Comparator.comparingInt(s -> 0), filter);
    }

    public Slot getSlot(Predicate<Slot> filter) {
        return slots().filter(filter).findFirst().orElse(null);
    }

    public Slot getSlot(Predicate<Slot> filter, Comparator<Slot> comparator) {
        return slots().filter(filter).max(comparator).orElse(null);
    }

    public Slot getSlot(Item item, Comparator<Slot> comparator, Predicate<Slot> filter) {
        return slots().filter(s -> s.getStack().getItem().equals(item)).filter(filter).max(comparator).orElse(null);
    }

    public Slot getFoodMaxSaturationSlot() {
        return slots().filter(s -> s.getStack().get(DataComponentTypes.FOOD) != null && !s.getStack().get(DataComponentTypes.FOOD).canAlwaysEat())
                .max(Comparator.comparingDouble(s -> s.getStack().get(DataComponentTypes.FOOD).saturation())).orElse(null);
    }

    public Slot getSlot(java.util.List<Item> item) {
        return slots().filter(s -> item.contains(s.getStack().getItem())).findFirst().orElse(null);
    }

    public Slot getPotion(RegistryEntry<StatusEffect> effect) {
        return slots().filter(s -> {
            PotionContentsComponent component = s.getStack().get(DataComponentTypes.POTION_CONTENTS);
            if (component == null) return false;
            return StreamSupport.stream(component.getEffects().spliterator(), false).anyMatch(e -> e.getEffectType().equals(effect));
        }).findFirst().orElse(null);
    }

    public Slot getPotionFromCategory(StatusEffectCategory category) {
        return slots().filter(s -> {
            ItemStack stack = s.getStack();
            PotionContentsComponent component = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (!stack.getItem().equals(Items.SPLASH_POTION) || component == null) return false;
            StatusEffectCategory category2 = category.equals(StatusEffectCategory.BENEFICIAL) ? StatusEffectCategory.HARMFUL : StatusEffectCategory.BENEFICIAL;
            long effects = StreamSupport.stream(component.getEffects().spliterator(), false).filter(e -> e.getEffectType().value().getCategory().equals(category)).count();
            long effects2 = StreamSupport.stream(component.getEffects().spliterator(), false).filter(e -> e.getEffectType().value().getCategory().equals(category2)).count();
            return effects >= effects2;
        }).findFirst().orElse(null);
    }

    public int getInventoryCount(Item item) {
        return IntStream.range(0, 45).filter(i -> Objects.requireNonNull(mc.player).getInventory().getStack(i).getItem().equals(item)).map(i -> mc.player.getInventory().getStack(i).getCount()).sum();
    }

    public int getHotbarItems(List<Item> items) {
        return IntStream.range(0, 9).filter(i -> items.contains(mc.player.getInventory().getStack(i).getItem())).findFirst().orElse(-1);
    }

    public int getHotbarSlotId(IntPredicate filter) {
        return IntStream.range(0, 9).filter(filter).findFirst().orElse(-1);
    }

    public int getCount(Predicate<Slot> filter) {
        return slots().filter(filter).mapToInt(s -> s.getStack().getCount()).sum();
    }

    public Slot mainHandSlot() {
        long count = slots().count();
        int i = count == 46 ? 10 : 9;
        return slots().toList().get(Math.toIntExact(count - i + mc.player.getInventory().selectedSlot));
    }

    public boolean isServerScreen() {
        if (mc.player == null) return false;
        return slots().toList().size() != 46;
    }

    public Stream<Slot> slots(){
        if(mc.player == null) return Stream.empty();
        return mc.player.currentScreenHandler.slots.stream();
    }


    public enum Swap {
        Pickup,
        Swap
    }

    public void jump() {
        if (mc.player.isSprinting()) {
            float g = mc.player.getYaw() * ((float)Math.PI / 180F);
            mc.player.addVelocityInternal(new Vec3d(-MathHelper.sin(g) * 0.2F, 0.0F, MathHelper.cos(g) * 0.2F));
        }
        mc.player.velocityDirty = true;
    }

    public int indexToSlot(int index) {
        if (index >= 0 && index <= 8) return 36 + index;
        return index;
    }

    public void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        assert mc.interactionManager != null;
        mc.interactionManager.sendSequencedPacket(mc.world, packetCreator);
    }


    public void switchSlot( int slot, int previousSlot) {
        if (slot == -1 || previousSlot == -1 || slot == KefirDLC.getInstance().getPlayerServis().getServerSlot()) return;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.syncSelectedSlot();

    }
    public void switchSlot(int slot) {
        if (slot == -1 || slot ==  KefirDLC.getInstance().getPlayerServis().getServerSlot()) return;


        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.syncSelectedSlot();
    }


    public void switchBack(int slot, int previousSlot) {
        if (slot == -1 || previousSlot == -1 || slot == KefirDLC.getInstance().getPlayerServis().getServerSlot()) return;


        mc.player.getInventory().selectedSlot = previousSlot;
        mc.interactionManager.syncSelectedSlot();
        mc.player.getInventory().selectedSlot = slot;
    }

    public void swap(Swap mode, int slot, int targetSlot) {
        if (slot == -1 || targetSlot == -1) return;
        switch (mode) {
            case Pickup -> {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, indexToSlot(slot), 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, indexToSlot(targetSlot), 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, indexToSlot(slot), 0, SlotActionType.PICKUP, mc.player);
            }
            case Swap -> mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, targetSlot, SlotActionType.SWAP, mc.player);
        }
    }

    public void updateSlotss() {
        ScreenHandler screenHandler = mc.player.currentScreenHandler;


        mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(
                screenHandler.syncId,
                screenHandler.getRevision(),
                (short) 0,
                (byte) 0,
                SlotActionType.PICKUP_ALL,
                Int2ObjectMaps.emptyMap(),
                null
        ));
    }

    public void closeScreens(boolean packet) {
        if (packet) mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        else mc.player.closeHandledScreen();
    }

//        public void bypassSwap(int slot, int targetSlot) {
//            if (slot == -1 || targetSlot == -1) return;
//
//            if (DrugHack.getInstance().getModuleManager().getModule(GuiMove.class).funtime.getValue()) {
//                DrugHack.getInstance().getModuleManager().getModule(GuiMove.class).setTicks(8);
//
//                new Thread(() -> {
//                    try {
//                        if (DrugHack.getInstance().getModuleManager().getModule(GuiMove.class).funtime.getValue()) Thread.sleep(DrugHack.getInstance().getModuleManager().getModule(GuiMove.class).getTicks() * 50L);
//                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, indexToSlot(slot), 0, SlotActionType.PICKUP, mc.player);
//                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, targetSlot, 0, SlotActionType.PICKUP, mc.player);
//                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, indexToSlot(slot), 0, SlotActionType.PICKUP, mc.player);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }).start();
//            } else swap(slot, targetSlot);
//        }

    public void swap(int slot, int targetSlot) {
        if (slot == -1 || targetSlot == -1) return;
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, PlayerInventoryUtil.indexToSlot(slot), 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, targetSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, PlayerInventoryUtil.indexToSlot(slot), 0, SlotActionType.PICKUP, mc.player);
    }


    public void swing() {
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    public int findBestSword(int start, int end) {
        int netheriteSlot = -1;
        int diamondSlot = -1;
        int ironSlot = -1;
        int goldenSlot = -1;
        int stoneSlot = -1;
        int woodenSlot = -1;

        for (int i = end; i >= start; i--) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.NETHERITE_SWORD) netheriteSlot = i;
            else if (stack.getItem() == Items.DIAMOND_SWORD) diamondSlot = i;
            else if (stack.getItem() == Items.IRON_SWORD) ironSlot = i;
            else if (stack.getItem() == Items.GOLDEN_SWORD) goldenSlot = i;
            else if (stack.getItem() == Items.STONE_SWORD) stoneSlot = i;
            else if (stack.getItem() == Items.WOODEN_SWORD) woodenSlot = i;
        }

        if (netheriteSlot != -1) return netheriteSlot;
        if (diamondSlot != -1) return diamondSlot;
        if (ironSlot != -1) return ironSlot;
        if (goldenSlot != -1) return goldenSlot;
        if (stoneSlot != -1) return stoneSlot;

        return woodenSlot;
    }

    public int findBestAxe(int start, int end) {
        int netheriteSlot = -1;
        int diamondSlot = -1;
        int ironSlot = -1;
        int goldenSlot = -1;
        int stoneSlot = -1;
        int woodenSlot = -1;

        for (int i = end; i >= start; i--) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.NETHERITE_AXE) netheriteSlot = i;
            else if (stack.getItem() == Items.DIAMOND_AXE) diamondSlot = i;
            else if (stack.getItem() == Items.IRON_AXE) ironSlot = i;
            else if (stack.getItem() == Items.GOLDEN_AXE) goldenSlot = i;
            else if (stack.getItem() == Items.STONE_AXE) stoneSlot = i;
            else if (stack.getItem() == Items.WOODEN_AXE) woodenSlot = i;
        }

        if (netheriteSlot != -1) return netheriteSlot;
        if (diamondSlot != -1) return diamondSlot;
        if (ironSlot != -1) return ironSlot;
        if (goldenSlot != -1) return goldenSlot;
        if (stoneSlot != -1) return stoneSlot;

        return woodenSlot;
    }

    public int findFastItem(BlockState blockState, int start, int end) {
        double bestScore = -1;
        int bestSlot = -1;

        for (int i = start; i <= end; i++) {
            double score = mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(blockState);

            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    public int findBestChestplate(int start, int end) {
        int leatherSlot = -1;
        int chainmail = -1;
        int ironSlot = -1;
        int goldenSlot = -1;
        int diamondSlot = -1;
        int netheriteSlot = -1;

        for (int i = end; i >= start; i--) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.LEATHER_CHESTPLATE) leatherSlot = i;
            else if (stack.getItem() == Items.CHAINMAIL_CHESTPLATE) chainmail = i;
            else if (stack.getItem() == Items.IRON_CHESTPLATE) ironSlot = i;
            else if (stack.getItem() == Items.GOLDEN_CHESTPLATE) goldenSlot = i;
            else if (stack.getItem() == Items.DIAMOND_CHESTPLATE) diamondSlot = i;
            else if (stack.getItem() == Items.NETHERITE_CHESTPLATE) netheriteSlot = i;
        }

        if (chainmail != -1) return chainmail;
        if (ironSlot != -1) return ironSlot;
        if (goldenSlot != -1) return goldenSlot;
        if (diamondSlot != -1) return diamondSlot;
        if (netheriteSlot != -1) return netheriteSlot;

        return leatherSlot;
    }

    public int getArmorColor(PlayerEntity entity, int slot) {

        ItemStack stack = entity.getInventory().getStack(36 + slot);

        if (stack.isIn(ItemTags.DYEABLE)) {
            return DyedColorComponent.getColor(stack, -6265536);
        }
        return -1;
    }
    public int find(Item item) { return find(item, 0, 35); }
    public int findHotbar(Item item) { return find(item, 0, 8); }
    public int findInventory(Item item) { return find(item, 9, 35); }
    public int find(Class<? extends Item> item) { return find(item, 0, 35); }
    public int findHotbar(Class<? extends Item> item) { return find(item, 0, 8); }
    public int findInventory(Class<? extends Item> item) { return find(item, 9, 35); }

    public int find(Item item, int start, int end) {
        for (int i = end; i >= start; i--) if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        return -1;
    }

    public int find(Class<? extends Item> item, int start, int end) {
        for (int i = end; i >= start; i--) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.getItem().getClass().isAssignableFrom(item)) continue;

            return i;
        }

        return -1;
    }

    public int findEmptySlot(int start, int end) {
        for (int i = end; i >= start; i--) if (mc.player.getInventory().getStack(i).isEmpty()) return i;
        return -1;
    }

}
