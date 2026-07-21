package slimeknights.tconstruct.library.json.predicate.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.JsonCodec;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags.Items;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/** Item sub-predicate for matching Tinkers' Construct tools using serialized tool data predicates. */
public class ToolStackItemPredicate implements ItemSubPredicate {
  public static final ResourceLocation ID = TConstruct.getResource("tool_stack");
  public static final Codec<ToolStackItemPredicate> CODEC = new JsonCodec<>() {
    @Override
    public ToolStackItemPredicate deserialize(JsonElement element, DynamicOps<?> ops) {
      if (!element.isJsonObject()) {
        throw new JsonSyntaxException("Tinkers' Construct tool stack predicate must be a JSON object");
      }
      return ToolStackItemPredicate.deserialize(element.getAsJsonObject());
    }

    @Override
    public JsonElement serialize(ToolStackItemPredicate predicate, DynamicOps<?> ops) {
      return predicate.serializeToJson();
    }

    @Override
    public String codecError() {
      return "Tinkers' Construct tool stack item predicate";
    }
  };
  public static final Type<ToolStackItemPredicate> TYPE = new Type<>(CODEC);

  private final IJsonPredicate<IToolStackView> predicate;

  public ToolStackItemPredicate(IJsonPredicate<IToolStackView> predicate) {
    this.predicate = predicate;
  }

  public static ToolStackItemPredicate ofTool(IJsonPredicate<IToolStackView> predicate) {
    return new ToolStackItemPredicate(predicate);
  }

  public static ToolStackItemPredicate ofContext(IJsonPredicate<IToolContext> predicate) {
    return new ToolStackItemPredicate(ToolStackPredicate.context(predicate));
  }

  @Override
  public boolean matches(ItemStack stack) {
    // The tag check prevents treating arbitrary stacks as Tinkers' tools.
    return stack.is(Items.MODIFIABLE) && predicate.matches(ToolStack.from(stack));
  }

  public JsonObject serializeToJson() {
    JsonObject json = new JsonObject();
    json.add("predicate", ToolStackPredicate.LOADER.serialize(predicate));
    return json;
  }

  /** Deserializes the tool predicate from the item sub-predicate payload. */
  public static ToolStackItemPredicate deserialize(JsonObject json) {
    return new ToolStackItemPredicate(ToolStackPredicate.LOADER.getIfPresent(json, "predicate"));
  }
}
