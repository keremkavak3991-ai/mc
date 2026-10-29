package dev.quietledger.server;

import dev.quietledger.LedgerConfig;
import dev.quietledger.core.Progression;
import dev.quietledger.core.AnomalyCatalog;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Operator-only diagnostics and emergency stop. */
public final class LedgerCommands {
    private LedgerCommands() {}
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("quietledger").requires(s -> s.hasPermission(2))
            .then(Commands.literal("status").executes(c -> {
                LedgerData d = LedgerData.get(c.getSource().getServer());
                int stage = Progression.stage(d.activeTicks(), LedgerConfig.STAGE_MINUTES.get());
                c.getSource().sendSuccess(() -> Component.literal("Quiet Ledger: stage " + stage + ", active ticks " + d.activeTicks() + ", events " + d.events() + ", suppressed " + d.suppressed()), false);
                return 1;
            }))
            .then(Commands.literal("suppress").executes(c -> {
                LedgerData.get(c.getSource().getServer()).setSuppressed(true);
                c.getSource().sendSuccess(() -> Component.literal("Scheduled anomalies suppressed."), true); return 1;
            }))
            .then(Commands.literal("resume").executes(c -> {
                LedgerData.get(c.getSource().getServer()).setSuppressed(false);
                c.getSource().sendSuccess(() -> Component.literal("Scheduled anomalies resumed."), true); return 1;
            }))
            .then(Commands.literal("enter")
                .then(Commands.literal("expanse").executes(c -> DimensionTravel.enter(c.getSource(), DimensionTravel.EXPANSE)))
                .then(Commands.literal("corridor").executes(c -> DimensionTravel.enter(c.getSource(), DimensionTravel.CORRIDOR))))
            .then(Commands.literal("sound")
                .then(Commands.literal("creak").executes(c -> preview(c.getSource(), AnomalyCatalog.Kind.DISTANT_DOOR)))
                .then(Commands.literal("drone").executes(c -> preview(c.getSource(), AnomalyCatalog.Kind.CAVE_ECHO)))
                .then(Commands.literal("knocks").executes(c -> preview(c.getSource(), AnomalyCatalog.Kind.DEEP_KNOCK)))
                .then(Commands.literal("breath").executes(c -> preview(c.getSource(), AnomalyCatalog.Kind.NEAR_BREATH)))
                .then(Commands.literal("steps").executes(c -> preview(c.getSource(), AnomalyCatalog.Kind.UNSEEN_STEPS))))
            .then(Commands.literal("test").executes(c -> {
                var p = c.getSource().getPlayerOrException();
                LedgerData d = LedgerData.get(c.getSource().getServer());
                boolean played = AnomalyDirector.trigger(p, Progression.stage(d.activeTicks(), LedgerConfig.STAGE_MINUTES.get()));
                if (played) d.recordEvent();
                c.getSource().sendSuccess(() -> Component.literal(played ? "Test anomaly sent." : "No enabled effect is eligible at this stage."), false);
                return played ? 1 : 0;
            })));
        event.getDispatcher().register(Commands.literal("ledgerjournal").executes(c -> JournalService.show(c.getSource().getPlayerOrException())));
        // Recovery must not depend on operator permissions or the travel configuration.
        event.getDispatcher().register(Commands.literal("ledgerescape").executes(c -> DimensionTravel.escape(c.getSource())));
    }
    private static int preview(net.minecraft.commands.CommandSourceStack source, AnomalyCatalog.Kind kind)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!AnomalyDirector.preview(source.getPlayerOrException(), kind)) {
            source.sendFailure(Component.literal("Audio is disabled or its configured volume is zero."));
            return 0;
        }
        return 1;
    }
}
