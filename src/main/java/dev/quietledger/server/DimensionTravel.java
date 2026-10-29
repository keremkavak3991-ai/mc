package dev.quietledger.server;

import dev.quietledger.LedgerConfig;
import dev.quietledger.QuietLedger;
import dev.quietledger.core.LandingOffsets;
import dev.quietledger.core.Progression;
import dev.quietledger.core.Journal;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

/** Server-only travel; does not place or remove blocks or touch inventory. */
public final class DimensionTravel {
    public static final ResourceKey<Level> EXPANSE = key("empty_expanse");
    public static final ResourceKey<Level> CORRIDOR = key("forgotten_corridor");
    private DimensionTravel() {}
    private static ResourceKey<Level> key(String id) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(QuietLedger.ID, id));
    }
    public static boolean isLedgerDimension(ResourceKey<Level> dimension) {
        return dimension.equals(EXPANSE) || dimension.equals(CORRIDOR);
    }
    public static int enter(CommandSourceStack source, ResourceKey<Level> destination) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!LedgerConfig.DIMENSION_TRAVEL.get()) return failure(source, "Dimension travel is disabled; escape remains available.");
        LedgerData data = LedgerData.get(source.getServer());
        if (Progression.stage(data.activeTicks(), LedgerConfig.STAGE_MINUTES.get()) < LedgerConfig.DIMENSION_STAGE.get())
            return failure(source, "The world has not reached the configured travel stage.");
        ServerLevel target = source.getServer().getLevel(destination);
        if (target == null) return failure(source, "Dimension is not available. Check world generation and restart the server.");
        int result = travel(source, player, target, new BlockPos(0, 0, 0));
        if (result == 1) data.discover(player.getUUID(), destination.equals(EXPANSE) ? Journal.Clue.EXPANSE : Journal.Clue.CORRIDOR);
        return result;
    }
    public static int escape(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!isLedgerDimension(player.serverLevel().dimension())) return failure(source, "Escape is only available inside Quiet Ledger dimensions.");
        ServerLevel target = source.getServer().overworld();
        return travel(source, player, target, target.getSharedSpawnPos());
    }
    private static int travel(CommandSourceStack source, ServerPlayer player, ServerLevel target, BlockPos origin) {
        BlockPos landing = findLanding(target, origin);
        if (landing == null) return failure(source, "No safe landing found near the destination. Contact an operator.");
        if (!player.teleportTo(target, landing.getX() + 0.5, landing.getY(), landing.getZ() + 0.5,
                Set.of(), player.getYRot(), player.getXRot())) return failure(source, "Travel was cancelled or failed.");
        player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        player.resetFallDistance();
        source.sendSuccess(() -> Component.literal("The Quiet Ledger: passage complete."), false);
        return 1;
    }
    /** Checks at most 81 columns in a 33x33 area. This may load destination chunks. */
    private static BlockPos findLanding(ServerLevel level, BlockPos origin) {
        for (var offset : LandingOffsets.offsets()) {
            BlockPos column = origin.offset(offset.x(), 0, offset.z());
            if (!level.getWorldBorder().isWithinBounds(column)) continue;
            BlockPos feet = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
            if (feet.getY() <= level.getMinBuildHeight() || feet.getY() + 1 >= level.getMaxBuildHeight()) continue;
            BlockPos floor = feet.below();
            var floorState = level.getBlockState(floor);
            if (!floorState.isFaceSturdy(level, floor, Direction.UP) || floorState.is(Blocks.MAGMA_BLOCK)
                    || floorState.is(Blocks.CAMPFIRE) || floorState.is(Blocks.SOUL_CAMPFIRE)) continue;
            if (!level.isEmptyBlock(feet) || !level.isEmptyBlock(feet.above())) continue;
            if (!level.getFluidState(feet).isEmpty() || !level.getFluidState(feet.above()).isEmpty()) continue;
            return feet;
        }
        return null;
    }
    private static int failure(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message));
        return 0;
    }
}
