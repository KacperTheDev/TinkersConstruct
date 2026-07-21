package slimeknights.tconstruct.tools.recipe;

import slimeknights.tconstruct.library.data.recipe.LoadableRecipeOutput;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.tconstruct.library.recipe.worktable.AbstractSizedIngredientRecipeBuilder;
import slimeknights.tconstruct.library.recipe.worktable.AbstractWorktableRecipe;

import java.util.function.Consumer;

/** Builder for {@link ToggleInteractionWorktableRecipe} */
@Accessors(fluent = true)
@Setter
@NoArgsConstructor(staticName = "builder")
public class ToggleInteractionWorktableRecipeBuilder extends AbstractSizedIngredientRecipeBuilder<ToggleInteractionWorktableRecipeBuilder> {
  private Ingredient tools = AbstractWorktableRecipe.DEFAULT_TOOLS;

  @Override
  public void save(RecipeOutput consumer) {
    save(consumer, Loadables.ITEM.getKey(tools.getItems()[0].getItem()));
  }

  @Override
  public void save(RecipeOutput consumer, ResourceLocation id) {
    if (inputs.isEmpty()) {
      throw new IllegalStateException("Must have at least one ingredient");
    }
    ResourceLocation advancementId = buildOptionalAdvancement(id, "modifiers");
    saveRecipe(consumer, id, new ToggleInteractionWorktableRecipe(id, tools, inputs), advancementId);
  }
}
