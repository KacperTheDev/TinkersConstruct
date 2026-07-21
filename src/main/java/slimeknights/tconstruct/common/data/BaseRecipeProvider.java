package slimeknights.tconstruct.common.data;

import net.minecraft.data.PackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import slimeknights.mantle.recipe.data.IRecipeHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.utils.ResourceId;

import java.util.concurrent.CompletableFuture;

/**
 * Shared logic for each module's recipe provider
 */
public abstract class BaseRecipeProvider extends RecipeProvider implements IConditionBuilder, IRecipeHelper {
  private HolderLookup.Provider registries;
  public BaseRecipeProvider(PackOutput generator, CompletableFuture<HolderLookup.Provider> lookupProvider) {
    super(generator, lookupProvider);
    TConstruct.sealTinkersClass(this, "BaseRecipeProvider", "BaseRecipeProvider is trivial to recreate and directly extending can lead to addon recipes polluting our namespace.");
  }

  @Override
  protected final void buildRecipes(RecipeOutput output, HolderLookup.Provider registries) {
    this.registries = registries;
    buildTinkersRecipes(output);
  }

  /** Builds recipes through the native 1.21 recipe sink. */
  protected abstract void buildTinkersRecipes(RecipeOutput output);

  /** Registry context supplied by the native 1.21 recipe provider. */
  protected final HolderLookup.Provider registries() {
    return java.util.Objects.requireNonNull(registries, "Recipe provider has not started");
  }

  /** Wraps this provider with the unique name required when multiple recipe providers share one generator. */
  public DataProvider named() {
    BaseRecipeProvider provider = this;
    return new DataProvider() {
      @Override
      public CompletableFuture<?> run(CachedOutput output) {
        return provider.run(output);
      }

      @Override
      public String getName() {
        return "Tinkers' Construct " + provider.getClass().getSimpleName();
      }
    };
  }

  @Override
  public String getModId() {
    return TConstruct.MOD_ID;
  }

  /** Extends a typed Tinkers ID while preserving the recipe provider namespace. */
  protected ResourceLocation wrap(ResourceId id, String prefix, String suffix) {
    return wrap(id.location(), prefix, suffix);
  }

  /** Prefixes a typed Tinkers ID. */
  protected ResourceLocation prefix(ResourceId id, String prefix) {
    return prefix(id.location(), prefix);
  }

  /** Suffixes a typed Tinkers ID. */
  protected ResourceLocation suffix(ResourceId id, String suffix) {
    return suffix(id.location(), suffix);
  }
}
