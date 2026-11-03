package com.lumentech;

import net.minecraftforge.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;

@Mod(LumenTech.MOD_ID)
public class LumenTechNeoForge {
    public LumenTechNeoForge(IEventBus modEventBus) {
        LumenTech.init();
    }
}
