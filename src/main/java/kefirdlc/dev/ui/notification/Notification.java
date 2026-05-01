package kefirdlc.dev.ui.notification;
// coded by sitoku \\
// since 27.04.2026 \\

import lombok.Getter;

@Getter
public class Notification {
    private final String title;
    private final String message;
    private final NotificationType type;

    private final long startTime;
    private final float maxTime;

    private float animationProgress = 0f;
    private boolean isExiting = false;


    private static final float ANIM_SPEED = 0.03f;

    public Notification(String title, String message, NotificationType type) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.startTime = System.currentTimeMillis();
        this.maxTime = 900;
    }

    public void update() {
        long timeAlive = System.currentTimeMillis() - startTime;

        if (timeAlive > maxTime) {
            isExiting = true;
        }

        if (isExiting) {

            animationProgress -= ANIM_SPEED;
        } else {

            animationProgress += ANIM_SPEED;
        }

        if (animationProgress > 1f) animationProgress = 1f;
        if (animationProgress < 0f) animationProgress = 0f;
    }

    public void forceExit() {
        this.isExiting = true;

    }

    public boolean shouldRemove() {
        return isExiting && animationProgress <= 0f;
    }
}