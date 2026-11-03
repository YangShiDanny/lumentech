# LumenTech — 蓝图系统 TODO

> **当前状态：**「起点蓝图」物品已存在（16×16 纹理、模型、注册项、中英文名）。
> 目前是一个没有任何行为的普通 `Item`。下面的解锁逻辑是下一步。

---

## 1. 一句话设计

**蓝图 = 知识。** 它为该玩家永久解锁「某台机器可以造」的**资格**。
**专用工作台 = 制造设备。** 解锁后，在工作台上用普通材料把它造出来。

必须同时具备两把「钥匙」：

| 钥匙 | 本质 | 存在哪里 |
|---|---|---|
| 蓝图 | *知识* ——「这台机器允许被造」 | 玩家身上的永久数据 |
| 工作台 | *设备* —— 真正动手制造的工作站 | 世界里的一个方块 |

原版工作台只负责拼装零件/材料，永远造不出机器。机器需要
**知识 + 工作台 + 材料** 三者同时满足。

---

## 2. 故事背景 —— 为什么会有蓝图

一个早已消失的外星文明 —— **流光（Lumen）** —— 在太阳系播撒了以光为本的技术。
他们在月球上的前哨至今仍囚着流动的光。

人类找到的不是纸质图纸，而是 **光刻碎片**：外星晶圆上的碎片，
其电路纹路**以光的形式编码知识**。当碎片被共鸣（右键）时，纹路被写入持有者脑中 —— 永久。

进度主线：

1. 拾到一块坠落的**光刻碎片** → 学会第一台机器。
2. 复原流光科技：能**切断沿途方块**、**击杀路径上生物**、**运输**物质/能量、**加工**材料的光。
3. 组装登月载具 → 抵达**月面前哨** → 揭开这个文明的结局。

蓝图系统就是这条主线的**脊椎**：每一个里程碑，都是一段被找回的记忆。

---

## 3. 核心规则

### 3.1 蓝图 = 知识（一次性、永久）
- 获得一次，对该玩家永久有效。
- 以 `unlockedBlueprints` 列表存储（一串蓝图 ID）。
- **使用碎片是幂等的** —— 第二次使用什么也不做，只是确认「已解锁」。

### 3.2 专用工作台 = 制造设备
- 解锁后，在**光刻工作台**上制造机器。
- 解锁前，配方隐藏 / 显示为 `???`。
- 工作台本身不携带知识 —— 知识在玩家身上。

### 3.3 「玩家把蓝图弄丢了怎么办？」
这正是把知识存在**玩家身上、而不是物品上**的全部理由：

- 丢掉/销毁实体碎片，**不会**失去解锁。
- 重新找到碎片是可选的；再次使用是安全的空操作。
- 知识在死亡、重生、重进世界后依然存在（按玩家 UUID 记录）。
- 可选的兜底：`/lumentech blueprint` 命令或一个「知识备份」装置，
  这样存档损坏时不用重新满世界找碎片。

---

## 4. 数据结构

- **蓝图 ID：** `ResourceLocation`，例如 `lumentech:signal_receiver`。
- **存储键：** `unlockedBlueprints` → ID 字符串列表。
- **作用域：** 按玩家 UUID 记录（死亡/重生后仍在）。

### 4.1 跨加载器存储 —— 三选一

本项目是多加载器，必须选一个在三个加载器都能编译的方案：

- **方案 A —— 以 UUID 为键的 `SavedData`（推荐）。**
  `BlueprintSaveData extends SavedData`，内部 `Map<UUID, Set<String>>`，
  通过 `server.overworld().getDataStorage().computeIfAbsent(...)` 取得。纯原版 API，
  Fabric / Forge / NeoForge 代码完全一致，无需 mixin。按 UUID 记录，死亡重生不丢。
- **方案 B —— Forge/NeoForge 原生：** `player.getPersistentData()`（`CompoundTag` +
  `ListTag`）。这就是需求里说的「玩家 persistent data」的字面实现，但 Fabric 没有。
- **方案 C —— Fabric 原生：** 用 mixin 给 `ServerPlayerEntity` 加一个 `CompoundTag`
  字段，在 `writeCustomDataToNbt` / `readCustomDataFromNbt` 里持久化。

> 建议：在 `common` 里实现 **方案 A**，并用项目已有的
> 「加载器注入模式」（`common` 里放静态函数字段，各加载器在 `init()` 覆盖）暴露出去。
> 一套代码，三个加载器通吃 —— 和 `ENTITY_FACTORY` / `TICKER` 同一套路。

### 4.2 建议 API（common）

```java
public final class BlueprintData {
    private static final String KEY = "unlockedBlueprints";

    public static boolean has(Player player, ResourceLocation id) { ... }
    public static boolean unlock(Player player, ResourceLocation id) { ... } // 已存在返回 false
    public static List<ResourceLocation> all(Player player) { ... }
    public static boolean revoke(Player player, ResourceLocation id) { ... } // 仅调试
}
```

---

## 5. 实施步骤

- [x] **步骤 0 —— 纹理。** `textures/item/starting_blueprint.png`
      （16×16，深色破损晶圆碎片 + 青色光刻纹路）。另出 24× 放大预览，仅供查看。
- [x] **步骤 1 —— 物品占位。** `models/item/starting_blueprint.json`（`item/generated`），
      注册为 `STARTING_BLUEPRINT`（普通 `Item`，`stacksTo(1)`），加入创造模式物品栏，
      英文 `Starting Blueprint` / 中文 `起点蓝图`。
- [ ] **步骤 2 —— 右键解锁。** 把普通 `Item` 换成 `StartingBlueprintItem extends Item`；
      在 `use(...)` 里（仅服务端）：
  - `BlueprintData.unlock(player, BLUEPRINT_ID)` —— 例如 `lumentech:signal_receiver`。
  - 发一条聊天/动作栏消息：「已解锁：信号接收器」/「Blueprint unlocked: Signal Receiver」。
  - 播放一个音效 + 可选的光环粒子。
  - 返回 `InteractionResultHolder.sidedSuccess(...)` 让手臂摆动。
  - 碎片是**保留**还是**消耗**？建议**保留**（知识钥匙，多一个也无害）。
- [ ] **步骤 3 —— 存储。** 实现 `BlueprintData` + `BlueprintSaveData`，并通过
      加载器注入字段接到 Fabric / Forge / NeoForge 的 `init()`。
- [ ] **步骤 4 —— 无 GUI 验证。** 解锁时打日志
      （`LOG.info("Unlocked blueprint {} for {}", id, player.getUUID())`）；如果命令接线不贵，
      再加 `/lumentech blueprint list|grant <id>|revoke <id>`。
      验收：右键 → 列表含该 ID → 重进世界仍在 → 第二次使用是空操作。
- [ ] **步骤 5 —— 光刻工作台。** `LumenFabricatorBlock` + `BlockEntity` + 容器/菜单
      （自定义配方类型）。这就是「制造设备」。
- [ ] **步骤 6 —— 配方门控。** 机器配方**只有**在
      `BlueprintData.has(player, id)` **且**玩家处于工作台时才可制造；否则显示锁定/`???`。
      这一步才让蓝图真正「有意义」。
- [ ] **步骤 7 —— 内容。** 更多蓝图 + 月球进度（登月载具、月面前哨维度/结构）。
- [ ] **步骤 8 —— 百科 GUI（后期）。** 一个蓝图百科界面，列出已知/未知机器，
      每个碎片配一段背景文字。第一版明确不做。

---

## 6. 蓝图 ID 注册表（发布后保持稳定）

| ID | 解锁内容 | 状态 |
|---|---|---|
| `lumentech:signal_receiver` | 信号接收器（起点蓝图的测试目标） | 计划中 |
| `lumentech:lumen_generator` | 流明发电机 | 计划中 |

> 一旦发布，**永远不要改 ID** —— 它已经写进玩家数据。

---

## 7. 验收标准（步骤 2–4）

- [ ] 生存模式：捡起碎片 → 右键 → 聊天提示「已解锁」。
- [ ] `unlockedBlueprints` 含 `lumentech:signal_receiver`（日志或命令可见）。
- [ ] 重进 / 重启 / 死亡重生 → 仍然解锁。
- [ ] 再次右键 → 「已解锁」，列表不变，不重复。
- [ ] 丢弃/销毁物品不会移除解锁。
- [ ] 创造物品栏里物品纹理正确、名称本地化正确。

---

## 8. 跨加载器 / 版本注意

- **Fabric** 没有 `getPersistentData()` → 用 `SavedData`（方案 A）或 mixin（方案 C）。
- **1.20.1 与 1.21.1** 的 API 差异必须用 Stonecutter 守卫
  （`//? if <1.21 { ... } //?}`），与项目其他部分一致。
- 物品 ID 与蓝图 ID 都是持久化的 —— 发布前冻结。
- `Item.use(Level, Player, InteractionHand)` 在 1.20.1 和 1.21.1 签名一致，
  物品类本身应几乎不需要版本守卫。

---

## 9. 待定问题

1. 使用时**保留**还是**消耗**碎片？（建议保留 —— 它是记忆，多一个也无害。）
2. 知识只**按玩家**独立，还是多人里可共享（团队解锁）？
3. 工作台需要**自己的配方类型**，还是第一版先用「自定义检查」门控原版配方？
4. 首个测试是走**原版配方书解锁**（好验证），还是直接上**工作台门控的自定义配方**？
