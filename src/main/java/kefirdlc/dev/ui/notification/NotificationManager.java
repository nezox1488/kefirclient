package kefirdlc.dev.ui.notification;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.util.render.core.Renderer2D;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

import kefirdlc.dev.util.render.text.FontRegistry;
import net.minecraft.client.MinecraftClient;

public class NotificationManager {

    private static final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();
    private static final int MAX_NOTIFICATIONS = 5;

    public static void add(String title, String message, NotificationType type) {
        for (Notification n : notifications) {
            if (n.getTitle().equalsIgnoreCase(title) && !n.isExiting()) {

                n.forceExit();
            }
        }

        notifications.add(new Notification(title, message, type));
    }

    public static void render(Renderer2D renderer) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getWindow() == null) return;

        float screenWidth = mc.getWindow().getScaledWidth();
        float screenHeight = mc.getWindow().getScaledHeight();

        float bottomOffset = 50;
        float itemHeight = 28;
        float padding = 6;


        int stackIndex = 0;


        if (notifications.size() > MAX_NOTIFICATIONS * 2) {
            notifications.remove(0);
        }

        for (Notification n : notifications) {
            n.update();


            if (n.shouldRemove()) {
                notifications.remove(n);
                continue;
            }


            var fontTitle = FontRegistry.INTER_MEDIUM;
            var fontDesc = FontRegistry.SF_REGULAR;

            String fullText = n.getTitle() + " " + n.getMessage();
            float width = Math.max(120, renderer.getStringWidth(fontDesc, fullText, 8) + 30);


            float animVal = n.getAnimationProgress();

            float eased = easeOutBack(animVal);

            float targetX = screenWidth - width - 10;


            float stackY;
            if (!n.isExiting()) {

                stackY = screenHeight - bottomOffset - (itemHeight + padding) * stackIndex;
                stackIndex++;
            } else {

                stackY = screenHeight - bottomOffset - (itemHeight + padding) * stackIndex - 15;
            }

            float renderY = stackY;


            if (!n.isExiting()) {
                renderY += (1f - eased) * 30;
            } else {

                renderY -= (1f - animVal) * 45;
            }

            renderer.pushAlpha(animVal);


            float scale = n.isExiting()
                    ? 1f - (1f - animVal) * 0.3f
                    : 0.5f + 0.5f * eased;

               renderer.pushScale(scale, scale, targetX + width / 2f, renderY + itemHeight / 2f);

            int bg = new Color(15, 15, 20, 240).getRGB();
            int accent = getColor(n.getType());

            renderer.shadow(targetX, renderY, width, itemHeight, 12, 2f, 1f, new Color(0,0,0, 100).getRGB());
            renderer.rect(targetX, renderY, width, itemHeight, 5, bg);

            renderer.rect(targetX, renderY + 4, 2, itemHeight - 8, 1, accent);
            renderer.circle(targetX + 12, renderY + 14, 3, 1, 1, accent);

            renderer.text(fontTitle, targetX + 22, renderY + 9, 8, n.getTitle(), -1, "l");
            renderer.text(fontDesc, targetX + 22, renderY + 19, 7, n.getMessage(), new Color(180, 180, 180).getRGB(), "l");

            renderer.popScale();
            renderer.popAlpha();
        }
    }

    private static int getColor(NotificationType type) {
        return switch (type) {
            case SUCCESS -> new Color(100, 255, 140).getRGB();
            case DISABLE, ERROR -> new Color(255, 90, 90).getRGB();
            case INFO -> new Color(100, 160, 255).getRGB();
            case WARNING -> new Color(255, 200, 80).getRGB();
        };
    }

    private static float easeOutBack(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(x - 1, 3) + c1 * (float) Math.pow(x - 1, 2);
    }
}