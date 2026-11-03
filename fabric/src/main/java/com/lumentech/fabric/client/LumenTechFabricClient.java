package com.lumentech.fabric.client;

import com.lumentech.fabric.FabricSignalReceiver;
import com.lumentech.fabric.SignalReceiverBlockEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * Fabric client entry point. Registers the GeckoLib renderer that draws the Signal
 * Receiver's animated 3D model.
 */
public class LumenTechFabricClient implements ClientModInitializer {
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onInitializeClient() {
        BlockEntityType type = FabricSignalReceiver.SIGNAL_RECEIVER.get();
        BlockEntityRendererRegistry.register(type,
                context -> new GeoBlockRenderer<SignalReceiverBlockEntity>(new SignalReceiverModel()));
    }
}
