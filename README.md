Decompiled the pet macro out of CadiaClient 1.5.75 so you don't have to dig it out yourself. Only file that matters is src/com/minecadiahelper/features/PetMacro.java.

How it works: a macro is a name, a keybind and a list of pet names. Pressing the key just flips a flag. On the next client tick onClientTick runs through the whole list in one go, no delay between pets, and calls usePet for each one. usePet looks through hotbar slots 0-8 for an item whose custom name contains the pet name, sets inventory.currentItem to that slot, calls playerController.sendUseItem, then puts the slot back. It never scrolls, never fakes a click and doesn't build any packets itself.

What the server ends up seeing is the normal vanilla use path: sendUseItem syncs the slot (C09 if it changed), sends C08 with the held stack and direction 255, then runs useItemRightClick and writes the consumed stack back. Since the whole list runs in one tick, 4 pets is 4 C08 and 5 C09 inside 50ms.

Two things worth knowing. petMacroMinDelay and petMacroMaxDelay are in the config but nothing reads them, so there's no pacing at all. And usePet requires hasDisplayName(), so plain splash pots won't match unless they've been renamed.

If you want to check it's the same build: file sha256 e832b109172d6a26541789ba7c612b085ceea7f6400df86489e63e3293832f9e, pulled from the jar c03c2d6f34c4257cb6ad3d5a24237d5d751d112e2e370b8e19b861a6306fa147. It's decompiler output so the formatting and some variable names aren't original.