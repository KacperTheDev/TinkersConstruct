package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.recipe.helper.LoadableIngredientSerializer;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicateField;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/** Ingredient that matches material items and optionally filters their material. */
public final class MaterialIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("material");
  private static final MaterialPredicateField<MaterialIngredient> MATERIAL_FIELD = new MaterialPredicateField<>("material", ingredient -> ingredient.material);
  public static final IngredientType<MaterialIngredient> TYPE = new IngredientType<>(
    LoadableIngredientSerializer.mapCodec(MaterialIngredient::parse, MaterialIngredient::serialize));

  private final Ingredient nested;
  private final IJsonPredicate<MaterialVariantId> material;
  @Nullable
  private ItemStack[] materialStacks;

  private MaterialIngredient(Ingredient nested, IJsonPredicate<MaterialVariantId> material) {
    this.nested = nested;
    this.material = material;
  }

  private static MaterialIngredient parse(JsonObject json) {
    Ingredient nested = JsonHelper.parse(Ingredient.CODEC, json.get("match"));
    IJsonPredicate<MaterialVariantId> material = MATERIAL_FIELD.get(json);
    if (json.has("tag")) {
      TConstruct.LOG.warn("Using deprecated tag field on material ingredient");
      IJsonPredicate<MaterialVariantId> tagPredicate = MaterialPredicate.tag(TinkerLoadables.MATERIAL_TAGS.getIfPresent(json, "tag"));
      material = material == MaterialPredicate.ANY ? tagPredicate : MaterialPredicate.and(material, tagPredicate);
    }
    return new MaterialIngredient(nested, material);
  }

  private static JsonObject serialize(MaterialIngredient ingredient) {
    JsonObject json = new JsonObject();
    json.add("match", JsonHelper.serialize(Ingredient.CODEC, ingredient.nested));
    MATERIAL_FIELD.serialize(ingredient, json);
    return json;
  }

  private static IJsonPredicate<MaterialVariantId> makePredicate(MaterialVariantId material, @Nullable TagKey<IMaterial> tag) {
    IJsonPredicate<MaterialVariantId> predicate = material.equals(IMaterial.UNKNOWN.getIdentifier()) ? MaterialPredicate.ANY : MaterialPredicate.variant(material);
    if (tag != null) {
      IJsonPredicate<MaterialVariantId> tagPredicate = MaterialPredicate.tag(tag);
      predicate = predicate == MaterialPredicate.ANY ? tagPredicate : MaterialPredicate.and(predicate, tagPredicate);
    }
    return predicate;
  }

  private static Ingredient wrap(Ingredient nested, IJsonPredicate<MaterialVariantId> material) {
    return new MaterialIngredient(nested, material).toVanilla();
  }

  public static Ingredient of(Ingredient ingredient, IJsonPredicate<MaterialVariantId> material) { return wrap(ingredient, material); }
  public static Ingredient of(ItemLike item, IJsonPredicate<MaterialVariantId> material) { return wrap(Ingredient.of(item), material); }
  public static Ingredient of(Ingredient ingredient) { return wrap(ingredient, MaterialPredicate.ANY); }
  public static Ingredient of(Ingredient ingredient, MaterialVariantId material) { return wrap(ingredient, MaterialPredicate.variant(material)); }
  public static Ingredient of(Ingredient ingredient, TagKey<IMaterial> tag) { return wrap(ingredient, MaterialPredicate.tag(tag)); }
  public static Ingredient of(ItemLike item, MaterialVariantId material) { return wrap(Ingredient.of(item), MaterialPredicate.variant(material)); }
  public static Ingredient of(ItemLike item, TagKey<IMaterial> tag) { return wrap(Ingredient.of(item), MaterialPredicate.tag(tag)); }
  public static Ingredient of(ItemLike item) { return wrap(Ingredient.of(item), MaterialPredicate.ANY); }
  public static Ingredient of(TagKey<Item> tag, MaterialVariantId material) { return wrap(Ingredient.of(tag), MaterialPredicate.variant(material)); }
  public static Ingredient of(TagKey<Item> tag) { return wrap(Ingredient.of(tag), MaterialPredicate.ANY); }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && !stack.isEmpty() && nested.test(stack)
      && (material == MaterialPredicate.ANY || material.matches(IMaterialItem.getMaterialFromStack(stack)));
  }

  @Override
  public Stream<ItemStack> getItems() {
    if (materialStacks == null) {
      if (!MaterialRegistry.isFullyLoaded()) {
        return Arrays.stream(nested.getItems());
      }
      materialStacks = Arrays.stream(nested.getItems())
        .flatMap(stack -> MaterialRecipeCache.getAllVariants().stream()
          .filter(material::matches)
          .map(mat -> IMaterialItem.withMaterial(stack, mat)))
        .distinct().toArray(ItemStack[]::new);
    }
    return Arrays.stream(materialStacks);
  }

  @Override
  public boolean isSimple() {
    return material == MaterialPredicate.ANY && nested.isSimple();
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean equals(Object object) {
    return this == object || object instanceof MaterialIngredient other
      && nested.equals(other.nested) && material.equals(other.material);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nested, material);
  }
}
