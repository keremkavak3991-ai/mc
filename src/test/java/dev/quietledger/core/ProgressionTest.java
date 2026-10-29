package dev.quietledger.core;

public final class ProgressionTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        check(Progression.stage(0, 90) == 0, "initial stage");
        check(Progression.stage(107999, 90) == 0, "threshold exclusive");
        check(Progression.stage(108000, 90) == 1, "threshold inclusive");
        check(Progression.stage(Long.MAX_VALUE, 90) == 5, "stage cap");
        check(Progression.stage(-1, 90) == 0, "negative tick handling");
        check(Progression.due(1200, 0, 1200), "interval inclusive");
        check(!Progression.due(1199, 0, 1200), "interval exclusive");
        check(!Progression.due(100, 101, 1), "future opportunity");
        check(Progression.due(Long.MAX_VALUE, Long.MAX_VALUE - 1200, 1200), "large time");
        System.out.println("ProgressionTest: 9 checks passed");
    }
}
