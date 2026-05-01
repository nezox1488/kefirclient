package kefirdlc.dev.util.render.core;
// coded by sitoku \\
// since 27.04.2026 \\

/**
 * Collects renderer frame statistics for debugging overlays and diagnostics.
 * The tracker records draw calls and timing for the UI renderer so modules can
 * display averaged performance metrics without touching low-level GL state.
 */
public final class RenderFrameMetrics {

    private static final RenderFrameMetrics INSTANCE = new RenderFrameMetrics();

    private final Object lock = new Object();

    private long frameStartNanos = 0L;
    private long lastFrameDurationNanos = 0L;
    private int currentDrawCalls = 0;
    private int currentTriangles = 0;
    private int lastFrameDrawCalls = 0;
    private int lastFrameTriangles = 0;

    private RenderFrameMetrics() {
    }

    public static RenderFrameMetrics getInstance() {
        return INSTANCE;
    }

    /**
     * Marks the beginning of a new renderer frame and resets accumulators.
     *
     * @param width  viewport width in pixels (unused for now but validated)
     * @param height viewport height in pixels (unused for now but validated)
     */
    public void beginFrame(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        long now = System.nanoTime();
        synchronized (lock) {
            frameStartNanos = now;
            currentDrawCalls = 0;
            currentTriangles = 0;
        }
    }

    /**
     * Records a single draw call for the active frame.
     *
     * @param triangleCount number of triangles emitted by the draw call
     */
    public void recordDrawCall(int triangleCount) {
        if (triangleCount < 0) {
            triangleCount = 0;
        }
        synchronized (lock) {
            currentDrawCalls++;
            currentTriangles += triangleCount;
        }
    }

    /**
     * Marks the end of the active renderer frame and stores the collected metrics.
     */
    public void endFrame() {
        long now = System.nanoTime();
        synchronized (lock) {
            if (frameStartNanos <= 0L) {
                frameStartNanos = now;
                lastFrameDurationNanos = 0L;
                lastFrameDrawCalls = currentDrawCalls;
                lastFrameTriangles = currentTriangles;
                currentDrawCalls = 0;
                currentTriangles = 0;
                return;
            }
            lastFrameDurationNanos = Math.max(0L, now - frameStartNanos);
            lastFrameDrawCalls = currentDrawCalls;
            lastFrameTriangles = currentTriangles;
            frameStartNanos = now;
            currentDrawCalls = 0;
            currentTriangles = 0;
        }
    }

    /**
     * Returns a snapshot of the most recently completed frame metrics.
     */
    public FrameMetricsSnapshot snapshot() {
        synchronized (lock) {
            return new FrameMetricsSnapshot(lastFrameDurationNanos, lastFrameDrawCalls, lastFrameTriangles);
        }
    }

    public record FrameMetricsSnapshot(long frameDurationNanos, int drawCalls, int triangles) {
        public FrameMetricsSnapshot {
            frameDurationNanos = Math.max(0L, frameDurationNanos);
            drawCalls = Math.max(0, drawCalls);
            triangles = Math.max(0, triangles);
        }

        public double frameTimeMillis() {
            return frameDurationNanos / 1_000_000.0;
        }

        public double framesPerSecond() {
            return frameDurationNanos > 0L ? 1_000_000_000.0 / frameDurationNanos : 0.0;
        }
    }
}
