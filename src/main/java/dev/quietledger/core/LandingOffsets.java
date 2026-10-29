package dev.quietledger.core;

import java.util.ArrayList;
import java.util.List;

/** A bounded, deterministic search pattern: centre first, then square rings. */
public final class LandingOffsets {
    public record Offset(int x, int z) {}
    private static final List<Offset> OFFSETS = build();
    private LandingOffsets() {}
    public static List<Offset> offsets() { return OFFSETS; }
    private static List<Offset> build() {
        var result = new ArrayList<Offset>();
        result.add(new Offset(0, 0));
        for (int radius = 4; radius <= 16; radius += 4) {
            for (int x = -radius; x <= radius; x += 4) {
                for (int z = -radius; z <= radius; z += 4) {
                    if (Math.abs(x) == radius || Math.abs(z) == radius) result.add(new Offset(x, z));
                }
            }
        }
        return List.copyOf(result);
    }
}
