package dev.quietledger.server;

import dev.quietledger.LedgerConfig;
import dev.quietledger.LedgerSounds;
import dev.quietledger.core.AnomalyCatalog;
import dev.quietledger.core.Progression;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Runs once a second, never changes blocks, inventory, permissions, or files. */
public final class AnomalyDirector {
    private AnomalyDirector() {}
    public static void onTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        var players = server.getPlayerList().getPlayers();
        if (server.getTickCount() % 20 != 0 || !LedgerConfig.ENABLED.get() || players.isEmpty()) return;
        LedgerData data = LedgerData.get(server);
        data.advance(20);
        if (data.suppressed() || !Progression.due(data.activeTicks(), data.lastOpportunity(), LedgerConfig.INTERVAL.get())) return;
        data.opportunity(); // Failed rolls and no eligible target also consume an opportunity.
        var random = server.overworld().random;
        if (random.nextInt(100) >= LedgerConfig.CHANCE.get()) return;
        int start = random.nextInt(players.size());
        int stage = Progression.stage(data.activeTicks(), LedgerConfig.STAGE_MINUTES.get());
        var lastEvents = data.lastEvents();
        // At most one delivery per opportunity, randomizing the first candidate.
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player = players.get((start + i) % players.size());
            if (!player.isAlive() || player.isSpectator() || !data.playerReady(player.getUUID(), LedgerConfig.PLAYER_COOLDOWN.get())) continue;
            var chosen = AnomalyCatalog.choose(stage, audioEnabled(), LedgerConfig.MESSAGES.get(),
                player.getY() < 40, data.activeTicks(), lastEvents, random::nextInt);
            if (chosen == null) continue;
            emit(player, chosen.kind());
            data.recordScheduled(chosen.kind(), player.getUUID());
            return;
        }
    }
    /** Operator test bypasses cooldowns and rarity but respects stage, location and channels. */
    public static boolean trigger(ServerPlayer player, int stage) {
        var chosen = AnomalyCatalog.choose(stage, audioEnabled(), LedgerConfig.MESSAGES.get(),
            player.getY() < 40, 0, Map.of(), player.serverLevel().random::nextInt);
        if (chosen == null) return false;
        emit(player, chosen.kind());
        return true;
    }
    private static boolean audioEnabled() {
        return LedgerConfig.AUDIO.get() && LedgerConfig.AUDIO_VOLUME.get() > 0;
    }
    /** Deliberate operator preview; honors the audio accessibility settings. */
    public static boolean preview(ServerPlayer player, AnomalyCatalog.Kind kind) {
        if (!audioEnabled() || (kind != AnomalyCatalog.Kind.DISTANT_DOOR && kind != AnomalyCatalog.Kind.CAVE_ECHO
                && kind != AnomalyCatalog.Kind.DEEP_KNOCK && kind != AnomalyCatalog.Kind.NEAR_BREATH
                && kind != AnomalyCatalog.Kind.UNSEEN_STEPS)) return false;
        emit(player, kind);
        return true;
    }
    private static void sound(ServerPlayer player, net.minecraft.sounds.SoundEvent sound) {
        player.playNotifySound(sound, SoundSource.AMBIENT, LedgerConfig.AUDIO_VOLUME.get() / 100.0f, 1.0f);
    }
    private static void emit(ServerPlayer player, AnomalyCatalog.Kind kind) {
        switch (kind) {
            case DISTANT_DOOR -> sound(player, LedgerSounds.DISTANT_CREAK.get());
            case CAVE_ECHO -> sound(player, LedgerSounds.HOLLOW_DRONE.get());
            case DEEP_KNOCK -> sound(player, LedgerSounds.DEEP_KNOCKS.get());
            case NEAR_BREATH -> sound(player, LedgerSounds.NEAR_BREATH.get());
            case UNSEEN_STEPS -> sound(player, LedgerSounds.UNSEEN_STEPS.get());
            case COUNTED_SILENCE -> player.sendSystemMessage(Component.literal("Something counted the silence."));
            case LOST_ROOM -> player.sendSystemMessage(Component.literal("The ledger remembers a room you have not entered."));
        }
    }
}
