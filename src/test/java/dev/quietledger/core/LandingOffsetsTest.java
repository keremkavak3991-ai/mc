package dev.quietledger.core;

import java.util.HashSet;

public final class LandingOffsetsTest {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        var offsets = LandingOffsets.offsets();
        check(offsets.size() == 81, "bounded number of checks");
        check(new HashSet<>(offsets).size() == 81, "no duplicate positions");
        check(offsets.get(0).equals(new LandingOffsets.Offset(0, 0)), "center first");
        int previousRadius = 0;
        for (var point : offsets) {
            int radius = Math.max(Math.abs(point.x()), Math.abs(point.z()));
            check(radius <= 16 && point.x() % 4 == 0 && point.z() % 4 == 0, "bounded grid");
            check(radius >= previousRadius, "nearer rings first");
            previousRadius = radius;
        }
        try { offsets.clear(); throw new AssertionError("offset list mutable"); }
        catch (UnsupportedOperationException expected) { }
        System.out.println("LandingOffsetsTest: 81 bounded unique locations, ordering and immutability passed");
    }
}
