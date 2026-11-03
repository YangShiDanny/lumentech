package com.lumentech;

import com.lumentech.fabric.FabricEnergy;
import com.lumentech.fabric.FabricSignalReceiver;
import net.fabricmc.api.ModInitializer;

public class LumenTechFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Common registration first so the generator block resolves, then the
        // Fabric-only TechReborn energy wiring.
        LumenTech.init();
        FabricEnergy.init();
        FabricSignalReceiver.init();
    }
}
