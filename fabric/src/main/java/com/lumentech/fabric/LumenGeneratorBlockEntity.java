package com.lumentech.fabric;

import com.lumentech.config.LumenTechConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleEnergyStorage;

/**
 * Fabric-only block entity that makes the Lumen generator speak the Reborn Energy
 * API (the same API TechReborn machines use), so they can pull power from it.
 *
 * <p>Reborn Energy is push-based: this generator fills its internal buffer every
 * tick (rate from {@link LumenTechConfig#GENERATOR_OUTPUT}) and then pushes energy
 * into any adjacent machine that accepts insertion.</p>
 *
 * <p>Energy is intentionally <em>not</em> persisted across world reloads in this
 * scaffold &mdash; a generator simply regenerates it. If you want buffered energy to
 * survive restarts, add {@code saveAdditional}/{@code loadAdditional} guarded by a
 * Stonecutter {@code //? if >=1.21} / {@code //? if <1.21} block, since the
 * {@code HolderLookup.Provider} parameter only exists on 1.20.5+.</p>
 */
public class LumenGeneratorBlockEntity extends BlockEntity {
    public static final long CAPACITY = 100_000L;
    public static final long MAX_INSERT = 0L; // generators never accept input
    public static final long MAX_EXTRACT = 1_000L;
    public static final long MAX_TRANSFER_PER_TICK = 1_000L;

    public final SimpleEnergyStorage energyStorage = new SimpleEnergyStorage(CAPACITY, MAX_INSERT, MAX_EXTRACT) {
        @Override
        protected void onFinalCommit() {
            setChanged();
        }
    };

    public LumenGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(FabricEnergy.GENERATOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LumenGeneratorBlockEntity self) {
        if (level.isClientSide()) {
            return;
        }

        // Generate energy (rate comes from the synced COMMON config).
        if (LumenTechConfig.GENERATOR_ENABLED.get()) {
            long produced = LumenTechConfig.GENERATOR_OUTPUT.get().longValue();
            long space = CAPACITY - self.energyStorage.amount;
            long added = Math.min(space, produced);
            if (added > 0) {
                self.energyStorage.amount += added;
                self.setChanged();
            }
        }

        // Push to adjacent acceptors (Reborn Energy is push-based).
        if (self.energyStorage.amount > 0) {
            for (Direction direction : Direction.values()) {
                EnergyStorage target = EnergyStorage.SIDED.find(level, pos.relative(direction), direction.getOpposite());
                if (target != null && target.supportsInsertion()) {
                    long moved = EnergyStorageUtil.move(self.energyStorage, target, MAX_TRANSFER_PER_TICK, null);
                    if (moved > 0) {
                        self.setChanged();
                    }
                    if (self.energyStorage.amount <= 0) {
                        break;
                    }
                }
            }
        }
    }
}
