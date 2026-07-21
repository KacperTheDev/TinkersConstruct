package slimeknights.tconstruct.library.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;

/** Bridges the old item-sensitive cure behavior to NeoForge's native named cures. */
public final class EffectCureUtil {
  private EffectCureUtil() {}

  public static EffectCure forStack(ItemStack stack) {
    if (stack.is(Items.MILK_BUCKET)) {
      return EffectCures.MILK;
    }
    if (stack.is(Items.HONEY_BOTTLE)) {
      return EffectCures.HONEY;
    }
    return EffectCure.get("tconstruct:item/" + BuiltInRegistries.ITEM.getKey(stack.getItem()));
  }
}
