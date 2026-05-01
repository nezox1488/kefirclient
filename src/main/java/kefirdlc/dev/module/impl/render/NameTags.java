package kefirdlc.dev.module.impl.render;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.event.impl.render.RenderEvent;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.module.setting.impl.BooleanSetting;
import kefirdlc.dev.util.Player.PlayerIntersectionUtil;
import kefirdlc.dev.util.math.ProjectionUtil;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontRegistry;

import kefirdlc.dev.util.render.utils.ColorUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ModuleInfo(name = "NameTags", category = Category.RENDER, visual = true)
public class NameTags extends Function {

    private final BooleanSetting armor = new BooleanSetting("Armor", true);
    private final BooleanSetting items = new BooleanSetting("Items", true);
    public final BooleanSetting hp = new BooleanSetting("HP", true);
    private final BooleanSetting pings = new BooleanSetting("Ping", true);
    private final BooleanSetting background = new BooleanSetting("Background", true);

    public NameTags() {
        addSettings(armor, items, hp, pings, background);
    }

    @Subscribe
    public void onRender(RenderEvent event) {
        if (fullNullCheck()) return;
        if (mc.currentScreen != null) return;

        Renderer2D renderer = event.renderer();
        float tickDelta = mc.getRenderTickCounter().getTickProgress(true);

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player && mc.options.getPerspective().isFirstPerson()) continue;
            renderPlayerNameTag(player, renderer, tickDelta);
        }

        if (items.getValue()) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof ItemEntity itemEntity) {
                    renderItemTag(itemEntity, renderer, tickDelta);
                }
            }
        }
    }

    private void renderPlayerNameTag(PlayerEntity player, Renderer2D r, float tickDelta) {
        Vec3d worldPos = ProjectionUtil.interpolateEntity(player, tickDelta)
                .add(0, player.getHeight() + 0.5, 0);

        Vec3d screen = ProjectionUtil.toScreen(worldPos);
        if (screen == null) return;

        double distance = mc.player.getCameraPosVec(tickDelta).distanceTo(worldPos);

        float fov = mc.gameRenderer.getFov(mc.gameRenderer.getCamera(), tickDelta, true);

        float baseScale = (float) (1.0 / Math.tan(Math.toRadians(fov * 0.5)));
        float distanceFactor = (float) MathHelper.clamp((float) distance / 12.0f, 1.2f, 1.2f);

        float scale = baseScale * distanceFactor;

        float x = (float) screen.x;
        float y = (float) screen.y;

        r.pushScale(scale, scale, x, y);

        Text text = getTextPlayer(player, false);
        float fontSize = 9f;
        float width = r.getStringWidth(FontRegistry.INTER_MEDIUM, text, fontSize) + 12;
        float height = 15f;


        r.rect(x - width / 2f, y, width, height, 0, new Color(0, 0, 0, 140).getRGB());


        r.text(FontRegistry.INTER_MEDIUM, x, y + height / 2f + 3f, fontSize, text, -1, "c");

           r.popScale();
    }


    private void renderItemTag(ItemEntity item, Renderer2D r, float tickDelta) {
        Vec3d pos = ProjectionUtil.interpolateEntity(item, tickDelta).add(0, 0.5, 0);
        Vec3d screen = ProjectionUtil.toScreen(pos);
        if (screen == null) return;

        double distance = mc.player.getCameraPosVec(tickDelta).distanceTo(pos);
        float scale = (float) MathHelper.clamp(1.0 / (distance * 0.08), 0.7, 1.0);

        float x = (float) screen.x;
        float y = (float) screen.y;

        r.pushScale(scale, scale, x, y);

        ItemStack stack = item.getStack();
        String text = stack.getName().getString()
                + (stack.getCount() > 1 ? " x" + stack.getCount() : "");

        float width = r.getStringWidth(FontRegistry.SF_REGULAR, text, 8f) + 6;

        r.rect(x - width / 2f, y - 4, width, 12, 0,
                new Color(0, 0, 0, 120).getRGB());

        r.text(FontRegistry.SF_REGULAR, x, y + 4, 8f, text,
                getRarityColor(stack), "c");

        r.popScale();
    }

    private void renderArmor(PlayerEntity player, float x, float y) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(player.getMainHandStack());
       // for (int i = 3; i >= 0; i--) stacks.add(player.getInventory().getArmorStack(i));
        stacks.add(player.getOffHandStack());

        stacks.removeIf(ItemStack::isEmpty);
        if (stacks.isEmpty()) return;

        float itemSize = 16;
        float gap = 2;
        float totalWidth = (stacks.size() * itemSize) + ((stacks.size() - 1) * gap);
        float startX = x - totalWidth / 2f;

//        DrawContext context = new DrawContext(mc, mc.getBufferBuilders().getEntityVertexConsumers());

       // context.getMatrices().push();
      //  context.getMatrices().translate(0, 0, 0);

        int i = 0;
        for (ItemStack stack : stacks) {
            float itemX = startX + i * (itemSize + gap);
           // context.drawItem(stack, (int)itemX, (int)y);
           // context.drawItemInSlot(mc.textRenderer, stack, (int)itemX, (int)y);
            i++;
        }
       // context.getMatrices().pop();
       // context.draw();
    }

    private MutableText getTextPlayer(PlayerEntity player, boolean friend) {
        float health = PlayerIntersectionUtil.getHealth(player);

        MutableText text = Text.empty();
        if (pings.getValue()) {
            float ping = getPing(player);
            if (ping >= 0 && ping <= 10000) text.append(Formatting.RESET + " " + getPingColor(getPing(player)) + getPing(player) + Formatting.RESET + " ");
        }
       // if (PenisMain.getInstance().getFriendManager().isFriend(player.getName().getString())) text.append("[" + Formatting.GREEN + "F" + Formatting.RESET + "] ");
        String rawName = player.getDisplayName().getString();
        text.append(replaceSymbolsPreserveStyle(player.getDisplayName()));
      //  if (ServerUtil.isBot(player)) text.append("[" + Formatting.DARK_RED + "BOT" + Formatting.RESET + "] ");

        ItemStack stack = player.getOffHandStack();
        MutableText itemName = (MutableText) stack.getName().copy();
        if (stack.isOf(Items.PLAYER_HEAD) || stack.isOf(Items.TOTEM_OF_UNDYING)) {
            itemName.formatted(stack.getRarity().getFormatting());
            text.append(itemName);
        }
        if (health >= 0 && health <= player.getMaxHealth()) text.append(Formatting.RESET + " [" + Formatting.RED + PlayerIntersectionUtil.getHealthString(player) + Formatting.RESET + "] ");
        //System.out.println(rawName);
        return text;
    }

    public static MutableText replaceSymbolsPreserveStyle(Text original) {
        MutableText result = Text.empty();

        original.visit((style, string) -> {
            String replaced = replaceSymbolsDonate(string);
            result.append(Text.literal(replaced).setStyle(style));
            return Optional.empty();
        }, Style.EMPTY);

        return result;
    }

    private Formatting getPingColor(int ping) {
        if (ping <= 50) return Formatting.GREEN;
        else if (ping <= 100) return Formatting.YELLOW ;
        else if (ping <= 150) return Formatting.GOLD;
        else return Formatting.RED;
    }

    private int getPing(PlayerEntity player) {
        if (mc.getNetworkHandler() == null) return 0;
        var entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        return entry == null ? 0 : entry.getLatency();
    }

    private Color getRarityColor(ItemEntity item) {
        switch (item.getStack().getRarity()) {
            case COMMON:
                return new Color(1f, 1f, 1f, 1f);
            case UNCOMMON:
                return Color.YELLOW;
            case RARE:
                return new Color(0.0f, 0.9f, 1f, 1f);
            case EPIC:
                return new Color(0.6f, 0.1f, 0.7f, 1f);
            default:
                return new Color(1f, 1f, 1f, 1f);
        }
    }


    private int getRarityColor(ItemStack stack) {
        if (stack.getRarity() == net.minecraft.util.Rarity.EPIC) return 0xFFA000FF;
        if (stack.getRarity() == net.minecraft.util.Rarity.RARE) return 0xFF00FFFF;
        if (stack.getRarity() == net.minecraft.util.Rarity.UNCOMMON) return 0xFFFFFF55;
        return -1;
    }


    public static String replaceSymbolsDonate(String string) {
        return string
                .replaceAll("\\[", "")
                .replaceAll("⚡","★")
                .replaceAll("]", "")
                .replaceAll("ᴀ", "a")
                .replaceAll("ʙ", "b")
                .replaceAll("ᴄ", "c")
                .replaceAll("ᴅ", "d")
                .replaceAll("ᴇ", "e")
                .replaceAll("ғ", "f")
                .replaceAll("ɢ", "g")
                .replaceAll("ʜ", "h")
                .replaceAll("ɪ", "i")
                .replaceAll("ᴊ", "j")
                .replaceAll("ᴋ", "k")
                .replaceAll("ʟ", "l")
                .replaceAll("ᴍ", "m")
                .replaceAll("ɴ", "n")
                .replaceAll("ᴏ", "o")
                .replaceAll("ᴘ", "p")
                .replaceAll("ǫ", "q")
                .replaceAll("ʀ", "r")
                .replaceAll("s", "s")
                .replaceAll("ᴛ", "t")
                .replaceAll("ᴜ", "u")
                .replaceAll("ᴠ", "v")
                .replaceAll("ᴡ", "w")
                .replaceAll("x", "x")
                .replaceAll("ʏ", "y")
                .replaceAll("ꔲ", Formatting.DARK_PURPLE + "BULL")
                .replaceAll("ꕒ", Formatting.WHITE + "RABBIT")
                .replaceAll("ꔨ", Formatting.DARK_PURPLE + "DRAGON")
                .replaceAll("ꔶ", Formatting.GOLD + "TIGER")
                .replaceAll("ꕠ", Formatting.YELLOW + "D.HELPER")
                .replaceAll("ꕖ", Formatting.DARK_GRAY + "BUNNY")
                .replaceAll("ꔠ", Formatting.GOLD + "MAGISTER")
                .replaceAll("ꔤ", Formatting.RED + "IMPERATOR")
                .replaceAll("ꕀ", Formatting.DARK_GREEN + "HYDRA")
                .replaceAll("ꕄ", Formatting.DARK_RED + "DRACULA")
                .replaceAll("ꕗ", Formatting.DARK_RED + "D.ADMIN")
                .replaceAll("ꔈ", Formatting.YELLOW + "TITAN")
                .replaceAll("ꕓ", Formatting.GRAY + "GHOST")
                .replaceAll("ꔨ", Formatting.GOLD + "GOD")
                .replaceAll("ꔈ",Formatting.YELLOW + "TITAN")
                .replaceAll("ꕈ", Formatting.GREEN + "COBRA")
                .replaceAll("ꔲ", Formatting.BLUE + "MODER")
                .replaceAll("ꔘ", Formatting.BLUE + "D.ST.MODER")
                .replaceAll("ꔐ", Formatting.BLUE + "D.GL.MODER")
                .replaceAll("ꔷ", Formatting.DARK_RED + "ADMIN")
                .replaceAll("ꔩ", Formatting.BLUE + "Gl Moder")
                .replaceAll("ꔥ", Formatting.DARK_RED + "St.Moder")
                .replaceAll("ꔡ", Formatting.DARK_BLUE + "Moder+")
                .replaceAll("ꔗ", Formatting.BLUE + "Moder")
                .replaceAll("ꔳ", Formatting.AQUA + "Ml.Admin")
                .replaceAll("ꔓ", Formatting.AQUA + "Ml.Moder")
                .replaceAll("ꔒ", Formatting.GREEN + "AVENGER")
                .replaceAll("ꔉ", Formatting.GOLD + "HELPER")
                .replaceAll("ꔖ ", Formatting.AQUA + "OVERLORD ")
                .replaceAll("ꔁ",Formatting.DARK_RED + "Media")
                .replaceAll("ꔦ", Formatting.RED + "D.ML.ADMIN")
                .replaceAll("ꔀ", Formatting.GRAY + "Игрок")
                .replaceAll("ꔀ",Formatting.GRAY + "PLAYER")
                .replaceAll("ꔅ", Formatting.WHITE + "Y" + Formatting.RED + "T")
                .replaceAll("ꔄ", ColorUtils.getUltraSmoothGradient(new Color(21, 140, 204), new Color(85, 255, 255), "HERO")
                        .replaceAll("ᴢ", "z"));
    }
}
