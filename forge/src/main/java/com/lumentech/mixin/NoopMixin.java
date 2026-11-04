package com.lumentech.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Placeholder mixin.
 *
 * <p>LumenTech has no real mixins yet. The Mixin annotation processor only writes a
 * refmap when at least one {@code @Mixin} class is present, and Legacy Forge's
 * {@code reobfJar} then requires {@code build/mixin/lumentech.refmap.json.mappings.tsrg}
 * to exist. This no-op mixin gives the processor something to map so the refmap (and
 * its SRG variant) are generated and the Forge jar builds. It applies nothing at
 * runtime.</p>
 */
@Mixin(Entity.class)
public abstract class NoopMixin {
}
