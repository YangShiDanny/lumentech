package com.lumentech.fabric;

import com.iamkaf.amber.api.registry.v1.DeferredRegister;
import com.iamkaf.amber.api.registry.v1.RegistrySupplier;
import com.lumentech.LumenTech;
import com.lumentech.block.SignalReceiverBlock;
import com.lumentech.registry.LumenTechRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Fabric block entity type for the Signal Receiver.
 *
 * <p>Must run after {@code LumenTech.init()} so the block is registered. This class
 * only registers the block entity type and hands the shared block a factory plus a
 * ticker; the "open on placement" behaviour lives in
 * {@link SignalReceiverBlockEntity#serverTick}.</p>
 */
public final class FabricSignalReceiver {
    // See FabricEnergy for why this is BlockEntityType<?> and not the raw type.
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(LumenTech.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<?>> SIGNAL_RECEIVER = BLOCK_ENTITY_TYPES.register("signal_receiver",
            () -> BlockEntityType.Builder.of(SignalReceiverBlockEntity::new, LumenTechRegistry.SIGNAL_RECEIVER.get())
                    .build(null));

    private FabricSignalReceiver() {
    }

    public static void init() {
        BLOCK_ENTITY_TYPES.register();

        // Let the shared block create our GeckoLib-backed block entity.
        // (Only on legacy MC - on 26.x GeckoLib is not wired, so the block has no BE.)
        //? if <1.22 {
        SignalReceiverBlock.ENTITY_FACTORY = SignalReceiverBlockEntity::new;
        // Vanilla 1.20.1 has no BlockEntity#onLoad (that hook is 1.21+/NeoForge), so the
        // "open on placement" trigger rides the block's existing ticker instead: it fires
        // on the first tick after the block is placed and deploys once (the flag persists,
        // so chunk loads don't replay it).
        SignalReceiverBlock.TICKER = SignalReceiverBlockEntity::serverTick;
        //?}
    }
}
