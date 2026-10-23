package com.minecadiahelper.features;

import com.minecadiahelper.auth.LicenseGate;
import com.minecadiahelper.config.PetMacroConfig;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;

public class PetMacro {
   private Minecraft mc = Minecraft.getMinecraft();
   private boolean enabled = false;
   private boolean isExecuting = false;
   private List<PetMacro.CustomPetMacro> macros = new ArrayList<>();
   private PetMacro.CustomPetMacro currentExecutingMacro = null;
   private int currentPetIndex = 0;

   public PetMacro() {
      List<PetMacro.CustomPetMacro> savedMacros = PetMacroConfig.loadMacros();
      if (savedMacros.isEmpty()) {
         PetMacro.CustomPetMacro defaultMacro = new PetMacro.CustomPetMacro("Default", 0);
         defaultMacro.addPet("Creeper Pet");
         defaultMacro.addPet("Pikachu Pet");
         defaultMacro.addPet("Scream Pet");
         defaultMacro.addPet("Shield Pet");
         this.macros.add(defaultMacro);
      } else {
         this.macros.addAll(savedMacros);
      }
   }

   public void toggle() {
      this.enabled = !this.enabled;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public void executeMacro(int macroIndex) {
      if (this.enabled && this.mc.thePlayer != null && macroIndex >= 0 && macroIndex < this.macros.size() && this.mc.currentScreen == null && !this.isExecuting
         )
       {
         this.currentExecutingMacro = this.macros.get(macroIndex);
         this.isExecuting = true;
         this.currentPetIndex = 0;
         this.releaseAllClicks();
      }
   }

   public void executeByKeybind(int key) {
      for (PetMacro.CustomPetMacro macro : this.macros) {
         if (macro.getKeybind() == key) {
            this.executeMacro(this.macros.indexOf(macro));
            return;
         }
      }
   }

   public void execute() {
      if (!this.macros.isEmpty()) {
         this.executeMacro(0);
      }
   }

   public void onClientTick() {
      if (LicenseGate.allowed()) {
         if (this.isExecuting && this.mc.thePlayer != null && this.currentExecutingMacro != null) {
            for (List<String> pets = this.currentExecutingMacro.getPets(); this.currentPetIndex < pets.size(); this.currentPetIndex++) {
               this.usePet(pets.get(this.currentPetIndex));
            }

            this.finishExecution();
         }
      }
   }

   private void finishExecution() {
      this.isExecuting = false;
      this.currentExecutingMacro = null;
      this.releaseAllClicks();
   }

   private void releaseAllClicks() {
      try {
         if (this.mc.gameSettings != null) {
            if (this.mc.gameSettings.keyBindAttack != null) {
               Field pressedField = KeyBinding.class.getDeclaredField("pressed");
               pressedField.setAccessible(true);
               pressedField.setBoolean(this.mc.gameSettings.keyBindAttack, false);
               Field pressTimeField = KeyBinding.class.getDeclaredField("pressTime");
               pressTimeField.setAccessible(true);
               pressTimeField.setInt(this.mc.gameSettings.keyBindAttack, 0);
            }

            if (this.mc.gameSettings.keyBindUseItem != null) {
               Field pressedField = KeyBinding.class.getDeclaredField("pressed");
               pressedField.setAccessible(true);
               pressedField.setBoolean(this.mc.gameSettings.keyBindUseItem, false);
               Field pressTimeField = KeyBinding.class.getDeclaredField("pressTime");
               pressTimeField.setAccessible(true);
               pressTimeField.setInt(this.mc.gameSettings.keyBindUseItem, 0);
            }
         }
      } catch (Exception var3) {
      }
   }

   private boolean usePet(String petName) {
      this.releaseAllClicks();
      String target = petName.toLowerCase().trim();

      for (int i = 0; i < 9; i++) {
         ItemStack stack = this.mc.thePlayer.inventory.getStackInSlot(i);
         if (stack != null && stack.hasDisplayName() && stack.getDisplayName().toLowerCase().contains(target)) {
            int saved = this.mc.thePlayer.inventory.currentItem;
            this.mc.thePlayer.inventory.currentItem = i;
            this.mc.playerController.sendUseItem(this.mc.thePlayer, this.mc.theWorld, stack);
            this.mc.thePlayer.inventory.currentItem = saved;
            return true;
         }
      }

      return false;
   }

   public List<PetMacro.CustomPetMacro> getMacros() {
      return this.macros;
   }

   public PetMacro.CustomPetMacro getMacro(int index) {
      return index >= 0 && index < this.macros.size() ? this.macros.get(index) : null;
   }

   public void addMacro(PetMacro.CustomPetMacro macro) {
      this.macros.add(macro);
      this.saveMacros();
   }

   public void removeMacro(int index) {
      if (index >= 0 && index < this.macros.size()) {
         this.macros.remove(index);
         this.saveMacros();
      }
   }

   public void saveMacros() {
      PetMacroConfig.saveMacros(this.macros);
   }

   public static class CustomPetMacro {
      private String name;
      private int keybind;
      private List<String> pets = new ArrayList<>();

      public CustomPetMacro(String name, int keybind) {
         this.name = name;
         this.keybind = keybind;
      }

      public String getName() {
         return this.name;
      }

      public void setName(String n) {
         this.name = n;
      }

      public int getKeybind() {
         return this.keybind;
      }

      public void setKeybind(int k) {
         this.keybind = k;
      }

      public List<String> getPets() {
         return this.pets;
      }

      public void addPet(String petName) {
         this.pets.add(petName);
      }

      public void removePet(int index) {
         if (index >= 0 && index < this.pets.size()) {
            this.pets.remove(index);
         }
      }

      public void clearPets() {
         this.pets.clear();
      }
   }
}
