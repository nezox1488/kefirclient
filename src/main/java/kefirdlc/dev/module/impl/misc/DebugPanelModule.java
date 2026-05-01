package kefirdlc.dev.module.impl.misc;
// coded by sitoku \\
// since 27.04.2026 \\

import com.google.common.eventbus.Subscribe;
import kefirdlc.dev.event.impl.render.RenderEvent;
import kefirdlc.dev.module.api.Category;
import kefirdlc.dev.module.api.Function;
import kefirdlc.dev.module.api.ModuleInfo;
import kefirdlc.dev.util.render.core.RenderFrameMetrics;
import kefirdlc.dev.util.render.core.Renderer2D;
import kefirdlc.dev.util.render.text.FontObject;
import kefirdlc.dev.util.render.text.FontRegistry;
import kefirdlc.dev.util.render.utils.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@ModuleInfo(name = "DebugPanel", category = Category.MISC)
public final class DebugPanelModule extends Function {

    private static final double MIN_UPDATE_INTERVAL_SECONDS = 0.2d;
    private static final double MAX_UPDATE_INTERVAL_SECONDS = 3.0d;
    private static final double DEFAULT_UPDATE_INTERVAL_SECONDS = 0.6d;
    private static final double UPDATE_INTERVAL_STEP_SECONDS = 0.1d;

    private static final float PANEL_MARGIN = 12f;
    private static final float PANEL_WIDTH = 240f;
    private static final float PANEL_PADDING = 12f;
    private static final float PANEL_CORNER_RADIUS = 10f;
    private static final float PANEL_BORDER_THICKNESS = 1.25f;
    private static final float HEADER_TEXT_SIZE = 15f;
    private static final float ENTRY_TEXT_SIZE = 13f;

    private static final float ENTRY_SPACING = 4f;

    private static final int PANEL_BACKGROUND_COLOR = Color.getRGB(0x121010, 0.88);

    private static final int LABEL_TEXT_COLOR = Color.getRGB(0xCBD5E1, 0.94);

    private static final DecimalFormat FPS_FORMAT = decimalFormat("0.0");
    private static final DecimalFormat TIME_FORMAT = decimalFormat("0.00");
    private static final DecimalFormat INTEGER_FORMAT = decimalFormat("0");
    private static final DecimalFormat MEMORY_FORMAT = decimalFormat("0.0");
    private static final String DRAG_HANDLE_ID = "module.debug_panel";

    private AutoCloseable renderSubscription;
    private long lastSampleNanos = 0L;
    private int framesAccumulated = 0;
    private int drawCallsAccumulated = 0;
    private int trianglesAccumulated = 0;
    private double fpsAccumulated = 0.0d;
    private int fpsSamples = 0;

    private double displayedFps = 0.0;
    private double displayedFrameTimeMs = 0.0;
    private double displayedDrawCalls = 0.0;
    private double displayedTriangles = 0.0;
    private int displayedLatencyMs = -1;
    private double displayedMemoryUsedMb = 0.0;
    private double displayedMemoryMaxMb = 0.0;

    private float panelX = PANEL_MARGIN;
    private float panelY = PANEL_MARGIN;

    public DebugPanelModule() {
        setKey(GLFW.GLFW_KEY_RIGHT_ALT);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        resetState();
        unsubscribe();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        unsubscribe();
    }

    @Subscribe
    public void handleRenderEvent(RenderEvent event) {
        Objects.requireNonNull(event, "event");
        updateMetrics(event);
        renderPanel(event);
    }

    private void renderPanel(RenderEvent event) {
        Renderer2D renderer = event.renderer();
        FontObject headerFont = FontRegistry.INTER_SEMIBOLD;
        FontObject entryFont = FontRegistry.INTER_MEDIUM;

        float headerHeight = renderer.measureText(headerFont, "Ag", HEADER_TEXT_SIZE).height();
        float entryHeight = renderer.measureText(entryFont, "Ag", ENTRY_TEXT_SIZE).height();

        MetricEntry[] entries = buildEntries(event);
        float contentHeight = 0;
        if (entries.length > 0) {
            contentHeight += entryHeight * entries.length;
            contentHeight += ENTRY_SPACING * (entries.length - 1);
        }
        float panelHeight = PANEL_PADDING * 2f + contentHeight;

        this.panelX = 25;
        this.panelY = 25;

        float panelX = this.panelX;
        float panelY = this.panelY;

        float innerX = panelX + PANEL_BORDER_THICKNESS;
        float innerY = panelY + PANEL_BORDER_THICKNESS;
        float innerWidth = PANEL_WIDTH - PANEL_BORDER_THICKNESS * 2f;
        float innerHeight = panelHeight - PANEL_BORDER_THICKNESS * 2f;
        float innerRadius = Math.max(0f, PANEL_CORNER_RADIUS - PANEL_BORDER_THICKNESS);
        if (innerWidth > 0f && innerHeight > 0f) {
            renderer.rect(innerX, innerY, innerWidth, innerHeight, innerRadius, PANEL_BACKGROUND_COLOR);
        }

        float textX = panelX + PANEL_PADDING;
        float valueX = panelX + PANEL_WIDTH - PANEL_PADDING;
        float entryBaseline = panelY + PANEL_PADDING + entryHeight * 0.8f;
        int valueColor = resolveValueTextColor();
        for (MetricEntry entry : entries) {
            renderer.text(entryFont, textX, entryBaseline, ENTRY_TEXT_SIZE, entry.label(), LABEL_TEXT_COLOR, "l");
            renderer.text(headerFont, valueX, entryBaseline, ENTRY_TEXT_SIZE, entry.value(), valueColor, "r");
            entryBaseline += entryHeight + ENTRY_SPACING;
        }
    }

    private MetricEntry[] buildEntries(RenderEvent event) {
        String fpsValue = FPS_FORMAT.format(Math.max(0.0, displayedFps));
        String frameTimeValue = TIME_FORMAT.format(Math.max(0.0, displayedFrameTimeMs)) + " ms";
        String drawCallValue = FPS_FORMAT.format(Math.max(0.0, displayedDrawCalls));
        String triangleValue = INTEGER_FORMAT.format(Math.max(0.0, Math.round(displayedTriangles)));
        String latencyValue = displayedLatencyMs >= 0 ? displayedLatencyMs + " ms" : "N/A";
        String memoryValue = MEMORY_FORMAT.format(Math.max(0.0, displayedMemoryUsedMb))
                + " / "
                + MEMORY_FORMAT.format(Math.max(0.0, displayedMemoryMaxMb))
                + " MB";
        String resolutionValue = event.viewportWidth() + "×" + event.viewportHeight();

        return new MetricEntry[]{
                new MetricEntry("FPS", fpsValue),
                new MetricEntry("Frame Time", frameTimeValue),
                new MetricEntry("Draw Calls", drawCallValue),
                new MetricEntry("Triangles", triangleValue),
                new MetricEntry("Latency", latencyValue),
                new MetricEntry("Memory", memoryValue),
                new MetricEntry("Resolution", resolutionValue)
        };
    }

    private void updateMetrics(RenderEvent event) {
        RenderFrameMetrics.FrameMetricsSnapshot snapshot = RenderFrameMetrics.getInstance().snapshot();
        MinecraftClient client = event.client();
        framesAccumulated++;
        drawCallsAccumulated += snapshot.drawCalls();
        trianglesAccumulated += snapshot.triangles();
        double fpsSample = Math.max(0.0d, client.getCurrentFps());
        fpsAccumulated += fpsSample;
        fpsSamples++;

        long now = System.nanoTime();
        if (lastSampleNanos == 0L) {
            lastSampleNanos = now;
        }
        long updateInterval = resolveUpdateIntervalNanos();
        long elapsed = now - lastSampleNanos;
        if (elapsed < updateInterval && framesAccumulated > 0) {
            return;
        }

        double averageFps = fpsSamples > 0 ? fpsAccumulated / fpsSamples : fpsSample;
        if (averageFps < 0.0) {
            averageFps = 0.0;
        }
        displayedFps = averageFps;
        displayedFrameTimeMs = averageFps > 0.0 ? 1000.0 / averageFps : 0.0;
        displayedDrawCalls = framesAccumulated > 0
                ? (double) drawCallsAccumulated / framesAccumulated
                : snapshot.drawCalls();
        displayedTriangles = framesAccumulated > 0
                ? (double) trianglesAccumulated / framesAccumulated
                : snapshot.triangles();
        displayedLatencyMs = resolveLatency(client);
        updateMemoryStats();

        lastSampleNanos = now;
        framesAccumulated = 0;
        drawCallsAccumulated = 0;
        trianglesAccumulated = 0;
        fpsAccumulated = 0.0d;
        fpsSamples = 0;
    }

    private void updateMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long usedBytes = runtime.totalMemory() - runtime.freeMemory();
        long maxBytes = runtime.maxMemory();
        displayedMemoryUsedMb = bytesToMegabytes(usedBytes);
        displayedMemoryMaxMb = bytesToMegabytes(maxBytes);
    }

    private long resolveUpdateIntervalNanos() {
        double seconds = 1.5;
        seconds = Math.max(MIN_UPDATE_INTERVAL_SECONDS, Math.min(MAX_UPDATE_INTERVAL_SECONDS, seconds));
        long millis = Math.max(1L, Math.round(seconds * 1000.0));
        return TimeUnit.MILLISECONDS.toNanos(millis);
    }

    private int resolveLatency(MinecraftClient client) {
        if (client == null || client.player == null) {
            return -1;
        }
        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (networkHandler == null) {
            return -1;
        }
        PlayerListEntry entry = networkHandler.getPlayerListEntry(client.player.getUuid());
        if (entry == null) {
            return -1;
        }
        return entry.getLatency();
    }

    private void resetState() {
        lastSampleNanos = 0L;
        framesAccumulated = 0;
        drawCallsAccumulated = 0;
        trianglesAccumulated = 0;
        displayedLatencyMs = -1;
        displayedFps = 0.0;
        displayedFrameTimeMs = 0.0;
        displayedDrawCalls = 0.0;
        displayedTriangles = 0.0;
        fpsAccumulated = 0.0d;
        fpsSamples = 0;
        updateMemoryStats();
        panelX = PANEL_MARGIN;
        panelY = PANEL_MARGIN;
    }

    private void unsubscribe() {
        if (renderSubscription == null) {
            return;
        }
        try {
            renderSubscription.close();
        } catch (Exception ignored) {
        }
        renderSubscription = null;
    }

    private static int resolveValueTextColor() {
        return java.awt.Color.ORANGE.getRGB();
    }

    private static DecimalFormat decimalFormat(String pattern) {
        NumberFormat base = NumberFormat.getNumberInstance(Locale.US);
        if (!(base instanceof DecimalFormat format)) {
            DecimalFormat fallback = new DecimalFormat(pattern);
            fallback.setRoundingMode(java.math.RoundingMode.HALF_UP);
            return fallback;
        }
        format.setRoundingMode(java.math.RoundingMode.HALF_UP);
        format.applyPattern(pattern);
        return format;
    }

    private static double bytesToMegabytes(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }

    private record MetricEntry(String label, String value) {
    }

}