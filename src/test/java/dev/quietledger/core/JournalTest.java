package dev.quietledger.core;

import java.util.HashSet;

public final class JournalTest {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        int flags = 0;
        var masks = new HashSet<Integer>();
        for (var clue : Journal.Clue.values()) {
            check(masks.add(clue.mask()), "unique persistent bit");
            flags = Journal.discover(flags, clue);
            check(Journal.contains(flags, clue), "discovery");
            check(Journal.discover(flags, clue) == flags, "idempotence");
        }
        check(Journal.entries(flags).size() == Journal.Clue.values().length, "all entries");
        check(Journal.entries(0).isEmpty(), "new player");
        check(Journal.eventClue(AnomalyCatalog.Kind.DEEP_KNOCK) == Journal.Clue.BELOW, "event clue");
        check(Journal.eventClue(AnomalyCatalog.Kind.LOST_ROOM) == null, "no accidental clue");
        System.out.println("JournalTest: unique persistent bits, discovery and event mapping passed");
    }
}
