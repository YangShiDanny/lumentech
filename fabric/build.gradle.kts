plugins {
    id("com.iamkaf.multiloader.fabric")
}

// TechReborn's energy API (the Reborn Energy library). It is bundled into the jar
// via `include` so the mod works standalone and still interops with TechReborn.
// Version is node-specific: 3.x for 1.20.x, 4.1.x for 1.21+.
val energyVersion = (project.findProperty("energy.version") as? String) ?: "3.0.0"

dependencies {
    include(modApi("teamreborn:energy:$energyVersion"))
}
