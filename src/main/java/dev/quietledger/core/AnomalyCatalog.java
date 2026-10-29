package dev.quietledger.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

/** Immutable event definitions and selection rules. No Minecraft classes or side effects. */
public final class AnomalyCatalog {
    public enum Kind { DISTANT_DOOR, CAVE_ECHO, COUNTED_SILENCE, LOST_ROOM, DEEP_KNOCK, NEAR_BREATH, UNSEEN_STEPS }
    public enum Channel { AUDIO, MESSAGE }
    public record Definition(Kind kind, Channel channel, int minimumStage, int weight,
                             long cooldownTicks, boolean undergroundOnly) {}

    private static final List<Definition> EVENTS = List.of(
        new Definition(Kind.DISTANT_DOOR, Channel.AUDIO, 0, 6, 18000, false),
        new Definition(Kind.CAVE_ECHO, Channel.AUDIO, 2, 4, 24000, false),
        new Definition(Kind.COUNTED_SILENCE, Channel.MESSAGE, 1, 3, 36000, false),
        new Definition(Kind.LOST_ROOM, Channel.MESSAGE, 3, 2, 48000, false),
        new Definition(Kind.DEEP_KNOCK, Channel.AUDIO, 1, 4, 24000, true),
        new Definition(Kind.NEAR_BREATH, Channel.AUDIO, 2, 2, 48000, false),
        new Definition(Kind.UNSEEN_STEPS, Channel.AUDIO, 1, 3, 36000, false)
    );
    private AnomalyCatalog() {}
    public static List<Definition> definitions() { return EVENTS; }

    /** Returns null if no event is eligible. lastPlayed contains successful emissions only. */
    public static Definition choose(int stage, boolean audio, boolean messages, boolean underground,
                                    long now, Map<Kind, Long> lastPlayed, IntUnaryOperator nextInt) {
        List<Definition> eligible = new ArrayList<>(EVENTS.size());
        int total = 0;
        for (Definition d : EVENTS) {
            Long last = lastPlayed.get(d.kind());
            if (stage < d.minimumStage() || (d.channel() == Channel.AUDIO && !audio)
                    || (d.channel() == Channel.MESSAGE && !messages)
                    || (d.undergroundOnly() && !underground)
                    || (last != null && !Progression.due(now, last, (int)d.cooldownTicks()))) continue;
            eligible.add(d);
            total += d.weight();
        }
        if (total == 0) return null;
        int roll = nextInt.applyAsInt(total);
        if (roll < 0 || roll >= total) throw new IllegalArgumentException("Random roll outside bound");
        for (Definition d : eligible) {
            if (roll < d.weight()) return d;
            roll -= d.weight();
        }
        throw new IllegalStateException("Invalid event weights");
    }
}
