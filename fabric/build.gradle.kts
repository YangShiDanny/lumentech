plugins {
    id("com.iamkaf.multiloader.fabric")
}

// GeckoLib is published to Cloudsmith; the multiloader convention does not add it.
repositories {
    maven {
        name = "GeckoLib"
        url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
        content {
            includeGroupByRegex("software\\.bernie.*")
            includeGroup("com.eliotlash.mclib")
        }
    }
}

// TechReborn's energy API (the Reborn Energy library). It is bundled into the jar
// via `include` so the mod works standalone and still interops with TechReborn.
// Version is node-specific: 3.x for 1.20.x, 4.1.x for 1.21+.
val energyVersion = (project.findProperty("energy.version") as? String) ?: "3.0.0"
// GeckoLib is version-specific too: 4.8.4 for 1.20.1, 4.9.3 for 1.21.1.
val geckolibVersion = (project.findProperty("geckolib.version") as? String) ?: "4.8.4"

dependencies {
    include(modApi("teamreborn:energy:$energyVersion"))

    // GeckoLib is a normal library mod players install alongside, so it is NOT bundled.
    modImplementation("software.bernie.geckolib:geckolib-fabric-${project.minecraft_version}:$geckolibVersion")

    // mclib is a helper dependency for the 4.8.x (1.20.x) line only.
    if (geckolibVersion.startsWith("4.8")) {
        modImplementation("com.eliotlash.mclib:mclib:20")
    }
}
