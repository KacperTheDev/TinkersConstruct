package slimeknights.tconstruct.library.recipe.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.recipe.helper.LoggingRecipeSerializer;
import slimeknights.mantle.util.LogicHelper;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.tables.TinkerTables;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shaped recipe with a number of {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient} and
 * {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient} to set the materials of the result.
 */
public class ShapedMaterialsRecipe extends ShapedRecipe implements MaterialsCraftingTableRecipe {
  /** List of tool parts to search for in the final recipe */
  @Getter
  private final List<Ingredient> parts;
  /**
   * If true, a part may show up multiple times in the inputs, and all copies should match.
   * If false, only the first instance of a part is checked for each input, allowing a tool with the same part multiple times.
   */
  private final boolean checkRepeats;
  /** List of additional materials to add beyond the parts */
  @Getter
  private final List<MaterialVariantId> extraMaterials;
  public ShapedMaterialsRecipe(ResourceLocation id, String group, CraftingBookCategory category, int width, int height, NonNullList<Ingredient> ingredients, ItemStack result, boolean showNotification, List<Ingredient> parts, List<MaterialVariantId> extraMaterials) {
    this(id, group, category, new ShapedRecipePattern(width, height, ingredients, Optional.empty()), result, showNotification, parts, extraMaterials);
  }

  private ShapedMaterialsRecipe(ResourceLocation id, String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification, List<Ingredient> parts, List<MaterialVariantId> extraMaterials) {
    super(group, category, pattern, result, showNotification);
    this.parts = parts;
    this.checkRepeats = parts.stream().unordered().distinct().count() == parts.size();
    this.extraMaterials = extraMaterials;
  }

  public ShapedMaterialsRecipe(ShapedRecipe recipe, List<Ingredient> parts, List<MaterialVariantId> extraMaterials) {
    this(null, recipe.getGroup(), recipe.category(), recipe.pattern,
         recipe.getResultItem((HolderLookup.Provider)null), recipe.showNotification(), parts, extraMaterials);
  }

  /** Copies this recipe with a component-updated result while retaining its packed pattern and material contract. */
  public ShapedMaterialsRecipe withResult(ItemStack result) {
    return new ShapedMaterialsRecipe(null, getGroup(), category(), pattern, result, showNotification(), parts, extraMaterials);
  }

  @Override
  public int getPartCount() {
    return parts.size();
  }

  /**
   * Finds materials for each of the parts
   * @return Array of all matched materials. Array will have no null entries, though the array may be null if no match was found.
   */
  @Nullable
  static MaterialVariantId[] findMaterials(CraftingInput inventory, List<Ingredient> parts, int partCount, boolean checkRepeats) {
    // want one material for each
    MaterialVariantId[] materials = new MaterialVariantId[partCount];
    for (int i = 0; i < inventory.size(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        for (int p = 0; p < partCount; p++) {
          MaterialVariantId current = materials[p];
          // if we have not found the material yet, or repeats are considered the same material, test the ingredient
          if ((current == null || checkRepeats) && parts.get(p).test(stack)) {
            MaterialVariantId matched;
            if (stack.getItem() instanceof IMaterialItem materialItem) {
              matched = materialItem.getMaterial(stack);
            } else {
              matched = MaterialRecipeCache.findRecipe(stack).getMaterial().getVariant();
            }
            // first occurrence? thats our material
            if (current == null) {
              materials[p] = matched;
              break;
            } else if (!current.matchesVariant(matched)) {
              // if same material but different variants, just discard the variant
              if (current.getId().equals(matched.getId())) {
                materials[p] = current.getId();
                break;
              } else {
                // if different materials, no match
                return null;
              }
            }
          }
        }
      }
    }
    // ensure we found all materials needed
    for (int p = 0; p < partCount; p++) {
      if (materials[p] == null) {
        return null;
      }
    }
    return materials;
  }

  @Override
  public boolean matches(CraftingInput inventory, Level level) {
    if (!super.matches(inventory, level)) {
      return false;
    }
    // ensure all part materials matched and we found all parts
    return findMaterials(inventory, parts, parts.size(), checkRepeats) != null;
  }

  /** Common logic to this and {@link ShapedMaterialsRecipe} */
  public static void setMaterial(ItemStack stack, MaterialVariantId material, List<MaterialVariantId> extraMaterials) {
    if (extraMaterials.isEmpty() && stack.getItem() instanceof IMaterialItem materialItem) {
      materialItem.setMaterial(stack, material);
    } else {
      MaterialNBT.Builder builder = MaterialNBT.builder();
      builder.add(material);
      for (MaterialVariantId extraMaterial : extraMaterials) {
        builder.add(extraMaterial);
      }
      ToolStack.from(stack).setMaterials(builder.build());
    }
  }

  /** Sets the material for the given stack */
  @Override
  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    setMaterial(stack, material, extraMaterials);
  }

  /** Assembles the item with material information */
  static ItemStack assemble(ItemStack stack, CraftingInput inventory, List<Ingredient> parts, int partCount, boolean checkRepeats, List<MaterialVariantId> extraMaterials) {
    MaterialVariantId[] materials = findMaterials(inventory, parts, partCount, checkRepeats);
    if (materials != null) {
      // if the result is a tool part, and we only have the one material, set its material
      if (materials.length == 1 && extraMaterials.isEmpty() && stack.getItem() instanceof IMaterialItem materialItem) {
        return materialItem.setMaterial(stack, materials[0]);
      }
      MaterialNBT.Builder builder = MaterialNBT.builder();
      // add each material
      for (MaterialVariantId material : materials) {
        builder.add(material);
      }
      // add extra materials
      builder.add(extraMaterials);
      ToolStack.from(stack).setMaterials(builder.build());
    }
    return stack;
  }

  @Override
  public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider registryAccess) {
    return assemble(super.assemble(inventory, registryAccess), inventory, parts, parts.size(), checkRepeats, extraMaterials);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerTables.shapedMaterialsRecipeSerializer.get();
  }

  public static class Serializer implements LoggingRecipeSerializer<ShapedMaterialsRecipe> {
    static final Loadable<List<MaterialVariantId>> EXTRA_MATERIALS = MaterialVariantId.LOADABLE.list(0);
    static final LoadableField<List<MaterialVariantId>, ShapedMaterialsRecipe> MATERIAL_FIELD = EXTRA_MATERIALS.defaultField("extra_materials", List.of(), r -> r.extraMaterials);

    @Override
    public ShapedMaterialsRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
      // Decode the native pattern data directly so "parts" can retain references to the same ingredient instances as the key.
      String group = GsonHelper.getAsString(json, "group", "");
      CraftingBookCategory category = json.has("category")
        ? CraftingBookCategory.CODEC.parse(JsonOps.INSTANCE, json.get("category")).getOrThrow()
        : CraftingBookCategory.MISC;
      ShapedRecipePattern.Data data = ShapedRecipePattern.Data.MAP_CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow();
      ShapedRecipePattern pattern = ShapedRecipePattern.of(data.key(), data.pattern());
      ItemStack result = ItemStack.STRICT_CODEC.parse(JsonOps.INSTANCE, GsonHelper.getAsJsonObject(json, "result")).getOrThrow();
      boolean showNotification = GsonHelper.getAsBoolean(json, "show_notification", true);

      // specific to shaped part recipe, map from a pattern string to the ingredients for each character
      // saves memory by not having separate copies of each, plus simplifies the JSON
      String partPattern = GsonHelper.getAsString(json, "parts");
      List<Ingredient> parts = new ArrayList<>();
      for (int i = 0; i < partPattern.length(); i++) {
        String sym = partPattern.substring(i, i + 1);
        Ingredient ingredient = data.key().get(sym.charAt(0));
        if (ingredient == null) {
          throw new JsonSyntaxException("Parts references symbol '" + sym + "' but it's not defined in the key");
        }
        parts.add(ingredient);
      }
      return new ShapedMaterialsRecipe(recipeId, group, category, pattern, result, showNotification, List.copyOf(parts), MATERIAL_FIELD.get(json));
    }

    @SuppressWarnings("Java8ListReplaceAll")
    @Override
    @Nullable
    public ShapedMaterialsRecipe fromNetworkSafe(ResourceLocation recipeId, RegistryFriendlyByteBuf buffer) {
      // shaped syncing
      int width = buffer.readVarInt();
      int height = buffer.readVarInt();
      String group = buffer.readUtf();
      CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
      // skipping ingredients for now
      ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
      boolean showNotification = buffer.readBoolean();
      // fetch remaining non-ingredient elements
      List<MaterialVariantId> extraMaterials = MATERIAL_FIELD.decode(buffer);

      // start syncing ingredients back over, they are distinct so we will need to rematch them
      int size = buffer.readVarInt();
      List<Ingredient> distinct = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        distinct.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
      }

      // form inputs and parts lists
      NonNullList<Ingredient> inputs = NonNullList.withSize(width * height, Ingredient.EMPTY);
      for (int i = 0; i < inputs.size(); i++) {
        inputs.set(i, LogicHelper.getOrDefault(distinct, buffer.readByte(), Ingredient.EMPTY));
      }
      size = buffer.readVarInt();
      // read in parts
      List<Ingredient> parts = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        parts.add(i, LogicHelper.getOrDefault(distinct, buffer.readByte(), Ingredient.EMPTY));
      }
      return new ShapedMaterialsRecipe(recipeId, group, category, width, height, inputs, result, showNotification, List.copyOf(parts), extraMaterials);
    }

    @Override
    public void toNetworkSafe(RegistryFriendlyByteBuf buffer, ShapedMaterialsRecipe recipe) {
      // standard shaped recipe stuff
      buffer.writeVarInt(recipe.getWidth());
      buffer.writeVarInt(recipe.getHeight());
      buffer.writeUtf(recipe.getGroup());
      buffer.writeEnum(recipe.category());
      // skipping ingredients for now
      ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(buffer.registryAccess()));
      buffer.writeBoolean(recipe.showNotification());
      // sync remaining non-ingredient elements
      MATERIAL_FIELD.encode(buffer, recipe);

      // save memory and ensure instance matching by syncing only unique ingredients (by instance comparison)
      List<Ingredient> inputs = recipe.getIngredients();
      List<Ingredient> distinct = inputs.stream().unordered().distinct().toList();
      buffer.writeVarInt(distinct.size());
      for (Ingredient ingredient : distinct) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
      }
      // to sync inputs, we just sync the index within the distinct list
      for (Ingredient ingredient : inputs) {
        buffer.writeByte(distinct.indexOf(ingredient));
      }
      // parts size is not determine from ingredients size, so sync it directly
      buffer.writeVarInt(recipe.parts.size());
      for (Ingredient ingredient : recipe.parts) {
        buffer.writeByte(distinct.indexOf(ingredient));
      }
    }

    private static final String SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /** Serializes the vanilla shaped fields while retaining the legacy key/pattern JSON layout. */
    static JsonObject serializeShapedBase(ShapedRecipe recipe) {
      JsonObject json = new JsonObject();
      if (!recipe.getGroup().isEmpty()) {
        json.addProperty("group", recipe.getGroup());
      }
      if (recipe.category() != CraftingBookCategory.MISC) {
        json.addProperty("category", recipe.category().getSerializedName());
      }

      IdentityHashMap<Ingredient,Character> symbols = new IdentityHashMap<>();
      JsonObject key = new JsonObject();
      JsonArray pattern = new JsonArray();
      List<Ingredient> ingredients = recipe.getIngredients();
      int nextSymbol = 0;
      for (int y = 0; y < recipe.getHeight(); y++) {
        StringBuilder row = new StringBuilder(recipe.getWidth());
        for (int x = 0; x < recipe.getWidth(); x++) {
          Ingredient ingredient = ingredients.get(y * recipe.getWidth() + x);
          if (ingredient.isEmpty()) {
            row.append(' ');
          } else {
            Character symbol = symbols.get(ingredient);
            if (symbol == null) {
              if (nextSymbol >= SYMBOLS.length()) {
                throw new JsonSyntaxException("Too many distinct ingredients in shaped material recipe");
              }
              symbol = SYMBOLS.charAt(nextSymbol++);
              symbols.put(ingredient, symbol);
              key.add(symbol.toString(), Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, ingredient).getOrThrow());
            }
            row.append(symbol);
          }
        }
        pattern.add(row.toString());
      }
      json.add("key", key);
      json.add("pattern", pattern);
      json.add("result", ItemStack.STRICT_CODEC.encodeStart(JsonOps.INSTANCE, recipe.getResultItem((HolderLookup.Provider)null)).getOrThrow());
      if (!recipe.showNotification()) {
        json.addProperty("show_notification", false);
      }
      return json;
    }

    @Override
    public JsonObject toJson(ShapedMaterialsRecipe recipe) {
      JsonObject json = serializeShapedBase(recipe);

      // Recreate the compact legacy parts string from the same identity-based key assignment.
      List<Ingredient> inputs = recipe.getIngredients();
      StringBuilder partPattern = new StringBuilder(recipe.parts.size());
      for (Ingredient part : recipe.parts) {
        int index = -1;
        for (int i = 0; i < inputs.size(); i++) {
          if (inputs.get(i) == part) {
            index = i;
            break;
          }
        }
        if (index < 0) {
          throw new JsonSyntaxException("Part ingredient is not present in shaped recipe inputs");
        }
        int distinct = 0;
        IdentityHashMap<Ingredient,Character> symbols = new IdentityHashMap<>();
        for (Ingredient input : inputs) {
          if (!input.isEmpty() && !symbols.containsKey(input)) {
            symbols.put(input, SYMBOLS.charAt(distinct++));
          }
        }
        partPattern.append(symbols.get(inputs.get(index)));
      }
      json.addProperty("parts", partPattern.toString());
      MATERIAL_FIELD.serialize(recipe, json);
      return json;
    }
  }
}
