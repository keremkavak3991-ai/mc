package dev.quietledger.item;

import dev.quietledger.QuietLedger;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LedgerItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(QuietLedger.ID);
    public static final DeferredItem<TornPageItem> TORN_PAGE = ITEMS.registerItem("torn_page", TornPageItem::new, new Item.Properties().stacksTo(1));
    private LedgerItems() {}
}
