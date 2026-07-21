package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/** Ingredient matching an item with no crafting remainder. */
public final class NoContainerIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("no_container");
  public static final MapCodec<NoContainerIngredient> CODEC = Ingredient.CODEC.fieldOf("match")
    .xmap(NoContainerIngredient::new, ingredient -> ingredient.nested);
  public static final IngredientType<NoContainerIngredient> TYPE = new IngredientType<>(CODEC);

  private final Ingredient nested;

  private NoContainerIngredient(Ingredient nested) {
    this.nested = nested;
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && nested.test(stack) && !stack.hasCraftingRemainingItem();
  }

  @Override
  public Stream<ItemStack> getItems() {
    return Arrays.stream(nested.getItems());
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean equals(Object object) {
    return this == object || object instanceof NoContainerIngredient other && nested.equals(other.nested);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nested);
  }

  public static Ingredient of(Ingredient ingredient) {
    return new NoContainerIngredient(ingredient).toVanilla();
  }

  public static Ingredient of(ItemLike... items) {
    return of(Ingredient.of(items));
  }

  public static Ingredient of(ItemStack... stacks) {
    return of(Ingredient.of(stacks));
  }

  public static Ingredient of(TagKey<Item> tag) {
    return of(Ingredient.of(tag));
  }
}
