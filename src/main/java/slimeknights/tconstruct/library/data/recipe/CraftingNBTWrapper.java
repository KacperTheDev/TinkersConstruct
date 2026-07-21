package slimeknights.tconstruct.library.data.recipe;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe;
import slimeknights.tconstruct.library.recipe.material.ShapedMaterialsRecipe;

import javax.annotation.Nullable;

/** Native 1.21 recipe output decorator for legacy crafting-result NBT. */
public final class CraftingNBTWrapper {
  private CraftingNBTWrapper() {}

  public static RecipeOutput wrap(RecipeOutput output, CompoundTag nbt, HolderLookup.Provider registries) {
    CompoundTag data = nbt.copy();
    return new RecipeOutput() {
      @Override
      public Advancement.Builder advancement() {
        return output.advancement();
      }

      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        ItemStack result = recipe.getResultItem(registries).copy();
        applyLegacyData(result, data, registries);
        Recipe<?> wrapped;
        if (recipe instanceof ShapedRetexturedRecipe retextured) {
          wrapped = retextured.withResult(result);
        } else if (recipe instanceof ShapedMaterialsRecipe materials) {
          wrapped = materials.withResult(result);
        } else if (recipe instanceof ShapedRecipe shaped) {
          wrapped = new ShapedRecipe(shaped.getGroup(), shaped.category(), shaped.pattern, result, shaped.showNotification());
        } else if (recipe instanceof ShapelessRecipe shapeless) {
          wrapped = new ShapelessRecipe(shapeless.getGroup(), shapeless.category(), result, shapeless.getIngredients());
        } else {
          throw new IllegalArgumentException("Crafting result NBT requires a shaped or shapeless recipe, got " + recipe.getClass().getName());
        }
        output.accept(id, wrapped, advancement, conditions);
      }
    };
  }

  private static void applyLegacyData(ItemStack stack, CompoundTag source, HolderLookup.Provider registries) {
    CompoundTag remaining = source.copy();
    if (remaining.contains("display", CompoundTag.TAG_COMPOUND)) {
      CompoundTag display = remaining.getCompound("display");
      if (display.contains("Name", CompoundTag.TAG_STRING)) {
        Component name = Component.Serializer.fromJson(display.getString("Name"), registries);
        if (name != null) {
          stack.set(DataComponents.CUSTOM_NAME, name);
        }
        display.remove("Name");
      }
      if (display.isEmpty()) {
        remaining.remove("display");
      }
    }
    if (!remaining.isEmpty()) {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(remaining));
    }
  }
}
