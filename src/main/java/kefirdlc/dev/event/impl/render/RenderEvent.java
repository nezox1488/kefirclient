package kefirdlc.dev.event.impl.render;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.event.api.Event;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;
import net.minecraft.client.MinecraftClient;

import java.util.Objects;

public final class RenderEvent extends Event {

    private final MinecraftClient client;
    private final Renderer2D renderer;
    private final FontObject defaultFont;
    private final int viewportWidth;
    private final int viewportHeight;

    public RenderEvent(
            MinecraftClient client,
            Renderer2D renderer,
            FontObject defaultFont,
            int viewportWidth,
            int viewportHeight
    ) {
        this.client = Objects.requireNonNull(client, "client");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.defaultFont = Objects.requireNonNull(defaultFont, "defaultFont");
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public MinecraftClient client() {
        return client;
    }

    public Renderer2D renderer() {
        return renderer;
    }

    public FontObject defaultFont() {
        return defaultFont;
    }

    public int viewportWidth() {
        return viewportWidth;
    }

    public int viewportHeight() {
        return viewportHeight;
    }
}
