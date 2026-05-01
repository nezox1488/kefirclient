package kefirdlc.dev.util.math;
// coded by sitoku \\
// since 27.04.2026 \\

import kefirdlc.dev.util.wrapper.Wrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

import static net.minecraft.util.math.MathHelper.lerp;

@UtilityClass
public class MathUtil implements Wrapper {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    Random random = new Random();
    public static double computeGcd() {
        return (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
    }
    /**
     * Интерполяция между двумя значениями (плавное смешивание).
     */

    public static float getTickDelta() {
        return mc.getRenderTickCounter().getTickProgress(false);
    }
    public static double interpolate(double prev, double current, double delta) {
        return prev + (current - prev) * delta;
    }

    public float interpolateRandom(float a, float b) {
        return (float) (random.nextGaussian() * (b - a) + a);
    }


    public double round(double num, double increment) {
        double rounded = Math.round(num / increment) * increment;
        return Math.round(rounded * 100.0) / 100.0;
    }

    public float textScrolling(float textWidth) {
        int speed = (int) (textWidth * 75);
        return (float) MathHelper.clamp((System.currentTimeMillis() % speed * Math.PI / speed), 0, 1) * textWidth;
    }

    public float round(float number) {
        return Math.round(number * 10f) / 10f;
    }
    public static float interpolate(float prev, float current, float delta) {
        return prev + (current - prev) * delta;
    }

    public static Vec3d interpolate(Entity entity) {
        if (entity == null) return Vec3d.ZERO;
        return new Vec3d(interpolate(entity.lastX, entity.getX()), interpolate(entity.lastY, entity.getY()), interpolate(entity.lastZ, entity.getZ()));
    }

    public static int floorNearestMulN(int x, int n) {
        return n * (int) Math.floor((double) x / (double) n);
    }
    public static double interpolate(double prev, double orig) {
        return lerp(tickCounter.getTickProgress(false), prev, orig);
    }

    public float interpolate(float prev, float orig) {
        return lerp(tickCounter.getTickProgress(false), prev, orig);
    }


    public Vec3d interpolate(Vec3d prevPos, Vec3d pos) {
        return new Vec3d(interpolate(prevPos.x, pos.x), interpolate(prevPos.y, pos.y), interpolate(prevPos.z, pos.z));
    }

    public static float getRandom(float min, double max) {
        return (float) (Math.random() * (max - min) + min);
    }
    /**
     * Ограничение значения между min и max.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Возвращает расстояние между двумя точками.
     */
    public double PI2 = Math.PI * 2;
    public Vec3d cosSin(int i, int size, double width) {
        int index = Math.min(i, size);
        float cos = (float) (Math.cos(index * MathUtil.PI2 / size) * width);
        float sin = (float) (-Math.sin(index * MathUtil.PI2 / size) * width);
        return new Vec3d(cos, 0, sin);
    }

    public double absSinAnimation(double input) {
        return Math.abs(1 + Math.sin(input)) / 2;
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Вычисление направления движения игрока (используется в FreeCam, Fly, Speed и т.д.)
     */


    /**
     * Преобразует градусы в радианы.
     */
    public static double toRadians(double degrees) {
        return degrees * (Math.PI / 180);
    }

    /**
     * Преобразует радианы в градусы.
     */
    public static double toDegrees(double radians) {
        return radians * (180 / Math.PI);
    }

    /**
     * Сглаживание угла (приведение к диапазону -180..180).
     */
    public static float wrapDegrees(float angle) {
        return MathHelper.wrapDegrees(angle);
    }

    /**
     * Вычисляет угол между двумя точками (в градусах).
     */
    public static float calcAngle(float srcX, float srcZ, float targetX, float targetZ) {
        float diffX = targetX - srcX;
        float diffZ = targetZ - srcZ;
        return (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F);
    }
}
