package dev.quietledger.server;

import dev.quietledger.core.Journal;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Player-private journal output; no external storage or network requests. */
public final class JournalService {
    private JournalService() {}
    public static int show(ServerPlayer player) {
        var entries = Journal.entries(LedgerData.get(player.getServer()).journal(player.getUUID()));
        player.sendSystemMessage(Component.literal("The Quiet Ledger — " + entries.size() + "/" + Journal.Clue.values().length + " fragments"));
        if (entries.isEmpty()) player.sendSystemMessage(Component.literal("No fragments recorded yet. Craft a torn page or wait and listen."));
        for (var clue : entries) player.sendSystemMessage(Component.literal("• " + clue.text()));
        return 1;
    }
}
