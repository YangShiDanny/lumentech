# LumenTech developer workflows
# Requires: just (https://github.com/casey/just), JDK 21, Git

set shell := ["bash", "-euo", "pipefail", "-c"]

# Show available recipes.
default:
    @just --list

# Print every Minecraft version that has a versions/<mc> node.
list-versions:
    @find versions -mindepth 2 -maxdepth 2 -type f -name 'gradle.properties' -printf '%h\n' | xargs -r -n1 basename | sort -V

# Print the loaders enabled for one version.
list-loaders version:
    @grep '^project.enabled-loaders=' "versions/{{version}}/gradle.properties" | head -n1 | cut -d= -f2- | tr ',' '\n' | sed 's/^[[:space:]]*//; s/[[:space:]]*$//' | sed '/^$/d'

# Print every version-loader node (e.g. 1.20.1-fabric).
list-nodes:
    @for props in versions/*/gradle.properties; do \
        version=$(basename "$(dirname "$props")"); \
        loaders=$(sed -nE 's/^project\.enabled-loaders=(.*)$/\1/p' "$props" | head -n1); \
        for loader in $(printf '%s\n' "$loaders" | tr ',' '\n' | sed 's/^[[:space:]]*//; s/[[:space:]]*$//' | sed '/^$/d'); do \
            echo "$version-$loader"; \
        done; \
    done | sort -V

# Print the Gradle project graph.
list-projects:
    ./gradlew projects --console=plain

# Build the currently active node (the one pinned in stonecutter.gradle.kts).
build:
    ./gradlew build --console=plain

# Build ONE node, e.g. `just build-node 1.20.1-forge`.
build-node node:
    @if ! just list-nodes | grep -Fxq "{{node}}"; then echo "Unknown node: {{node}}"; exit 1; fi
    @version="{{node}}"; loader="${version##*-}"; version="${version%-*}"; \
        ./gradlew --console=plain --configure-on-demand ":$loader:$version:build"

# Build EVERY node in a single Gradle invocation (all versions x all loaders).
# Pass extra args after `--`, e.g. `just build-all -- --stacktrace`.
build-all *args:
    @nodes=$(just list-nodes); \
        if [ -z "$nodes" ]; then echo "No nodes found under versions/"; exit 1; fi; \
        tasks=""; \
        for node in $nodes; do \
            loader="${node##*-}"; version="${node%-*}"; \
            tasks="$tasks :$loader:$version:build"; \
        done; \
        echo "==> Building nodes:"; printf '    %s\n' $nodes; \
        ./gradlew --console=plain --configure-on-demand $tasks {{args}}

# Build every node in parallel, one Gradle process per node.
# Safer for memory than a single huge invocation on constrained machines.
build-all-parallel:
    @just list-nodes | xargs -r -P 4 -I{} just build-node {}

# Boot the client for a specific node, e.g. `just run 1.21.1 fabric`.
run version loader:
    @if ! just list-loaders "{{version}}" | grep -Fxq "{{loader}}"; then echo "Loader {{loader}} is not enabled for {{version}}"; exit 1; fi
    ./gradlew --console=plain --configure-on-demand ":{{loader}}:{{version}}:runClient"

# Remove all build output.
clean:
    ./gradlew clean --console=plain

# Publish the active node's artifacts (needs Modrinth/CurseForge tokens).
publish:
    ./gradlew publishMod --console=plain
