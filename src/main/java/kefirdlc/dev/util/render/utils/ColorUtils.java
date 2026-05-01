package kefirdlc.dev.util.render.utils;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.util.wrapper.Wrapper;
import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector4i;

import java.awt.Color;
import java.util.concurrent.*;
import java.util.regex.Pattern;

@UtilityClass
public class ColorUtils implements Wrapper {

    /**
     * Считывает цвет с экрана по координатам (пикер цвета)
     */


    /**
     * Преобразует цвет в формате ARGB (int) в массив [R, G, B, A] в долях от 1.0 (0.0 - 1.0)
     */
    public static float[] getRGBa(final int color) {
        return new float[]{
                (color >> 16 & 0xFF) / 255f,
                (color >> 8 & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                (color >> 24 & 0xFF) / 255f
        };
    }

    public static int rgba(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }
    private final long CACHE_EXPIRATION_TIME = 60 * 1000;
    private final ConcurrentHashMap<ColorKey, CacheEntry> colorCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);
    private final DelayQueue<CacheEntry> cleanupQueue = new DelayQueue<>();
    public static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9a-f-or]");
    public Char2IntArrayMap colorCodes = new Char2IntArrayMap() {{
        put('0', 0x000000);
        put('1', 0x0000AA);
        put('2', 0x00AA00);
        put('3', 0x00AAAA);
        put('4', 0xAA0000);
        put('5', 0xAA00AA);
        put('6', 0xFFAA00);
        put('7', 0xAAAAAA);
        put('8', 0x555555);
        put('9', 0x5555FF);
        put('A', 0x55FF55);
        put('B', 0x55FFFF);
        put('C', 0xFF5555);
        put('D', 0xFF55FF);
        put('E', 0xFFFF55);
        put('F', 0xFFFFFF);
    }};

    static {
        cacheCleaner.scheduleWithFixedDelay(() -> {
            CacheEntry entry = cleanupQueue.poll();
            while (entry != null) {
                if (entry.isExpired()) {
                    colorCache.remove(entry.getKey());
                }
                entry = cleanupQueue.poll();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public final int RED = getColor(255, 0, 0);
    public final int GREEN = getColor(0, 255, 0);
    public final int BLUE = getColor(0, 0, 255);
    public final int YELLOW = getColor(255, 255, 0);
    public final int WHITE = getColor(255);
    public final int BLACK = getColor(0);
    public final int HALF_BLACK = getColor(0,0.5F);
    public final int LIGHT_RED = getColor(255, 85, 85);

    public int red(int c) {return (c >> 16) & 0xFF;}

    public int green(int c) {
        return (c >> 8) & 0xFF;
    }

    public int blue(int c) {
        return c & 0xFF;
    }

    public int alpha(int c) {
        return (c >> 24) & 0xFF;
    }

    public float redf(int c) {
        return red(c) / 255.0f;
    }

    public float greenf(int c) {
        return green(c) / 255.0f;
    }

    public float bluef(int c) {
        return blue(c) / 255.0f;
    }

    public float alphaf(int c) {
        return alpha(c) / 255.0f;
    }

    public int[] getRGBA(int c) {
        return new int[]{red(c), green(c), blue(c), alpha(c)};
    }

    public int[] getRGB(int c) {
        return new int[]{red(c), green(c), blue(c)};
    }

    public float[] getRGBAf(int c) {
        return new float[]{redf(c), greenf(c), bluef(c), alphaf(c)};
    }

    public float[] getRGBf(int c) {
        return new float[]{redf(c), greenf(c), bluef(c)};
    }

    public int getColor(float red, float green, float blue, float alpha) {
        return getColor(Math.round(red * 255), Math.round(green * 255), Math.round(blue * 255), Math.round(alpha * 255));
    }

    public int getColor(int red, int green, int blue, float alpha) {
        return getColor(red, green, blue, Math.round(alpha * 255));
    }

    public int getColor(float red, float green, float blue) {
        return getColor(red, green, blue, 1.0F);
    }

    public int getColor(int brightness, int alpha) {
        return getColor(brightness, brightness, brightness, alpha);
    }

    public int getColor(int brightness, float alpha) {
        return getColor(brightness, Math.round(alpha * 255));
    }

    public int getColor(int brightness) {
        return getColor(brightness, brightness, brightness);
    }

    public int replAlpha(int color, int alpha) {
        return getColor(red(color), green(color), blue(color), alpha);
    }

    public int replAlpha(int color, float alpha) {
        return getColor(red(color), green(color), blue(color), alpha);
    }

    public int multAlpha(int color, float percent01) {
        return getColor(red(color), green(color), blue(color), Math.round(alpha(color) * percent01));
    }

    public int multColor(int colorStart, int colorEnd, float progress) {
        return getColor(Math.round(red(colorStart) * (redf(colorEnd) * progress)), Math.round(green(colorStart) * (greenf(colorEnd) * progress)),
                Math.round(blue(colorStart) * (bluef(colorEnd) * progress)), Math.round(alpha(colorStart) * (alphaf(colorEnd) * progress)));
    }

    public int multRed(int colorStart, int colorEnd, float progress) {
        return getColor(Math.round(red(colorStart) * (redf(colorEnd) * progress)), Math.round(green(colorStart) * (greenf(colorEnd) * progress)),
                Math.round(blue(colorStart) * (bluef(colorEnd) * progress)), Math.round(alpha(colorStart) * (alphaf(colorEnd) * progress)));
    }

    public int multDark(int color, float percent01) {
        return getColor(
                Math.round(red(color) * percent01),
                Math.round(green(color) * percent01),
                Math.round(blue(color) * percent01),
                alpha(color)
        );
    }

    public int multBright(int color, float percent01) {
        return getColor(
                Math.min(255, Math.round(red(color) / percent01)),
                Math.min(255, Math.round(green(color) / percent01)),
                Math.min(255, Math.round(blue(color) / percent01)),
                alpha(color)
        );
    }

    public int overCol(int color1, int color2, float percent01) {
        final float percent = MathHelper.clamp(percent01, 0F, 1F);
        return getColor(
                MathHelper.lerp(percent, red(color1), red(color2)),
                MathHelper.lerp(percent, green(color1), green(color2)),
                MathHelper.lerp(percent, blue(color1), blue(color2)),
                MathHelper.lerp(percent, alpha(color1), alpha(color2))
        );
    }

    public Vector4i multRedAndAlpha(Vector4i color, float red, float alpha) {
        return new Vector4i(multRedAndAlpha(color.x, red, alpha), multRedAndAlpha(color.y, red, alpha), multRedAndAlpha(color.w, red, alpha), multRedAndAlpha(color.z, red, alpha));
    }

    public int multRedAndAlpha(int color, float red, float alpha) {
        return getColor(red(color),Math.min(255, Math.round(green(color) / red)), Math.min(255, Math.round(blue(color) / red)), Math.round(alpha(color) * alpha));
    }

    public int multRed(int color, float percent01) {
        return getColor(red(color),Math.min(255, Math.round(green(color) / percent01)), Math.min(255, Math.round(blue(color) / percent01)), alpha(color));
    }

    public int multGreen(int color, float percent01) {
        return getColor(Math.min(255, Math.round(green(color) / percent01)), green(color), Math.min(255, Math.round(blue(color) / percent01)), alpha(color));
    }
    public static int pack(int red, int green, int blue, int alpha) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | ((blue & 0xFF) << 0);
    }


    public int[] genGradientForText(int color1, int color2, int length) {
        int[] gradient = new int[length];
        for (int i = 0; i < length; i++) {
            float pc = (float) i / (length - 1);
            gradient[i] = overCol(color1, color2, pc);
        }
        return gradient;
    }

    public static float[] normalize(java.awt.Color color) {
        return new float[] {color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f};
    }

    public static float[] normalize(int color) {
        int[] components = unpack(color);
        return new float[] {components[0] / 255.0f, components[1] / 255.0f, components[2] / 255.0f, components[3] / 255.0f};
    }


    public static int applyAlpha(int color, float alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return pack(r, g, b, (int) (255 * alpha));
    }
    public static int[] unpack(int color) {
        return new int[] {color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF};
    }


    public int rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        int color = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
        return getColor(red(color), green(color), blue(color), Math.round(opacity * 255));
    }

    public int fade(int index) {
        java.awt.Color clientColor = new java.awt.Color(getClientColor());
        return fade(8, index, clientColor.brighter().getRGB(), clientColor.darker().getRGB());
    }

    /**
     * Fade анимация между двумя выбранными цветами
     * @param speed скорость анимации
     * @param index индекс для смещения анимации
     * @param color1 первый цвет
     * @param color2 второй цвет
     * @return анимированный цвет
     */
    public int fade(int speed, int index, int color1, int color2) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = angle >= 180 ? 360 - angle : angle;
        return overCol(color1, color2, angle / 180f);
    }

    /**
     * Fade анимация между двумя выбранными цветами с дефолтной скоростью
     * @param index индекс для смещения анимации
     * @param color1 первый цвет
     * @param color2 второй цвет
     * @return анимированный цвет
     */
    public int fade(int index, int color1, int color2) {
        return fade(8, index, color1, color2);
    }

    /**
     * Fade анимация между двумя выбранными цветами (Color объекты)
     * @param speed скорость анимации
     * @param index индекс для смещения анимации
     * @param color1 первый цвет
     * @param color2 второй цвет
     * @return анимированный цвет
     */
    public int fade(int speed, int index, java.awt.Color color1, java.awt.Color color2) {
        return fade(speed, index, color1.getRGB(), color2.getRGB());
    }

    /**
     * Fade анимация между двумя выбранными цветами (Color объекты) с дефолтной скоростью
     * @param index индекс для смещения анимации
     * @param color1 первый цвет
     * @param color2 второй цвет
     * @return анимированный цвет
     */
    public int fade(int index, java.awt.Color color1, java.awt.Color color2) {
        return fade(8, index, color1.getRGB(), color2.getRGB());
    }

    public Vector4i roundClientColor(float alpha) {
        return new Vector4i(ColorUtils.multAlpha(ColorUtils.fade(270), alpha), ColorUtils.multAlpha(ColorUtils.fade(0), alpha),
                ColorUtils.multAlpha(ColorUtils.fade(180), alpha), ColorUtils.multAlpha(ColorUtils.fade(90), alpha));
    }

    public int getColor(int red, int green, int blue, int alpha) {
        ColorKey key = new ColorKey(red, green, blue, alpha);
        CacheEntry cacheEntry = colorCache.computeIfAbsent(key, k -> {
            CacheEntry newEntry = new CacheEntry(k, computeColor(red, green, blue, alpha), CACHE_EXPIRATION_TIME);
            cleanupQueue.offer(newEntry);
            return newEntry;
        });
        return cacheEntry.getColor();
    }

    public int getColor(int red, int green, int blue) {
        return getColor(red, green, blue, 255);
    }

    private int computeColor(int red, int green, int blue, int alpha) {
        return ((MathHelper.clamp(alpha, 0, 255) << 24) |
                (MathHelper.clamp(red, 0, 255) << 16) |
                (MathHelper.clamp(green, 0, 255) << 8) |
                MathHelper.clamp(blue, 0, 255));
    }

    private String generateKey(int red, int green, int blue, int alpha) {
        return red + "," + green + "," + blue + "," + alpha;
    }

    public String formatting(int color) {
        return "⏏" + color + "⏏";
    }

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class ColorKey {
        final int red, green, blue, alpha;
    }

    @Getter
    private static class CacheEntry implements Delayed {
        private final ColorKey key;
        private final int color;
        private final long expirationTime;

        CacheEntry(ColorKey key, int color, long ttl) {
            this.key = key;
            this.color = color;
            this.expirationTime = System.currentTimeMillis() + ttl;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long delay = expirationTime - System.currentTimeMillis();
            return unit.convert(delay, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            if (other instanceof CacheEntry) {
                return Long.compare(this.expirationTime, ((CacheEntry) other).expirationTime);
            }
            return 0;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }

    }

    public String removeFormatting(String text) {
        return text == null || text.isEmpty() ? null : FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
    }

    public int getMainGuiColor() {return new java.awt.Color(0x18181D).getRGB();}
    public int getGuiRectColor(float alpha) {return multAlpha(new java.awt.Color(0x1A1A1F).getRGB(),alpha);}
    public int getGuiRectColor2(float alpha) {return multAlpha(new java.awt.Color(0x1E1E26).getRGB(),alpha);}

    public int getRect(float alpha) {return multAlpha(new java.awt.Color(0x18181C).getRGB(),alpha);}

    public int getRectDarker(float alpha) {
        return multAlpha(new java.awt.Color(0x18181E).getRGB(),alpha);
    }

    public int getText(float alpha) {return multAlpha(getText(),alpha);}

    public int getText() {return new java.awt.Color(0xE6E6E6).getRGB();}

    public int getClientColor() {
        return new java.awt.Color(0x1C74E6).getRGB();
    }

    public int getClientColor(float alpha) {
        return multAlpha(getClientColor(),alpha);
    }

    public int getFriendColor() {
        return new java.awt.Color(0x55FF55).getRGB();
    }

    public int getOutline(float alpha, float bright) {return multBright(multAlpha(getOutline(),alpha),bright);}

    public int getOutline(float alpha) {return multAlpha(getOutline(), alpha);}

    public int getOutline() {
        return new java.awt.Color(0x373746).getRGB();
    }



    /**
     * Устанавливает альфа-канал для цвета
     */
    public java.awt.Color alpha(java.awt.Color color, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * Применяет прозрачность (множитель от 0.0 до 1.0) к цвету (int)
     */
    public static int applyOpacity(int color, float opacity) {
        java.awt.Color old = new java.awt.Color(color);
        return applyOpacity(old, opacity).getRGB();
    }

    /**
     * Применяет прозрачность к цвету (возвращает Color)
     */
    public static java.awt.Color applyOpacity(java.awt.Color color, float opacity) {
        opacity = Math.min(1.0F, Math.max(0.0F, opacity));
        int alpha = (int) (color.getAlpha() * opacity);
        return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * Плавный переход между двумя цветами
     */
    public java.awt.Color fade(java.awt.Color color1, java.awt.Color color2, float alpha) {
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));

        int r = (int) (color1.getRed() * (1 - alpha) + color2.getRed() * alpha);
        int g = (int) (color1.getGreen() * (1 - alpha) + color2.getGreen() * alpha);
        int b = (int) (color1.getBlue() * (1 - alpha) + color2.getBlue() * alpha);
        int a = (int) (color1.getAlpha() * (1 - alpha) + color2.getAlpha() * alpha);

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        a = Math.max(0, Math.min(255, a));

        return new java.awt.Color(r, g, b, a);
    }

    /**
     * Пульсация цвета (изменение альфы по синусоиде)
     */

    /**
     * Уменьшает альфу цвета на заданный множитель
     */
    public java.awt.Color offset(java.awt.Color color, float alpha) {
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        int newAlpha = (int) (color.getAlpha() * alpha);
        newAlpha = Math.max(0, Math.min(255, newAlpha));
        return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
    }

    /**
     * Глобальный градиентный цвет (фиолетовый от темного к светлому)
     * ВНИМАНИЕ: в оригинале был баг — цикл возвращал цвет сразу, не дойдя до нужного прогресса
     */
    public java.awt.Color getGlobalColor(int alpha) {
        return new java.awt.Color(118, 86, 211, alpha);
    }

    public java.awt.Color getGlobalColor() {
        return getGlobalColor(255);
    }

    /**
     * Глобальный цвет с заданным прогрессом и альфой
     */
    public java.awt.Color getGlobalColor1(float amount) {
        amount = Math.max(0f, Math.min(1f, amount));
        java.awt.Color lightPurple = new java.awt.Color(118, 86, 211);
        java.awt.Color darkPurple = new java.awt.Color(75, 82, 158);
        java.awt.Color color = gradient(darkPurple, lightPurple, amount);
        return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
    }

    public java.awt.Color getGlobalColor1() {
        return getGlobalColor1(0f);
    }

    /**
     * Линейный градиент между двумя цветами
     */
    public static java.awt.Color gradient(java.awt.Color color1, java.awt.Color color2, float amount) {
        amount = Math.max(0.0f, Math.min(1.0f, amount));
        int r = (int) (color1.getRed() * (1 - amount) + color2.getRed() * amount);
        int g = (int) (color1.getGreen() * (1 - amount) + color2.getGreen() * amount);
        int b = (int) (color1.getBlue() * (1 - amount) + color2.getBlue() * amount);
        int a = (int) (color1.getAlpha() * (1 - amount) + color2.getAlpha() * amount);

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        a = Math.max(0, Math.min(255, a));

        return new java.awt.Color(r, g, b, a);
    }

    /**
     * Многоступенчатый градиент через массив цветов
     */
    public java.awt.Color multiGradient(java.awt.Color[] colors, float progress) {
        if (colors == null || colors.length == 0) return java.awt.Color.WHITE;
        if (colors.length == 1) return colors[0];

        progress = Math.max(0.0f, Math.min(1.0f, progress));
        float scaledProgress = progress * (colors.length - 1);
        int index = (int) Math.floor(scaledProgress);
        float localProgress = scaledProgress - index;

        if (index >= colors.length - 1) return colors[colors.length - 1];

        return fade(colors[index], colors[index + 1], localProgress);
    }

    /**
     * Создаёт цвет из HSV (Hue, Saturation, Value)
     */
    public java.awt.Color fromHSV(float hue, float saturation, float value) {
        hue = ((hue % 360) + 360) % 360;
        saturation = Math.max(0.0f, Math.min(1.0f, saturation));
        value = Math.max(0.0f, Math.min(1.0f, value));

        float c = value * saturation;
        float x = c * (1 - Math.abs((hue / 60) % 2 - 1));
        float m = value - c;

        float r, g, b;
        if (hue < 60)      { r = c; g = x; b = 0; }
        else if (hue < 120) { r = x; g = c; b = 0; }
        else if (hue < 180) { r = 0; g = c; b = x; }
        else if (hue < 240) { r = 0; g = x; b = c; }
        else if (hue < 300) { r = x; g = 0; b = c; }
        else                { r = c; g = 0; b = x; }

        int red = (int) ((r + m) * 255);
        int green = (int) ((g + m) * 255);
        int blue = (int) ((b + m) * 255);

        return new java.awt.Color(
                Math.max(0, Math.min(255, red)),
                Math.max(0, Math.min(255, green)),
                Math.max(0, Math.min(255, blue))
        );
    }

    public static String minecraftGradient(java.awt.Color start, java.awt.Color end, String text) {
        StringBuilder sb = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float progress = (float) i / (length - 1);
            java.awt.Color color = gradient(start, end, progress);

            sb.append(getNearestColorCode(color)).append(text.charAt(i));
        }

        return sb.toString();
    }

    public static String getLetterGradientText(java.awt.Color startColor, java.awt.Color endColor, String text) {
        StringBuilder result = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float progress = (float) i / (length - 1);
            java.awt.Color currentColor = gradient(startColor, endColor, progress);
            String colorCode = getNearestMinecraftColor(currentColor);
            result.append(colorCode).append(text.charAt(i));
        }

        return result.toString();
    }

    public static String getNearestMinecraftColor(java.awt.Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();


        if (r < 30 && g < 30 && b < 30) return "§0";
        if (r > 200 && g > 200 && b > 200) return "§f";
        if (r > g && r > b) {
            if (g > 150) return "§6";
            return "§c";
        }
        if (g > r && g > b) return "§a";
        if (b > r && b > g) return "§9";
        if (r > 200 && g > 200) return "§e";
        if (r > 200 && b > 200) return "§d";
        if (g > 200 && b > 200) return "§b";
        return "§f";
    }
    public static String getFullRGBGradient(java.awt.Color start, java.awt.Color end, String text) {
        StringBuilder sb = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float progress = (float) i / (length - 1);
            java.awt.Color color = gradient(start, end, progress);
            sb.append(getNearestMinecraftColor(color)).append(text.charAt(i));
        }

        return sb.toString();
    }

    public static String getUltraSmoothGradient(java.awt.Color start, java.awt.Color end, String text) {
        StringBuilder sb = new StringBuilder();
        int steps = text.length() * 2;

        for (int i = 0; i < text.length(); i++) {

            float progress1 = (float) (i * 2) / steps;
            java.awt.Color color1 = gradient(start, end, progress1);


            float progress2 = (float) (i * 2 + 1) / steps;
            java.awt.Color color2 = gradient(start, end, progress2);


            java.awt.Color avgColor = new java.awt.Color(
                    (color1.getRed() + color2.getRed()) / 2,
                    (color1.getGreen() + color2.getGreen()) / 2,
                    (color1.getBlue() + color2.getBlue()) / 2
            );

            sb.append(getNearestMinecraftColor(avgColor)).append(text.charAt(i));
        }

        return sb.toString();
    }

    public static String getHexColor(java.awt.Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    public static String getHexGradient(java.awt.Color start, java.awt.Color end, String text) {
        StringBuilder sb = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float progress = (float) i / (length - 1);
            java.awt.Color color = gradient(start, end, progress);
            sb.append("§x");
            String hex = getHexColor(color).substring(1);
            for (char c : hex.toCharArray()) {
                sb.append("§").append(c);
            }
            sb.append(text.charAt(i));
        }

        return sb.toString();
    }
    public static String getSmoothGradientText(java.awt.Color start, java.awt.Color end, String text) {
        StringBuilder sb = new StringBuilder();
        int steps = text.length() * 2 - 1;

        for (int i = 0; i < text.length(); i++) {

            float progress1 = (float) (i*2) / steps;
            java.awt.Color color1 = gradient(start, end, progress1);


            float progress2 = (float) (i*2 + 1) / steps;
            java.awt.Color color2 = gradient(start, end, progress2);


            java.awt.Color avgColor = new java.awt.Color(
                    (color1.getRed() + color2.getRed())/2,
                    (color1.getGreen() + color2.getGreen())/2,
                    (color1.getBlue() + color2.getBlue())/2
            );

            sb.append(getNearestColorCode(avgColor))
                    .append(text.charAt(i));
        }

        return sb.toString();
    }
    public static String getGradientText(java.awt.Color start, java.awt.Color end, String text) {
        StringBuilder sb = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float progress = (float) i / Math.max(1, length - 1);
            java.awt.Color color = gradient(start, end, progress);
            sb.append(getNearestColorCode(color)).append(text.charAt(i));
        }

        return sb.toString();
    }
    private static String getNearestColorCode(java.awt.Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();


        if (r < 30 && g < 30 && b < 30) return "§0";
        if (r > 200 && g < 50 && b < 50) return "§c";
        if (r < 50 && g > 200 && b < 50) return "§a";
        if (r < 50 && g < 50 && b > 200) return "§9";
        if (r > 200 && g > 200 && b < 50) return "§e";
        if (r > 200 && g < 50 && b > 200) return "§d";
        if (r < 50 && g > 200 && b > 200) return "§b";
        if (r > 200 && g > 200 && b > 200) return "§f";
        if (r > 150 && g > 100 && b < 50) return "§6";
        return "§f";
    }
    /**
     * Градиент с весами (взвешенная интерполяция)
     */
    public java.awt.Color weightedGradient(java.awt.Color[] colors, float[] weights, float progress) {
        if (colors == null || weights == null || colors.length != weights.length || colors.length == 0) {
            return Color.WHITE;
        }

        progress = Math.max(0.0f, Math.min(1.0f, progress));

        float totalWeight = 0;
        for (float w : weights) totalWeight += w;

        if (totalWeight <= 0) return colors[0];

        float accumulated = 0;
        for (int i = 0; i < weights.length; i++) {
            float normalizedWeight = weights[i] / totalWeight;
            if (progress <= accumulated + normalizedWeight) {
                float localProgress = (progress - accumulated) / normalizedWeight;
                localProgress = Math.max(0.0f, Math.min(1.0f, localProgress));
                if (i == weights.length - 1) return colors[i];
                return fade(colors[i], colors[i + 1], localProgress);
            }
            accumulated += normalizedWeight;
        }

        return colors[colors.length - 1];
    }
}