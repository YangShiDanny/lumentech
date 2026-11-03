package com.lumentech;

import com.lumentech.config.LumenTechConfig;
import com.lumentech.registry.LumenTechRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LumenTech common entry point.
 *
 * <p>Shared across Fabric, Forge and NeoForge. Each loader entry point calls
 * {@link #init()} from its own mod-construction phase. Registration is handled by
 * Amber's cross-loader {@code DeferredRegister}; the config file/screen metadata is
 * produced by Konfig when {@link LumenTechConfig} is first touched.</p>
 */
public final class LumenTech {
    public static final String MOD_ID = "lumentech";
    public static final String MOD_NAME = "LumenTech";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        LumenTechRegistry.init();
        // Touch the config handle so Konfig builds config/lumentech/common.toml and
        // registers the generated config screen metadata.
        LumenTechConfig.COMMON.toString();
        LOG.info("{} initialized (config: {})", MOD_NAME, LumenTechConfig.COMMON.getFileName());
    }
}
