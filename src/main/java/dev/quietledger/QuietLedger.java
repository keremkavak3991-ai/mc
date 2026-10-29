package dev.quietledger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import dev.quietledger.server.AnomalyDirector;
import dev.quietledger.server.LedgerCommands;
import dev.quietledger.item.LedgerItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(QuietLedger.ID)
public final class QuietLedger {
    public static final String ID = "quietledger";

    public QuietLedger(IEventBus bus, ModContainer container) {
        LedgerSounds.EVENTS.register(bus);
        LedgerItems.ITEMS.register(bus);
        bus.addListener(QuietLedger::creativeItems);
        container.registerConfig(ModConfig.Type.SERVER, LedgerConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(AnomalyDirector::onTick);
        NeoForge.EVENT_BUS.addListener(LedgerCommands::register);
    }
    private static void creativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) event.accept(LedgerItems.TORN_PAGE);
    }
}
