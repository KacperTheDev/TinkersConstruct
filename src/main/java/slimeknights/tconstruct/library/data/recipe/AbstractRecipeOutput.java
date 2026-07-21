package slimeknights.tconstruct.library.data.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.helper.FinishedRecipe;

import javax.annotation.Nullable;

/** @deprecated compatibility result for the pre-1.21 FinishedRecipe API. */
@Deprecated(forRemoval = true)
public abstract class AbstractRecipeOutput implements FinishedRecipe {
  protected final ResourceLocation id;
  @Nullable
  protected final ResourceLocation advancementId;

  protected AbstractRecipeOutput(ResourceLocation id, @Nullable ResourceLocation advancementId) {
    this.id = id;
    this.advancementId = advancementId;
  }

  public abstract RecipeSerializer<?> getType();

  public abstract void serializeRecipeData(JsonObject json);

  @Override public ResourceLocation getId() { return id; }
  @Override @Nullable public ResourceLocation getAdvancementId() { return advancementId; }
  @Override @Nullable public JsonObject serializeAdvancement() { return null; }
}
