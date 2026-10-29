package dev.quietledger.core;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class AnomalyCatalogTest {
    private static void check(boolean ok, String what) { if (!ok) throw new AssertionError(what); }
    public static void main(String[] args) {
        var empty = Map.<AnomalyCatalog.Kind, Long>of();
        check(AnomalyCatalog.choose(0, false, false, false, 0, empty, n -> 0) == null, "disabled channels");
        check(AnomalyCatalog.choose(0, false, true, true, 0, empty, n -> 0) == null, "stage gating");
        check(AnomalyCatalog.choose(0, true, false, false, 0, empty, n -> 0).kind() == AnomalyCatalog.Kind.DISTANT_DOOR, "initial sound");
        var cooldowns = new EnumMap<AnomalyCatalog.Kind, Long>(AnomalyCatalog.Kind.class);
        cooldowns.put(AnomalyCatalog.Kind.DISTANT_DOOR, 100L);
        check(AnomalyCatalog.choose(0, true, false, false, 18099, cooldowns, n -> 0) == null, "event cooldown");
        check(AnomalyCatalog.choose(0, true, false, false, 18100, cooldowns, n -> 0) != null, "cooldown boundary");
        check(AnomalyCatalog.choose(0, true, false, false, 99, cooldowns, n -> 0) == null, "future cooldown");
        Set<AnomalyCatalog.Kind> seen = new HashSet<>();
        Random random = new Random(42);
        for (int i = 0; i < 10000; i++) seen.add(AnomalyCatalog.choose(5, true, true, true, 0, empty, random::nextInt).kind());
        check(seen.size() == AnomalyCatalog.Kind.values().length, "all definitions reachable");
        for (int i = 0; i < 1000; i++) check(AnomalyCatalog.choose(5, true, false, false, 0, empty, random::nextInt).kind() != AnomalyCatalog.Kind.DEEP_KNOCK, "surface restriction");
        boolean threw = false;
        try { AnomalyCatalog.choose(0, true, true, true, 0, empty, n -> n); }
        catch (IllegalArgumentException expected) { threw = true; }
        check(threw, "invalid random bound");
        System.out.println("AnomalyCatalogTest: gating, weighted selection and cooldown checks passed");
    }
}
