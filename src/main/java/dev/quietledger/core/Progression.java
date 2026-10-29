package dev.quietledger.core;

/** Pure, deterministic progression and scheduling rules; time is active server ticks. */
public final class Progression {
    private Progression() {}
    public static int stage(long activeTicks, int minutesPerStage) {
        long ticksPerStage = Math.max(1L, minutesPerStage) * 1200L;
        return (int)Math.min(5L, Math.max(0L, activeTicks) / ticksPerStage);
    }
    public static boolean due(long activeTicks, long lastEventTick, int interval) {
        return activeTicks >= lastEventTick && activeTicks - lastEventTick >= Math.max(1, interval);
    }
}
