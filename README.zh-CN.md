# LumenTech（光科技）

一个跨版本、跨加载器的 Minecraft 科技模组，基于
[iamkaf](https://github.com/iamkaf) 的 `multiloader` + Stonecutter 工具链构建。

- **跨加载器 API**：[Amber](https://modrinth.com/mod/amber) —— 注册、事件、网络等功能统一在
  一套 common API 后面，Fabric / Forge / NeoForge 共用同一份逻辑。
- **配置系统**：[Konfig](https://modrinth.com/mod/konfig) —— 带注释的 TOML 配置、类型化取值、
  自动客户端同步，以及游戏内配置界面（Fabric 走 Mod Menu，Forge / NeoForge 走配置按钮）。
- **动画 3D 模型**（仅 Fabric）：[GeckoLib 4](https://modrinth.com/mod/geckolib) 驱动，
  方块可以带骨骼动画。
- **能量 API**（仅 Fabric）：TechReborn 的 Reborn Energy（`teamreborn:energy`），
  与 TechReborn 的机器和线缆直接互通。

## 内容一览

| 注册名 | 类型 | 说明 |
|---|---|---|
| `lumen_ingot` | 物品 | 流明锭 |
| `raw_lumen` | 物品 | 粗流明 |
| `lumen_core` | 物品 | 流明核心 |
| `lumen_generator` | 方块 | 流明发电机，Fabric 端可产电并推送给相邻机器 |
| `signal_receiver` | 方块 | 信号接收器，放下后自动播放 `open` 动画展开碟形天线 |

## 版本矩阵

| Minecraft | Fabric | Forge | NeoForge |
|-----------|:------:|:-----:|:--------:|
| 1.20.1    |   ✅   |  ✅  |    —     |
| 1.21.1    |   ✅   |  ✅  |    ✅    |

每个版本启用哪些加载器，由 `versions/<mc>/gradle.properties` 里的
`project.enabled-loaders` 决定。想加新版本，只要再放一份
`versions/<mc>/gradle.properties` 节点即可。

## 仓库结构

```
lumentech/
├── common/                 # 共享代码 + 资源（注册、配置、模型贴图、语言文件）
├── fabric/                 # Fabric 入口、GeckoLib 渲染、Reborn Energy 能量实现
├── forge/                  # Forge 入口 + mods.toml
├── neoforge/               # NeoForge 入口 + neoforge.mods.toml
├── versions/<mc>/          # 每个版本的元数据（Java 版本、依赖区间、启用加载器）
├── settings.gradle.kts     # Stonecutter + multiloader 项目图
├── stonecutter.gradle.kts  # 当前编译节点与源码参数
├── build.gradle.kts        # 根插件装配
├── gradle.properties       # 模组元数据 + Amber/Konfig 依赖声明
└── justfile                # 常用开发命令
```

## 构建

### 一次构建全部节点（GitHub Actions）

`.github/workflows/build.yml` 会自动把 `versions/*/gradle.properties` 里声明的
`enabled-loaders` 展开成矩阵，**每个「版本 + 加载器」一个并行 job**：

| 当前节点 | 说明 |
|---|---|
| `1.20.1-fabric` | 1.20.1 / Fabric |
| `1.20.1-forge` | 1.20.1 / Forge |
| `1.21.1-fabric` | 1.21.1 / Fabric |
| `1.21.1-forge` | 1.21.1 / Forge |
| `1.21.1-neoforge` | 1.21.1 / NeoForge |

5 个 job 用 `max-parallel: 5` 同时跑，`fail-fast: false` 保证某个节点失败不会
取消其它节点。每个节点只构建自己的任务
（`:<loader>:<version>:build`），并用
`-Pmultiloader.target.versions` / `-Pmultiloader.target.loaders` 收窄配置图，
避免每次都配置全部版本。

矩阵由 `scripts/build-matrix.py` 生成。手动触发 workflow 时可以填
`versions` / `loaders` 来只构建一部分，例如只跑 `1.21.1`，或只跑 `fabric`。

产物按节点分别上传，artifact 名为 `lumentech-<version>-<loader>`。

> 加新版本只需新建 `versions/<mc>/gradle.properties`，CI 会自动把新节点加进矩阵，
> 不需要改 workflow。

### 本地构建

本地构建需要 **JDK 21** 和 **Git**。首次运行若缺少 Gradle 9.5 wrapper，会自动生成。

```bash
# 安装 just 命令工具（可选）：https://github.com/casey/just
just build-all             # 一次 Gradle 调用构建全部 5 个节点
just build-all-parallel    # 每个节点一个进程，4 路并行（内存紧张时更稳）
just build-node 1.21.1-forge   # 只构建单个节点
just list-nodes            # 列出所有节点
just run 1.21.1 fabric     # 启动某个节点的客户端
```

`just build-all` 实际执行的是把所有节点任务拼成一条命令：

```bash
./gradlew --configure-on-demand \
  :fabric:1.20.1:build :forge:1.20.1:build \
  :fabric:1.21.1:build :forge:1.21.1:build :neoforge:1.21.1:build
```

如果内存吃紧（5 个节点的 Minecraft 依赖同时解析比较重），改用
`build-all-parallel`，或者单独跑 `build-node`。

产物位于 `<loader>/versions/<mc>/build/libs/`。

## 配置

配置文件由 Konfig 依据
`common/src/main/java/com/lumentech/config/LumenTechConfig.java` 生成，
路径为 `config/lumentech/common.toml`。

| 键 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `generator.enabled` | bool | `true` | 发电机总开关，同步到客户端 |
| `generator.output` | int | `100` | 每 tick 产电，范围 1–1000，改动需重载世界 |
| `appearance.lumen_color` | ARGB | 绿色 | 流明方块的光晕颜色 |
| `appearance.energy_unit` | enum | `fe` | `fe` / `eu` / `rf` 单位显示 |

带 `.sync(true)` 的项会在玩家登录时下发给客户端（`SyncMode.LOGIN`）。

## 信号接收器（Fabric）

模型来自 Blockbench 工程（`Signal Receiver.bbmodel`），资源放在
`common/src/main/resources/assets/lumentech/`：

```
geckolib/models/block/signal_receiver.geo.json          # 几何体（由 bbmodel 转换）
geckolib/animations/block/signal_receiver.animation.json # 动画（原始文件直接拷贝）
textures/block/signal_receiver.png                       # 64×64 贴图
blockstates/signal_receiver.json                         # 方块状态（ENTITYBLOCK_ANIMATED）
models/block/signal_receiver.json                        # 占位模型（只用于粒子贴图）
```

行为流程：

1. 方块是 `EntityBlock`，`getRenderShape()` 返回 `ENTITYBLOCK_ANIMATED`，
   因此只画 GeckoLib 模型，不画原版方块模型。
2. Fabric 端由 `FabricSignalReceiver` 注册方块实体类型，并把实体工厂
   （`SignalReceiverBlockEntity::new`）注入到共享的 `SignalReceiverBlock` 中。
3. `SignalReceiverBlockEntity#onLoad()` 在服务端首次加载时调用一次，
   把 `opened` 标记置为 `true` 并同步给周围客户端 —— 也就是**方块刚放下就展开**。
4. 动画控制器在 `opened` 为真时播放 `open` 动画，`thenPlayAndHold` 对应
   Blockbench 里的 `hold_on_last_frame`，所以会停在展开后的最后一帧。
5. `opened` 会写入 NBT，因此重进世界不会重复播动画。

**关于 `rotation` 动画**：`model.animation.json` 里还有一个碟形天线跟踪目标的
`rotation` 动画，目前**故意没有接入**，等后续需要"信号追踪"逻辑时再加。

**能量持久化**：发电机当前不做能量存档（重启即重新发电）。若需要，可按
Stonecutter 的 `//? if >=1.21` / `//? if <1.21` 条件编译补
`saveAdditional` / `loadAdditional` —— `HolderLookup.Provider` 参数只存在于 1.20.5+。

## 依赖说明

- **Amber / Konfig**：由根 `gradle.properties` 的
  `dependencies.modrinth.required=amber,konfig` 声明，multiloader 插件会按节点
  自动解析出对应加载器、对应版本的构件，无需手写 maven 坐标。
- **Reborn Energy**（`teamreborn:energy`）：仅 Fabric 使用，通过
  `include(modApi(...))` **打包进 jar**，因此独立安装也能工作，同时能和
  TechReborn 互通。版本按节点取：1.20.1 用 `3.0.0`，1.21.1 用 `4.1.0`。
- **GeckoLib**：仅 Fabric 使用，**不打包**（属于玩家自行安装的库模组）。
  版本同样按节点取：1.20.1 用 `4.8.4`，1.21.1 用 `4.9.3`，仓库为 Cloudsmith。

版本号统一写在 `versions/<mc>/gradle.properties` 的 `energy.version` 与
`geckolib.version` 中，改版本只需动这两行。

### GeckoLib 4.8 与 4.9 的包路径差异

这两个小版本的 API 包结构不同，代码里用 Stonecutter 条件编译兼容：

| | 1.20.1（4.8.4） | 1.21.1（4.9.3） |
|---|---|---|
| 动画相关 | `software.bernie.geckolib.core.animation.*` | `software.bernie.geckolib.animation.*` |
| 实例缓存 | `...core.animatable.instance.*` | `...animatable.instance.*` |
| `PlayState` | `...core.object.PlayState` | `...animation.PlayState` |
| `ResourceLocation` | `new ResourceLocation(ns, path)` | `ResourceLocation.fromNamespaceAndPath(...)` |
| `BlockEntity#load` | `load(CompoundTag)` | `loadAdditional(tag, registries)` |

## 许可证

MIT —— 见 [LICENSE](LICENSE)。
