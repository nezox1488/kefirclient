package kefirdlc.dev.util.math;
// coded by sitoku \\
// since 27.04.2026 \\

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TimerUtil {
    private long lastMS = Instant.now().toEpochMilli();

    public void reset() {
        lastMS = Instant.now().toEpochMilli();
    }

    public boolean hasTimeElapsed(long time, boolean reset) {
        if (Instant.now().toEpochMilli() - lastMS > time) {
            if (reset) reset();
            return true;
        }
        return false;
    }

    public long getLastMS() {
        return this.lastMS;
    }

    public void updateLastMS() {
        lastMS = Instant.now().toEpochMilli();
    }

    public boolean hasTimeElapsed(long time) {
        return Instant.now().toEpochMilli() - lastMS > time;
    }

    public long getTime() {
        return Instant.now().toEpochMilli() - lastMS;
    }

    public void setTime(long time) {
        lastMS = time;
    }
    public void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public static long getCurrentTimeMillis() {
        return Instant.now().toEpochMilli();
    }
    public boolean hasSecondsElapsed(long seconds, boolean reset) {
        long timeInSeconds = ChronoUnit.SECONDS.between(Instant.ofEpochMilli(lastMS), Instant.now());
        if (timeInSeconds > seconds) {
            if (reset) reset();
            return true;
        }
        return false;
    }
    public void setTimeInFuture(long futureTimeMillis) {
        lastMS = Math.max(futureTimeMillis, Instant.now().toEpochMilli());
    }

    public boolean isTimeBefore(long targetTimeMillis) {
        return Instant.now().toEpochMilli() < targetTimeMillis;
    }
    public long getTimeDifference(long otherTimeMillis) {
        return Instant.now().toEpochMilli() - otherTimeMillis;
    }
    public String formatTime(long timeMillis) {
        return Instant.ofEpochMilli(timeMillis).toString();
    }
    private long startTime;



    public boolean finished(final double delay) {
        return System.currentTimeMillis() - delay >= startTime;
    }

    public boolean every(final double delay) {
        boolean finished = this.finished(delay);
        if (finished) reset();
        return finished;
    }



    public int elapsedTime() {
        long elapsed = System.currentTimeMillis() - this.startTime;
        if (elapsed > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else if (elapsed < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) elapsed;
    }

    public TimerUtil setMs(long ms) {
        this.startTime = System.currentTimeMillis() - ms;
        return this;
    }
}
