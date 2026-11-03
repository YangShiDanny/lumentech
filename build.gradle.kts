plugins {
    id("com.iamkaf.multiloader.root") apply false
}

if (project == rootProject) {
    apply(plugin = "com.iamkaf.multiloader.root")
}
