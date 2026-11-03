# LumenTech

### 需要中文 README 请到 [README.zh-CN.md](README.zh-CN.md)
A cross-version, cross-loader tech mod for Minecraft, built with the
[iamkaf](https://github.com/iamkaf) `multiloader` + Stonecutter toolchain.

- **Cross-loader API** is provided by [Amber](https://modrinth.com/mod/amber) —
  registration, events, networking and more, behind one common API.
- **Configuration** is provided by [Konfig](https://modrinth.com/mod/konfig) —
  typed, commented TOML config with automatic client sync and generated in-game
  config screens (Mod Menu on Fabric; config button on Forge / NeoForge).

LumenTech currently registers a small tech content set (a Lumen ingot, raw Lumen,
a Lumen core and a Lumen generator block) plus a COMMON-scoped, login-synced
configuration.

On **Fabric**, the Lumen generator is energy-capable through
[TechReborn](https://modrinth.com/mod/techreborn)'s Reborn Energy API: it generates
energy each tick (rate from `generator.output`) and pushes it into adjacent machines
that accept Reborn Energy, so it interops directly with TechReborn cables and
machines. On Forge / NeoForge the generator block stays a plain decorative block.

## Version matrix

| Minecraft | Fabric | Forge | NeoForge |
|-----------|:------:|:-----:|:--------:|
| 1.20.1    |   ✅   |  ✅  |    —     |
| 1.21.1    |   ✅   |  —   |    ✅    |
| 26.1.2    |   ✅   |  —   |    ✅    |
| 26.2      |   ✅   |  —   |    ✅    |

> **Feature parity note:** GeckoLib (Signal Receiver animation) and Reborn Energy
> (generator power) are wired on the legacy 1.x lines only. The 26.x builds are
> core-only for now — blocks, items and config register, but the Signal Receiver
> has no block entity and the generator produces no power. GeckoLib 5.x for 26.x
> is a different API (`com.geckolib`) and needs a separate port.

The enabled loaders per line are declared in `versions/<mc>/gradle.properties`
(`project.enabled-loaders`). Add a new line by dropping in another
`versions/<mc>/gradle.properties` node.

## Repository layout

```
lumentech/
├── common/                 # shared code + resources (registries, config, pack.mcmeta)
├── fabric/                 # Fabric entry point + fabric.mod.json
├── forge/                  # Forge entry point + mods.toml
├── neoforge/               # NeoForge entry point + neoforge.mods.toml
├── versions/<mc>/          # per-version metadata (java, ranges, enabled loaders)
├── settings.gradle.kts     # Stonecutter + multiloader project graph
├── stonecutter.gradle.kts  # active node + source parameters
├── build.gradle.kts        # root plugin wiring
├── gradle.properties       # mod metadata + amber/konfig dependencies
└── justfile                # developer workflows
```

## Building

### All nodes at once (GitHub Actions)

`.github/workflows/build.yml` expands the `enabled-loaders` declared in
`versions/*/gradle.properties` into a matrix and runs **one parallel job per
version + loader**:

| Node | |
|---|---|
| `1.20.1-fabric` | 1.20.1 / Fabric |
| `1.20.1-forge` | 1.20.1 / Forge |
| `1.21.1-fabric` | 1.21.1 / Fabric |
| `1.21.1-neoforge` | 1.21.1 / NeoForge |
| `26.1.2-fabric` | 26.1.2 / Fabric |
| `26.1.2-neoforge` | 26.1.2 / NeoForge |
| `26.2-fabric` | 26.2 / Fabric |
| `26.2-neoforge` | 26.2 / NeoForge |

All eight run concurrently with `fail-fast: false`, so one
broken node does not cancel the rest. Each job builds only its own task
(`:<loader>:<version>:build`) and narrows the Stonecutter graph with
`-Pmultiloader.target.versions` / `-Pmultiloader.target.loaders`.

The matrix is produced by `scripts/build-matrix.py`. When triggering the
workflow manually you can fill in `versions` / `loaders` to build a subset
(for example only `1.21.1`, or only `fabric`).

Jars are uploaded per node as `lumentech-<version>-<loader>`.

> Adding a version is just a new `versions/<mc>/gradle.properties` — CI picks the
> new node up automatically, no workflow edit needed.

### Building locally

You need **JDK 21** and **Git**. A Gradle 9.5 wrapper is generated automatically
on first run if it is missing:

```bash
# install the just command runner (optional): https://github.com/casey/just
just build-all             # build all 8 nodes in one Gradle invocation
just build-all-parallel    # one process per node, 4 in parallel (kinder on RAM)
just build-node 1.21.1-forge   # build a single node
just list-nodes            # print every Minecraft/loader node
just run 1.21.1 fabric     # run the client for one node
```

`just build-all` expands to a single command listing every node's task:

```bash
./gradlew --configure-on-demand \
  :fabric:1.20.1:build :forge:1.20.1:build \
  :fabric:1.21.1:build :neoforge:1.21.1:build \
  :fabric:26.1.2:build :neoforge:26.1.2:build \
  :fabric:26.2:build :neoforge:26.2:build
```

If memory is tight (resolving the Minecraft dependencies for all nodes at once is
heavy), use `build-all-parallel` or build nodes individually.Built jars land in `<loader>/versions/<mc>/build/libs/`.

## Configuration

The config lives at `config/lumentech/common.toml` and is generated by Konfig
from `common/src/main/java/com/lumentech/config/LumenTechConfig.java`.

| Key                      | Type   | Default | Notes                                  |
|--------------------------|--------|---------|----------------------------------------|
| `generator.enabled`      | bool   | `true`  | Master toggle, synced to clients.       |
| `generator.output`       | int    | `100`   | Energy/tick, range 1–1000, world restart|
| `appearance.lumen_color` | ARGB   | green   | Glow color of Lumen blocks.             |
| `appearance.energy_unit` | enum   | `fe`    | `fe` / `eu` / `rf` label.               |

Values marked `.sync(true)` are sent to clients on login (`SyncMode.LOGIN`).

## TechReborn energy integration (Fabric)

The energy wiring lives entirely in the `fabric/` module so Forge / NeoForge keep
building:

- `fabric/build.gradle.kts` bundles the Reborn Energy library
  (`teamreborn:energy`, version picked per node from `energy.version` in
  `versions/<mc>/gradle.properties` — 3.0.0 for 1.20.1, 4.1.0 for 1.21.1).
- `common/.../block/LumenGeneratorBlock.java` is an `EntityBlock` with a
  loader-injected block-entity factory and ticker (null on Forge / NeoForge).
- `fabric/.../fabric/LumenGeneratorBlockEntity.java` holds a
  `SimpleEnergyStorage`, generates energy and pushes it to neighbours.
- `fabric/.../fabric/FabricEnergy.java` registers the block-entity type and exposes
  it through `EnergyStorage.SIDED`, then injects the factory/ticker into the common
  block during `onInitialize`.

Energy is **not** persisted across world reloads in this scaffold (a generator just
regenerates it). Add `saveAdditional` / `loadAdditional` guarded by a Stonecutter
`//? if >=1.21` / `//? if <1.21` block if you need buffered energy to survive
restarts — the `HolderLookup.Provider` parameter only exists on 1.20.5+.

## License

MIT — see [LICENSE](LICENSE).
