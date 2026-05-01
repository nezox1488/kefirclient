package kefirdlc.dev.util.render.core;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.util.render.backends.gl.GlBackend;

final class ShapeBatcher {
    private final GlBackend backend;

    ShapeBatcher(GlBackend backend) {
        this.backend = backend;
    }

    void enqueueRect(float x, float y, float w, float h,
                     float roundTopLeft, float roundTopRight,
                     float roundBottomRight, float roundBottomLeft,
                     int color, float[] transform) {
        backend.enqueueRect(x, y, w, h,
                roundTopLeft, roundTopRight, roundBottomRight, roundBottomLeft,
                color, transform);
    }

    void enqueueRectOutline(float x, float y, float w, float h,
                            float roundTopLeft, float roundTopRight,
                            float roundBottomRight, float roundBottomLeft,
                            int color, float thickness, float[] transform) {
        backend.enqueueRectOutline(x, y, w, h,
                roundTopLeft, roundTopRight, roundBottomRight, roundBottomLeft,
                color, thickness, transform);
    }

    void enqueueGradient(float x, float y, float w, float h,
                         float roundTopLeft, float roundTopRight,
                         float roundBottomRight, float roundBottomLeft,
                         int c00, int c10, int c11, int c01, float[] transform) {
        backend.enqueueGradient(x, y, w, h,
                roundTopLeft, roundTopRight, roundBottomRight, roundBottomLeft,
                c00, c10, c11, c01, transform);
    }

    void enqueueCircle(float cx, float cy, float radius, float startDeg, float pct, int color, float[] transform) {
        backend.enqueueCircle(cx, cy, radius, startDeg, pct, color, transform);
    }

    void flush() {
        backend.flush();
    }
}
