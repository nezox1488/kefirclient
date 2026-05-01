package kefirdlc.dev.util.render.utils;
// coded by sitoku \\
// since 27.04.2026 \\


public class Color {

    public int interpolateRGB(int start, int end, double factor) {
        if (factor < 0.0) factor = 0.0;
        if (factor > 1.0) factor = 1.0;

        int a1 = (start >> 24) & 0xFF;
        int r1 = (start >> 16) & 0xFF;
        int g1 = (start >> 8) & 0xFF;
        int b1 = start & 0xFF;

        int a2 = (end >> 24) & 0xFF;
        int r2 = (end >> 16) & 0xFF;
        int g2 = (end >> 8) & 0xFF;
        int b2 = end & 0xFF;

        if (a1 == 0) a1 = 255;
        if (a2 == 0) a2 = 255;

        int r = (int) Math.round(r1 + (r2 - r1) * factor);
        int g = (int) Math.round(g1 + (g2 - g1) * factor);
        int b = (int) Math.round(b1 + (b2 - b1) * factor);
        int a = (int) Math.round(a1 + (a2 - a1) * factor);

        return getRGB(r, g, b, a);
    }

    public int getRGB(int r, int g, int b) {
        return getRGB(r, g, b, 255);
    }


    public static int getRGB(int hex, double alpha) {
        int a = (int) Math.round(alpha * 255);
        int rgb = hex & 0x00FFFFFF;
        return (a << 24) | rgb;
    }

    public int getRGB(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int fromRgba(int r, int g, int b, int a) {
        return new Color().getRGB(r, g, b, a);
    }

    public static int alpha(int rgba) {
        return (rgba >>> 24) & 0xFF;
    }

    public static int red(int rgba) {
        return (rgba >>> 16) & 0xFF;
    }

    public static int green(int rgba) {
        return (rgba >>> 8) & 0xFF;
    }

    public static int blue(int rgba) {
        return rgba & 0xFF;
    }

    public static int lerp(int startColor, int endColor, float t) {
        float clamped = t;
        if (clamped <= 0.0f) {
            return startColor;
        }
        if (clamped >= 1.0f) {
            return endColor;
        }
        int a1 = (startColor >>> 24) & 0xFF;
        int r1 = (startColor >>> 16) & 0xFF;
        int g1 = (startColor >>> 8) & 0xFF;
        int b1 = startColor & 0xFF;

        int a2 = (endColor >>> 24) & 0xFF;
        int r2 = (endColor >>> 16) & 0xFF;
        int g2 = (endColor >>> 8) & 0xFF;
        int b2 = endColor & 0xFF;

        int a = Math.round(a1 + (a2 - a1) * clamped);
        int r = Math.round(r1 + (r2 - r1) * clamped);
        int g = Math.round(g1 + (g2 - g1) * clamped);
        int b = Math.round(b1 + (b2 - b1) * clamped);

        return ((a & 0xFF) << 24)
                | ((r & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | (b & 0xFF);
    }

}
