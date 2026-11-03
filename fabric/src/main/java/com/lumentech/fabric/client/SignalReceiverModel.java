package com.lumentech.fabric.client;

// GeckoLib is only wired on legacy MC (1.x) - compiled out on 26.x.
//? if <1.22 {
import com.lumentech.LumenTech;
import com.lumentech.fabric.FabricSignalReceiver;
import com.lumentech.fabric.SignalReceiverBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for the Signal Receiver.
 *
 * <p>GeckoLib 4 looks up block assets by the block entity id, at
 * <ul>
 *   <li>{@code assets/lumentech/geckolib/models/block/signal_receiver.geo.json}</li>
 *   <li>{@code assets/lumentech/geckolib/animations/block/signal_receiver.animation.json}</li>
 *   <li>{@code assets/lumentech/textures/block/signal_receiver.png}</li>
 * </ul>
 * The coordinates below are explicit so the paths are obvious.</p>
 */
public class SignalReceiverModel extends GeoModel<SignalReceiverBlockEntity> {
    private static final ResourceLocation MODEL =
            id("geckolib/models/block/signal_receiver.geo.json");
    private static final ResourceLocation TEXTURE =
            id("textures/block/signal_receiver.png");
    private static final ResourceLocation ANIMATION =
            id("geckolib/animations/block/signal_receiver.animation.json");

    // ResourceLocation.fromNamespaceAndPath only exists on 1.21+; 1.20.1 uses the constructor.
    private static ResourceLocation id(String path) {
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(LumenTech.MOD_ID, path);
        //?} else {
        /*return new ResourceLocation(LumenTech.MOD_ID, path);
        *///?}
    }

    @Override
    public ResourceLocation getModelResource(SignalReceiverBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SignalReceiverBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SignalReceiverBlockEntity animatable) {
        return ANIMATION;
    }
}
//?}
