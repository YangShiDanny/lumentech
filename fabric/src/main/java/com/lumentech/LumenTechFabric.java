package com.lumentech;

import com.lumentech.fabric.FabricSignalReceiver;
//? if <1.22 {
import com.lumentech.fabric.FabricEnergy;
//?}
import net.fabricmc.api.ModInitializer;

public class LumenTechFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Common registration first so the generator block resolves, then the
        // Fabric-only TechReborn energy wiring (legacy MC only - 26.x has no
        // Reborn Energy port yet).
        LumenTech.init();
        //? if <1.22 {
        FabricEnergy.init();
        //?}
        FabricSignalReceiver.init();
    }
}
