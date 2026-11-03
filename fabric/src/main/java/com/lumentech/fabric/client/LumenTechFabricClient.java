package com.lumentech.fabric.client;

// GeckoLib is only wired on legacy MC (1.x) - 26.x keeps an empty client entrypoint
// shell so fabric.mod.json still resolves the entrypoint class.
//? if <1.22 {
import com.lumentech.LumenTech;
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
//?} else {
import net.fabricmc.api.ClientModInitializer;

/** 26.x shell: no GeckoLib renderer yet. */
public class LumenTechFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
    }
}
//?}
