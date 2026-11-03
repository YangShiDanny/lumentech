package com.lumentech.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The Signal Receiver: a 3D animated block whose dish opens the moment it is placed.
 *
 * <p>The block entity is injected per loader, exactly like {@link LumenGeneratorBlock}:
 * Fabric supplies a GeckoLib-backed entity (so the model is animated and rendered),
 * while Forge / NeoForge leave the factory null and the block has no entity. The
 * vanilla block model is hidden via {@link RenderShape#ENTITYBLOCK_ANIMATED} so that
 * only the GeckoLib model is drawn.</p>
 */
public class SignalReceiverBlock extends Block implements EntityBlock {
    /** Thin pole footprint in 0..16 block units, so the tall dish does not trap players. */
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    /** Supplies the block entity. Fabric sets this; other loaders leave it null. */
    public static BlockEntityFactory ENTITY_FACTORY = (pos, state) -> null;
    /** Server tick callback. Fabric sets this. */
    public static BlockEntityTicker<BlockEntity> TICKER = null;

    public SignalReceiverBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
