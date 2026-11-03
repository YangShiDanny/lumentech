plugins {
    id("com.iamkaf.multiloader.forge")
}

// Konfig 0.10.2+1.20.1 (forge) is published broken upstream: 381 KB, 14 entries,
// zero .class files (only assets + mods.toml). Compiling against it fails with
// "package com.iamkaf.konfig.api.v1 does not exist", and it would be equally
// unusable at runtime. 0.10.1+1.20.1 is intact (1.06 MB, 370 classes, identical
// api.v1 surface), so pin the forge module to it until the 0.10.2 forge artifact
// is republished. Scoped to `konfig-forge`, which only this module ever resolves.
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group.startsWith("com.iamkaf") && requested.name == "konfig-forge") {
            useVersion("0.10.1+1.20.1")
            because("konfig-forge 0.10.2+1.20.1 ships with no class files")
        }
    }
}
