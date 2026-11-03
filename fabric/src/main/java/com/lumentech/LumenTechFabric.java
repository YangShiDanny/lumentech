package com.lumentech;

import net.fabricmc.api.ModInitializer;

public class LumenTechFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        LumenTech.init();
    }
}
