package dev.quietledger.core;

import java.util.ArrayList;
import java.util.List;

/** Stable bit assignments are saved to the world; never reorder or reuse them. */
public final class Journal {
    public enum Clue {
        FIRST_PAGE(0, "The page is numbered zero. Its margins say: listen before you answer."),
        CREAK(1, "The first sound was not a door. Something is measuring distance."),
        FOOTSTEPS(2, "The steps stop whenever you count them."),
        BELOW(3, "Three knocks come from below. The third never comes from the same place."),
        BREATH(4, "A breath belongs to no visible speaker. Do not follow it blindly."),
        EXPANSE(5, "The Empty Expanse has a floor, but no horizon you can reach."),
        CORRIDOR(6, "A corridor was promised. Only its floor remains in this version of the world.");
        private final int bit;
        private final String text;
        Clue(int bit, String text) { this.bit = bit; this.text = text; }
        public int mask() { return 1 << bit; }
        public String text() { return text; }
    }
    private Journal() {}
    public static int discover(int flags, Clue clue) { return flags | clue.mask(); }
    public static boolean contains(int flags, Clue clue) { return (flags & clue.mask()) != 0; }
    public static List<Clue> entries(int flags) {
        var entries = new ArrayList<Clue>();
        for (Clue clue : Clue.values()) if (contains(flags, clue)) entries.add(clue);
        return List.copyOf(entries);
    }
    public static Clue eventClue(AnomalyCatalog.Kind kind) {
        return switch (kind) {
            case DISTANT_DOOR -> Clue.CREAK;
            case DEEP_KNOCK -> Clue.BELOW;
            case NEAR_BREATH -> Clue.BREATH;
            case UNSEEN_STEPS -> Clue.FOOTSTEPS;
            default -> null;
        };
    }
}
