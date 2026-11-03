package com.lumentech.fabric;

// Reborn Energy is only wired on legacy MC (1.x) - compiled out on 26.x.
//? if <1.22 {
import com.iamkaf.amber.api.registry.v1.DeferredRegister;
import com.iamkaf.amber.api.registry.v1.RegistrySupplier;
import com.lumentech.LumenTech;
import com.lumentech.block.LumenGeneratorBlock;
import com.lumentech.registry.LumenTechRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.Objects;

/**
 * Fabric-only wiring for the TechReborn / Reborn Energy integration.
 *
 * <p>Called from {@link com.lumentech.LumenTechFabric#onInitialize()} <em>after</em>
 * {@link com.lumentech.LumenTech#init()} so the generator block (registered in
 * common) is already present and {@link LumenTechRegistry#LUMEN_GENERATOR} resolves.
 * This is what makes the otherwise plain generator block energy-capable on Fabric
 * while leaving Forge / NeoForge untouched.</p>
 */
public final class FabricEnergy {
    // BlockEntityType<?> (not the raw type): DeferredRegister.create takes a
    // ResourceKey<Registry<T>>, and Registries.BLOCK_ENTITY_TYPE is keyed by
    // BlockEntityType<?>, so a raw BlockEntityType leaves T with two conflicting bounds.
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(LumenTech.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<?>> GENERATOR = BLOCK_ENTITY_TYPES.register("lumen_generator",
            () -> BlockEntityType.Builder.of(LumenGeneratorBlockEntity::new, LumenTechRegistry.LUMEN_GENERATOR.get())
                    .build(null));

    private FabricEnergy() {
    }

    public static void init() {
        BLOCK_ENTITY_TYPES.register();

        // Inject the energy block entity + ticker into the common generator block.
        LumenGeneratorBlock.ENTITY_FACTORY = LumenGeneratorBlockEntity::new;
        LumenGeneratorBlock.TICKER = (@Nullable Level level, BlockPos pos, BlockState state, @Nullable BlockEntity be) -> {
            if (be instanceof LumenGeneratorBlockEntity self) {
                LumenGeneratorBlockEntity.serverTick(Objects.requireNonNull(level), pos, state, self);
            }
        };

        // Expose the energy storage to TechReborn machines and any Reborn Energy consumer.
        @SuppressWarnings({"unchecked", "rawtypes"})
        BlockEntityType<LumenGeneratorBlockEntity> type = (BlockEntityType) GENERATOR.get();
        EnergyStorage.SIDED.registerForBlockEntity((be, direction) -> be.energyStorage, type);
    }
}
//?}
