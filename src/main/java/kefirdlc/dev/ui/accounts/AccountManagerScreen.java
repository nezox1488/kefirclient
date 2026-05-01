package kefirdlc.dev.ui.accounts;
// coded by sitoku \\
// since 28.04.2026 \\

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class AccountManagerScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget input;
    private final List<String> accounts = new ArrayList<>();

    private long lastClickTime;
    private int lastClickedIndex = -1;
    private int selectedIndex = -1;
    private String status = "";

    public AccountManagerScreen(Screen parent) {
        super(Text.literal("Accounts"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        accounts.clear();
        accounts.addAll(AccountRepository.load());

        int centerX = this.width / 2;
        int top = 30;

        input = new TextFieldWidget(this.textRenderer, centerX - 122, top, 244, 18, Text.literal("Nickname"));
        input.setMaxLength(16);
        addDrawableChild(input);

        addDrawableChild(ButtonWidget.builder(Text.literal("Добавить"), b -> addAccount(input.getText()))
                .dimensions(centerX - 122, top + 24, 118, 18)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Сгенерировать"), b -> input.setText(generateNick()))
                .dimensions(centerX + 4, top + 24, 118, 18)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Очистить"), b -> {
                    accounts.clear();
                    selectedIndex = -1;
                    persist();
                    status = "Список очищен";
                }).dimensions(centerX - 65, this.height - 26, 130, 18)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), b -> close())
                .dimensions(8, 8, 56, 18)
                .build());
    }

    private void addAccount(String name) {
        String fixed = sanitize(name);
        if (fixed.isEmpty()) return;
        if (!accounts.contains(fixed)) {
            accounts.add(fixed);
            persist();
            status = "Добавлен: " + fixed;
        }
    }

    private void persist() {
        AccountRepository.save(accounts);
    }

    private String sanitize(String name) {
        return name.replaceAll("[^A-Za-z0-9_]", "").trim();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubledClick) {
        if (handleAccountClick(click.x(), click.y(), click.button())) {
            return true;
        }
        return super.mouseClicked(click, doubledClick);
    }

    private boolean handleAccountClick(double mouseX, double mouseY, int button) {
        int startY = 82;
        int rowH = 18;
        int x = this.width / 2 - 136;
        int w = 272;

        for (int i = 0; i < accounts.size(); i++) {
            int y = startY + i * rowH;
            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + rowH - 2) {
                if (button == 1) {
                    String removed = accounts.remove(i);
                    selectedIndex = -1;
                    persist();
                    status = "Удален: " + removed;
                    return true;
                }

                if (button == 0) {
                    long now = System.currentTimeMillis();
                    if (lastClickedIndex == i && now - lastClickTime < 350) {
                        loginAs(accounts.get(i));
                    }
                    lastClickedIndex = i;
                    lastClickTime = now;
                    return true;
                }
            }
        }
        return false;
    }

    private void loginAs(String name) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            Field sessionField = MinecraftClient.class.getDeclaredField("session");
            sessionField.setAccessible(true);

            Object currentSession = sessionField.get(mc);
            Object newSession = buildSessionWithName(currentSession.getClass(), name);
            if (newSession != null) {
                sessionField.set(mc, newSession);
                selectedIndex = accounts.indexOf(name);
                status = "Вход: " + name;
            } else {
                status = "Не удалось сменить сессию";
            }
        } catch (Throwable t) {
            status = "Ошибка смены аккаунта";
        }
    }

    private Object buildSessionWithName(Class<?> sessionClass, String name) {
        for (Constructor<?> constructor : sessionClass.getDeclaredConstructors()) {
            Class<?>[] p = constructor.getParameterTypes();
            if (p.length == 0) continue;
            if (p[0] != String.class) continue;

            try {
                constructor.setAccessible(true);
                Object[] args = new Object[p.length];
                for (int i = 0; i < p.length; i++) {
                    Class<?> type = p[i];
                    if (i == 0) {
                        args[i] = name;
                    } else if (type == String.class) {
                        args[i] = "";
                    } else if (type == UUID.class) {
                        args[i] = UUID.randomUUID();
                    } else if (type == Optional.class) {
                        args[i] = Optional.empty();
                    } else if (type == boolean.class || type == Boolean.class) {
                        args[i] = false;
                    } else if (type.isEnum()) {
                        Object[] constants = type.getEnumConstants();
                        args[i] = constants != null && constants.length > 0 ? constants[0] : null;
                    } else {
                        args[i] = null;
                    }
                }
                return constructor.newInstance(args);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xE00A0A0A);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        drawRounded(context, centerX - 146, 74, centerX + 146, this.height - 34, 0xB0141418);
        drawRoundedOutline(context, centerX - 146, 74, centerX + 146, this.height - 34, 0xFF2A2A33);

        context.drawCenteredTextWithShadow(this.textRenderer, "Accounts", centerX, 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "ЛКМ x2 — войти, ПКМ — удалить", centerX, 64, 0xAAAAAA);

        int startY = 82;
        int rowH = 18;
        int x = centerX - 136;
        int w = 272;

        for (int i = 0; i < accounts.size(); i++) {
            int y = startY + i * rowH;
            boolean selected = i == selectedIndex;
            int bg = selected ? new Color(32, 95, 45, 210).getRGB() : new Color(16, 16, 18, 190).getRGB();
            int border = selected ? new Color(90, 230, 130, 255).getRGB() : new Color(70, 70, 80, 220).getRGB();

            drawRounded(context, x, y, x + w, y + rowH - 2, bg);
            drawRoundedOutline(context, x, y, x + w, y + rowH - 2, border);
            context.drawText(this.textRenderer, accounts.get(i), x + 7, y + 5, 0xFFFFFF, false);
        }

        if (!status.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, status, centerX, this.height - 40, 0xD0D0D0);
        }
    }

    private String generateNick() {
        String[] first = {"Cobra", "Shadow", "Frost", "Viper", "Blaze", "Storm", "Raven", "Aero"};
        String[] second = {"Wither", "Ghost", "Falcon", "Wolf", "Nova", "Blade", "Pixel", "Skye"};
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789_";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        sb.append(first[random.nextInt(first.length)]);
        sb.append(second[random.nextInt(second.length)]);
        sb.append(random.nextInt(90) + 10);
        if (random.nextBoolean()) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.substring(0, Math.min(16, sb.length()));
    }

    private void drawRounded(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1 + 2, y1, x2 - 2, y2, color);
        context.fill(x1, y1 + 2, x2, y2 - 2, color);
    }

    private void drawRoundedOutline(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1 + 2, y1, x2 - 2, y1 + 1, color);
        context.fill(x1 + 2, y2 - 1, x2 - 2, y2, color);
        context.fill(x1, y1 + 2, x1 + 1, y2 - 2, color);
        context.fill(x2 - 1, y1 + 2, x2, y2 - 2, color);
    }
}
