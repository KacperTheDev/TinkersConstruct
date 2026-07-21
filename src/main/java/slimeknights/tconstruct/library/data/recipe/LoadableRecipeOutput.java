package slimeknights.tconstruct.library.data.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;

/** Datagen bridge for recipes backed by Mantle record loadables. */
public class LoadableRecipeOutput<T extends Recipe<?>> extends AbstractRecipeOutput {
  private final T recipe;
  private final RecordLoadable<T> loader;

  public LoadableRecipeOutput(T recipe, RecordLoadable<T> loader, @Nullable ResourceLocation advancementId) {
    super(recipeId(recipe), advancementId);
    this.recipe = recipe;
    this.loader = loader;
  }

  private static ResourceLocation recipeId(Recipe<?> recipe) {
    try {
      return (ResourceLocation)recipe.getClass().getMethod("getId").invoke(recipe);
    } catch (ReflectiveOperationException | ClassCastException e) {
      throw new IllegalStateException("Recipe does not expose getId(): " + recipe.getClass().getName(), e);
    }
  }

  @Override
  public RecipeSerializer<?> getType() {
    return recipe.getSerializer();
  }

  @Override
  public void serializeRecipeData(JsonObject json) {
    JsonElement element = loader.serialize(recipe);
    if (element.isJsonObject()) {
      json.asMap().putAll(element.getAsJsonObject().asMap());
    }
  }

  /** Emits this legacy builder result through the native 1.21 recipe sink. */
  public void emitTo(RecipeOutput output) {
    output.accept(id, recipe, null);
  }
}
