# item-use trace — PetMacro source for review

Decompiled source of the `PetMacro` class from **CadiaClient 1.5.75**, shared so the client's developer can see exactly what the class does without decompiling it again.

Nothing here is original source: this is the output of a Java decompiler applied to the shipped JAR, so formatting and some variable names are decompiler artifacts.

## Provenance

| Item | Value |
|---|---|
| Artifact | `CadiaClient-1.5.75.jar` |
| SHA-256 | `c03c2d6f34c4257cb6ad3d5a24237d5d751d112e2e370b8e19b861a6306fa147` |
| Class | `com.minecadiahelper.features.PetMacro` |
| Source file SHA-256 | `e832b109172d6a26541789ba7c612b085ceea7f6400df86489e63e3293832f9e` |

## What the class does

Four things, in this order:

1. **Storage** — a macro is a name, a keybind, and an ordered list of pet names (`CustomPetMacro`, lines 157-200). The list is persisted as JSON at `config/petmacros.json` by `PetMacroConfig`.
2. **Trigger** — a key press arms the macro (`executeByKeybind` → `executeMacro`, lines 46-64). This only sets a flag.
3. **Execution** — on the next client tick, `onClientTick` (lines 72-82) walks the **entire** pet list in one pass and calls `usePet` for each. There is no delay and no early exit; `petMacroMinDelay` / `petMacroMaxDelay` exist in the config but are not read anywhere.
4. **Item use** — `usePet` (lines 115-131) finds the first hotbar slot (0-8) whose custom display name contains the pet name, writes `inventory.currentItem` directly, calls `playerController.sendUseItem(...)`, then restores the previous slot before returning.

No packets are constructed by this class and no input is simulated; there is no scroll, no key injection, and no `rightClickMouse` call.

## Line map

| Lines | Member | Note |
|---|---|---|
| 46-55 | `executeMacro` | arms the macro; requires enabled, in-game, no screen open, not already executing |
| 57-64 | `executeByKeybind` | key code → macro index |
| 72-82 | `onClientTick` | drains the whole list in one tick |
| 90-113 | `releaseAllClicks` | reflectively clears `KeyBinding.pressed` / `pressTime` for attack and use-item |
| 115-131 | `usePet` | slot write, `sendUseItem`, restore |
| 157-200 | `CustomPetMacro` | name + keybind + ordered pet list |

## Packet consequence

`PlayerControllerMP.sendUseItem` is the vanilla right-click entry point. In 1.8.9 it does, in order:

1. `syncCurrentPlayItem()` — sends `C09PacketHeldItemChange` if `inventory.currentItem` differs from the last synced slot
2. sends `C08PacketPlayerBlockPlacement` built from `player.inventory.getCurrentItem()`, with the direction field set to `255` (the "use item" sentinel)
3. runs `ItemStack.useItemRightClick` client-side and writes the consumed stack back

Because the loop runs inside one tick, a 4-pet macro produces 4 `C08` and 5 `C09` packets in a single 50 ms tick, and the server resolves each use because the `C09` precedes its `C08`.

## Reproducing

1. Decompile the JAR with Vineflower or CFR and open `com/minecadiahelper/features/PetMacro.java`.
2. Compare the SHA-256 above to confirm the same build.
3. Read `usePet` (115-131) for the item-use path and `onClientTick` (72-82) for the single-tick loop.
