package com.lumentech.config;

import com.iamkaf.konfig.api.v1.ConfigBuilder;
import com.iamkaf.konfig.api.v1.ConfigHandle;
import com.iamkaf.konfig.api.v1.ConfigScope;
import com.iamkaf.konfig.api.v1.ConfigValue;
import com.iamkaf.konfig.api.v1.Konfig;
import com.iamkaf.konfig.api.v1.RestartRequirement;
import com.iamkaf.konfig.api.v1.SyncMode;

/**
 * LumenTech configuration, declared once in common code and synchronized to clients
 * on login via Konfig.
 *
 * <p>Values are read with {@link ConfigValue#get()}; the effective value reflects the
 * server overlay while connected to a remote authority. The generated config screen
 * is automatically wired into Mod Menu (Fabric), and the config button on Forge /
 * NeoForge.</p>
 */
public final class LumenTechConfig {
    public static final ConfigHandle COMMON;

    /** Master toggle for Lumen generators. */
    public static final ConfigValue<Boolean> GENERATOR_ENABLED;
    /** Base energy output per tick (arbitrary FE/t). */
    public static final ConfigValue<Integer> GENERATOR_OUTPUT;
    /** Glow color of Lumen blocks (ARGB hex). */
    public static final ConfigValue<Integer> LUMEN_COLOR;
    /** Preferred energy unit label shown in tooltips. */
    public static final ConfigValue<String> ENERGY_UNIT;

    static {
        ConfigBuilder builder = Konfig.builder("lumentech", "common")
                .scope(ConfigScope.COMMON)
                .syncMode(SyncMode.LOGIN)
                .comment("LumenTech configuration. Tunables for Lumen generators and their visuals.");

        builder.push("generator");
        GENERATOR_ENABLED = builder.bool("enabled", true)
                .comment("Master toggle for Lumen generators.")
                .tooltip("When disabled, no Lumen generator produces energy.")
                .sync(true)
                .build();
        GENERATOR_OUTPUT = builder.intRange("output", 100, 1, 1000)
                .comment("Base energy output per tick (arbitrary FE/t).")
                .tooltip("Higher values make generators produce more energy per tick.")
                .sync(true)
                .restart(RestartRequirement.WORLD)
                .build();
        builder.pop();

        builder.push("appearance");
        LUMEN_COLOR = builder.colorArgb("lumen_color", 0xFF7CFC00)
                .comment("Glow color of Lumen blocks (ARGB hex).")
                .tooltip("ARGB integer; alpha is ignored in-world.")
                .build();
        ENERGY_UNIT = builder.dropdown("energy_unit", "fe", options -> options
                .option("fe", "Forge Energy")
                .option("eu", "EU (IC2-style)")
                .option("rf", "Redstone Flux"))
                .comment("Preferred energy unit label shown in tooltips.")
                .tooltip("Purely cosmetic; changes the unit label in item tooltips.")
                .build();
        builder.pop();

        COMMON = builder.build();
    }
}
