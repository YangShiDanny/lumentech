plugins {
    id("com.iamkaf.multiloader.forge")
}

// Konfig 0.10.2+1.20.1 (forge) is published broken upstream: 381 KB, 14 entries,
// zero .class files (only assets + mods.toml). Compiling against it fails with
// "package com.iamkaf.konfig.api.v1 does not exist", and it would be equally
// unusable at runtime. 0.10.1+1.20.1 is intact (1.06 MB, 370 classes, identical
// api.v1 surface), so pin the forge module to it until the 0.10.2 forge artifact
// is republished.
//
// The rewrite matches the exact broken coordinate instead of the whole module, so it
// is a no-op on every other node: enabling Forge on a newer line (where the catalog
// points at, say, konfig-forge 0.10.2+1.21.1, which is intact) keeps that version
// instead of being dragged back to a 1.20.1 artifact.
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group.startsWith("com.iamkaf") &&
            requested.name == "konfig-forge" &&
            requested.version?.startsWith("0.10.2+1.20.1") == true
        ) {
            useVersion("0.10.1+1.20.1")
            because("konfig-forge 0.10.2+1.20.1 ships with no class files")
        }
    }
}
