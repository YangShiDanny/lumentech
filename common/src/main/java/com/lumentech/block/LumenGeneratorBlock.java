package com.lumentech.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * The Lumen generator block. It is an {@link EntityBlock} on every loader, but the
 * actual block entity is injected per-loader so the mod keeps building on Forge and
 * NeoForge:
 *
 * <ul>
 *   <li><b>Fabric</b> wires in an energy-capable entity (see
 *       {@code com.lumentech.fabric.FabricEnergy}) that produces and emits Reborn
 *       Energy, so it interops with TechReborn machines.</li>
 *   <li><b>Forge / NeoForge</b> leave {@link #ENTITY_FACTORY} and {@link #TICKER}
 *       null, so the block behaves as a plain decorative block (those loaders would
 *       need a Forge / NeoForge energy capability instead).</li>
 * </ul>
 */
public class LumenGeneratorBlock extends Block implements EntityBlock {
    /** Supplies the block entity. Fabric sets this; other loaders leave it null. */
    public static BlockEntityFactory ENTITY_FACTORY = (pos, state) -> null;
    /** Per-tick callback. Fabric sets this to drive energy generation. */
    public static BlockEntityTicker<BlockEntity> TICKER = null;

    public LumenGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_FACTORY.create(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return TICKER == null ? null : (BlockEntityTicker<T>) TICKER;
    }

    @FunctionalInterface
    public interface BlockEntityFactory {
        @Nullable
        BlockEntity create(BlockPos pos, BlockState state);
    }
}
