plugins {
    id("com.iamkaf.multiloader.fabric")
}

// GeckoLib lives on Cloudsmith; the multiloader conventions do not know about it.
repositories {
    maven {
        name = "GeckoLib"
        url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
        content {
            includeGroupByRegex("software\\.bernie(\\..*)?")
            includeGroup("com.eliotlash.mclib")
        }
    }
}

// GeckoLib + Reborn Energy are only wired for the 1.x lines.
//
// - GeckoLib 5.x (used on 26.x) moved to the `com.geckolib` group and is a different
//   API, so the Signal Receiver's animated model needs a separate port; the 1.x code
//   targets GeckoLib 4.x (`software.bernie.geckolib`).
// - Reborn Energy (teamreborn:energy) is gated out on 26.x for the same reason, so 26.x
//   builds as a feature-free core mod — matching the neoforge behaviour.
//
// The `mc.startsWith("1.")` check keeps these dependencies off the 26.x nodes, where the
// corresponding Java sources are excluded by Stonecutter `//? if <1.22` guards.
// Version-specific properties live in versions/<node>/gradle.properties and are
// NOT inherited by the loader node project (`providers.gradleProperty` cannot see
// them), so read that file directly. The node name (project.name) is the
// Minecraft version and doubles as the fallback for `project.minecraft`.
// (Parsed by hand: java.util.Properties is not resolvable in this buildscript.)
val nodeProps: Map<String, String> =
    rootProject.file("versions/${project.name}/gradle.properties")
        .takeIf { it.exists() }
        ?.readLines()
        ?.mapNotNull { raw ->
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("!") || "=" !in line) {
                null
            } else {
                val (k, v) = line.split("=", limit = 2)
                k.trim() to v.trim()
            }
        }
        ?.toMap()
        ?: emptyMap()

val mc = nodeProps["project.minecraft"] ?: project.name
val isLegacyMc = mc.startsWith("1.")

val energyVersion = nodeProps["energy.version"] ?: "3.0.0"
val geckolibVersion = nodeProps["geckolib.version"] ?: "4.8.4"

dependencies {
    if (isLegacyMc) {
        // GeckoLib is a normal library mod players install alongside, so it is NOT bundled.
        // NOTE: use add("configuration", ...) instead of the generated Kotlin accessors
        // (modImplementation/include) — those accessors are not reliably generated for
        // every Stonecutter node and fail the script compilation on some of them.
        add("modImplementation", "software.bernie.geckolib:geckolib-fabric-$mc:$geckolibVersion")
        if (geckolibVersion.startsWith("4.8")) {
            // mclib is a helper dependency for the 4.8.x (1.20.x) line only.
            add("modImplementation", "com.eliotlash.mclib:mclib:20")
        }

        // Reborn Energy is bundled with jar-in-jar so LumenTech keeps its energy storage
        // without TechReborn installed, while still interoping with it when present.
        add("modImplementation", "teamreborn:energy:$energyVersion")
        add("include", "teamreborn:energy:$energyVersion")
    }
}
