package slimeknights.tconstruct.library.recipe.helper;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.recipe.helper.SimpleRecipeSerializer;

/** Emits fieldless loadable recipes through the native 1.21 recipe sink. */
public final class SimpleRecipeOutput {
  private SimpleRecipeOutput() {}

  public static void save(RecipeOutput output, ResourceLocation id, RecipeSerializer<?> serializer) {
    if (serializer instanceof SimpleRecipeSerializer<?> simple) {
      output.accept(id, simple.fromJson(id, new JsonObject()), null);
    } else if (serializer instanceof LoadableRecipeSerializer<?> loadable) {
      output.accept(id, loadable.fromJson(id, new JsonObject()), null);
    } else {
      throw new IllegalArgumentException("Simple recipe serializer must be fieldless: " + serializer);
    }
  }
}
