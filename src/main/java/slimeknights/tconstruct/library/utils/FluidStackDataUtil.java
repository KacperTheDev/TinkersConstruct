package slimeknights.tconstruct.library.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.Holder;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/** Component-based helpers for fluid stacks that carried custom NBT before 1.21. */
public final class FluidStackDataUtil {
  private FluidStackDataUtil() {}

  public static FluidStack create(Fluid fluid, int amount, CompoundTag data) {
    FluidStack stack = new FluidStack(fluid, amount);
    if (data != null && !data.isEmpty()) {
      CompoundTag remaining = data.copy();
      String potionId = remaining.getString("Potion");
      if (!potionId.isEmpty()) {
        ResourceLocation id = ResourceLocation.tryParse(potionId);
        if (id != null) {
          BuiltInRegistries.POTION.getHolder(id).ifPresent(
            potion -> stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion)));
        }
        remaining.remove("Potion");
      }
      if (!remaining.isEmpty()) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(remaining));
      }
    }
    return stack;
  }

  /** Creates a stack with an exact copy of the supplied native data component patch. */
  public static FluidStack create(Fluid fluid, int amount, DataComponentPatch components) {
    return new FluidStack(BuiltInRegistries.FLUID.wrapAsHolder(fluid), amount, components);
  }

  /** Creates a fluid stack carrying the native potion component. */
  public static FluidStack createPotion(Fluid fluid, int amount, Holder<Potion> potion) {
    FluidStack stack = new FluidStack(fluid, amount);
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
    return stack;
  }
}
