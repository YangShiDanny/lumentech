# LumenTech developer workflows
# Requires: just (https://github.com/casey/just), JDK 21, Git

# Show available recipes.
default:
    @just --list

# Print every enabled Minecraft/loader node.
list-nodes:
    ./gradlew projects

# Build the currently active node across all of its enabled loaders.
build:
    ./gradlew build

# Build a single loader of the active node.
build-loader loader:
    ./gradlew :{{loader}}:build

# Boot the client for a specific node + loader (needs a Minecraft client).
run version loader:
    ./gradlew :{{version}}-{{loader}}:runClient

# Publish the active node's artifacts (needs Modrinth/CurseForge tokens).
publish:
    ./gradlew publishMod
