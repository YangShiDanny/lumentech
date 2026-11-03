# LumenTech — Blueprint System TODO

> **Status:** the "Starting Blueprint" item now exists (16×16 texture, model, registry
> entry, en/zh names). It is a plain `Item` with no behaviour yet. The unlock logic
> below is the next step.

---

## 1. One-line design

**Blueprint = knowledge.** It permanently unlocks the *right* to build a machine for
that player. **The dedicated workbench = the manufacturing device.** Once unlocked,
you build the machine there from ordinary materials.

Two independent keys must both be present:

| Key | What it is | Where it lives |
|---|---|---|
| Blueprint | *knowledge* — "this machine may be built" | per-player permanent data |
| Fabricator | *device* — the station that physically builds it | a block in the world |

Vanilla crafting tables only assemble parts/materials; they can never produce a
machine. Machines require **knowledge + fabricator + materials**.

---

## 2. Lore — why blueprints exist

A vanished alien civilisation — the **Lumen (流光)** — seeded the solar system with
light-based technology. Their outpost on the Moon still hums with trapped light.

What humanity recovers are not paper schematics but **photolithography shards**: slivers
of alien wafer whose circuit patterns *encode knowledge as light*. When a shard is
resonated (right-clicked), the pattern is written into the holder's mind — permanently.

Progression arc:

1. Find a fallen **photolithography shard** → learn the first machine.
2. Rebuild Lumen tech: light that **cuts through blocks**, **kills mobs along its path**,
   **transports** matter/energy, and **processes** materials.
3. Assemble a launch vehicle → reach the **Moon outpost** → uncover the civilisation's fate.

The blueprint system is the *spine* of this arc: every milestone is a recovered memory.

---

## 3. Core rules

### 3.1 Blueprint = knowledge (one-time, permanent)
- Acquired once, valid forever for that player.
- Stored as `unlockedBlueprints`: a list of blueprint IDs.
- **Using the shard is idempotent** — a second use does nothing but confirm.

### 3.2 Dedicated workbench = manufacturing device
- After unlocking, the machine is built at the **Lumen Fabricator** (光刻工作台).
- Before unlocking, the recipe is hidden / shown as `???`.
- The fabricator holds no knowledge itself — knowledge is on the player.

### 3.3 "What if the player loses the blueprint?"
This is the whole reason knowledge is stored **on the player, not on the item**:

- Losing/destroying the physical shard does **not** revoke the unlock.
- Re-finding a shard is optional; re-using it is a harmless no-op.
- Knowledge survives death, respawn and world reload (keyed by player UUID).
- Optional safety net: a `/lumentech blueprint` command or a "knowledge backup"
  device so a corrupted save can be repaired without hunting shards again.

---

## 4. Data model

- **Blueprint ID:** `ResourceLocation`, e.g. `lumentech:signal_receiver`.
- **Storage key:** `unlockedBlueprints` → list of ID strings.
- **Scope:** per player, keyed by UUID (survives death/respawn).

### 4.1 Cross-loader storage — pick one

The project is multiloader, so choose the approach that compiles on all three:

- **Option A — `SavedData` keyed by UUID (recommended).**
  `BlueprintSaveData extends SavedData` holding `Map<UUID, Set<String>>`, obtained from
  `server.overworld().getDataStorage().computeIfAbsent(...)`. Pure vanilla API → works on
  Fabric, Forge and NeoForge with identical code, no mixin. Keyed by UUID so it survives
  death/respawn.
- **Option B — Forge/NeoForge native:** `player.getPersistentData()` (`CompoundTag` + a
  `ListTag`). This is the literal "player persistent data" the brief mentions, but it does
  not exist on Fabric.
- **Option C — Fabric native:** mixin `ServerPlayerEntity` to add a `CompoundTag` field
  and persist it in `writeCustomDataToNbt` / `readCustomDataFromNbt`.

> Recommendation: implement **Option A** in `common`, and expose it through the same
> loader-injection pattern already used for `ENTITY_FACTORY` / `TICKER` (static function
> fields in `common`, overwritten by each loader's `init()`). One code path, all loaders.

### 4.2 Suggested API (common)

```java
public final class BlueprintData {
    private static final String KEY = "unlockedBlueprints";

    public static boolean has(Player player, ResourceLocation id) { ... }
    public static boolean unlock(Player player, ResourceLocation id) { ... } // false if already present
    public static List<ResourceLocation> all(Player player) { ... }
    public static boolean revoke(Player player, ResourceLocation id) { ... } // debug only
}
```

---

## 5. Implementation steps

- [x] **Step 0 — Texture.** `textures/item/starting_blueprint.png` (16×16, dark broken
      wafer chip with cyan lithography traces). Preview: 24× upscale for review only.
- [x] **Step 1 — Item stub.** `models/item/starting_blueprint.json` (`item/generated`),
      registered as `STARTING_BLUEPRINT` (plain `Item`, `stacksTo(1)`), added to the
      creative tab, en `Starting Blueprint` / zh `起点蓝图`.
- [ ] **Step 2 — Right-click unlock.** Replace the plain `Item` with
      `StartingBlueprintItem extends Item`; on `use(...)` (server side only):
  - `BlueprintData.unlock(player, BLUEPRINT_ID)` — e.g. `lumentech:signal_receiver`.
  - Send a chat/action-bar message: "已解锁：信号接收器" / "Blueprint unlocked: Signal Receiver".
  - Play a small sound + optional particle pulse.
  - Return `InteractionResultHolder.sidedSuccess(...)` so the arm swings.
  - Keep the shard (knowledge key) or consume it — decide; recommend **keep**.
- [ ] **Step 3 — Storage.** Implement `BlueprintData` + `BlueprintSaveData` and wire it
      through the loader-injection fields (Fabric / Forge / NeoForge `init()`).
- [ ] **Step 4 — Verify without GUI.** Log on unlock
      (`LOG.info("Unlocked blueprint {} for {}", id, player.getUUID())`) and add
      `/lumentech blueprint list|grant <id>|revoke <id>` if command wiring is cheap.
      Acceptance: right-click → list contains the ID → survives relog → second use is a no-op.
- [ ] **Step 5 — Lumen Fabricator.** `LumenFabricatorBlock` + `BlockEntity` + container /
      menu (custom recipe type). This is the "manufacturing device".
- [ ] **Step 6 — Recipe gating.** A machine recipe is craftable **only** when
      `BlueprintData.has(player, id)` **and** the player is at the fabricator. Otherwise
      show the locked/`???` state. This is what makes blueprints matter.
- [ ] **Step 7 — Content.** More blueprints + the Moon progression (launch vehicle,
      lunar outpost dimension/structure).
- [ ] **Step 8 — Codex GUI (later).** A blueprint codex screen listing known/unknown
      machines, with lore text per shard. Explicitly out of scope for the first pass.

---

## 6. Blueprint ID registry (keep stable once released)

| ID | Unlocks | Status |
|---|---|---|
| `lumentech:signal_receiver` | Signal Receiver (test target for the Starting Blueprint) | planned |
| `lumentech:lumen_generator` | Lumen Generator | planned |

> Once shipped, **never rename an ID** — it is persisted in player data.

---

## 7. Acceptance criteria (Step 2–4)

- [ ] Survival: pick up the shard → right-click → chat says "unlocked".
- [ ] `unlockedBlueprints` contains `lumentech:signal_receiver` (log or command).
- [ ] Relog / restart / die-and-respawn → still unlocked.
- [ ] Right-click again → "already unlocked", list unchanged, no duplicate.
- [ ] Dropping / destroying the item does not remove the unlock.
- [ ] Creative tab shows the item with the correct texture and localized name.

---

## 8. Cross-loader / version notes

- **Fabric** has no `getPersistentData()` → use `SavedData` (Option A) or a mixin (Option C).
- **1.20.1 vs 1.21.1** API differences must be Stonecutter-guarded
  (`//? if <1.21 { ... } //?}`), same as the rest of the project.
- Item IDs and blueprint IDs are persistent — freeze them before release.
- `Item.use(Level, Player, InteractionHand)` has the same signature on 1.20.1 and 1.21.1,
  so the item class should need few guards.

---

## 9. Open questions

1. On use, **keep** the shard or **consume** it? (Recommend: keep — it is a memory, and
   duplicates are harmless.)
2. Should knowledge be **per-player** only, or also shareable in multiplayer (team unlock)?
3. Does the fabricator need its own **recipe type**, or can we gate vanilla recipes with a
   custom check for the first pass?
4. Should the first test unlock a **vanilla recipe-book entry** (fast to verify) or go
   straight to the **fabricator-gated custom recipe**?
