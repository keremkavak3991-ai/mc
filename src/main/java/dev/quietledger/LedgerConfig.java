package dev.quietledger;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class LedgerConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue ENABLED = B.comment("Master switch for scheduled anomalies and progression.").define("enabled", true);
    public static final ModConfigSpec.BooleanValue AUDIO = B.comment("Allow Quiet Ledger ambient sound events.").define("audio", true);
    public static final ModConfigSpec.IntValue AUDIO_VOLUME = B.comment("Volume of Quiet Ledger sounds as a percentage; 0 silences them. The client ambient volume slider also applies.").defineInRange("audioVolumePercent", 50, 0, 100);
    public static final ModConfigSpec.BooleanValue MESSAGES = B.comment("Allow occasional in-game narrative messages.").define("messages", true);
    public static final ModConfigSpec.IntValue INTERVAL = B.comment("Minimum ticks between anomaly opportunities (20 ticks per second).").defineInRange("intervalTicks", 12000, 1200, 1200000);
    public static final ModConfigSpec.IntValue STAGE_MINUTES = B.comment("Active server minutes required per progression stage.").defineInRange("minutesPerStage", 90, 1, 10080);
    public static final ModConfigSpec.IntValue CHANCE = B.comment("Percent chance at each anomaly opportunity.").defineInRange("chancePercent", 30, 0, 100);
    public static final ModConfigSpec.IntValue PLAYER_COOLDOWN = B.comment("Minimum ticks between scheduled events for the same player.").defineInRange("playerCooldownTicks", 12000, 1200, 1200000);
    public static final ModConfigSpec.BooleanValue DIMENSION_TRAVEL = B.comment("Allow operator-initiated travel to Quiet Ledger dimensions; escape is always available.").define("dimensionTravel", true);
    public static final ModConfigSpec.IntValue DIMENSION_STAGE = B.comment("Minimum world stage for operator-initiated dimension travel.").defineInRange("dimensionMinimumStage", 0, 0, 5);
    public static final ModConfigSpec SPEC = B.build();
    private LedgerConfig() {}
}
