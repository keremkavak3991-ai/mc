package dev.quietledger.item;

import dev.quietledger.core.Journal;
import dev.quietledger.server.JournalService;
import dev.quietledger.server.LedgerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A reusable, server-authoritative introduction to the investigation. */
public final class TornPageItem extends Item {
    public TornPageItem(Properties properties) { super(properties); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            LedgerData.get(serverPlayer.getServer()).discover(serverPlayer.getUUID(), Journal.Clue.FIRST_PAGE);
            JournalService.show(serverPlayer);
            serverPlayer.getCooldowns().addCooldown(this, 40);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
