package slimeknights.tconstruct.library.utils;

import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.TConstruct;

import java.util.UUID;

/** Preserves the identity of pre-1.21 UUID attribute modifiers in the ResourceLocation-based API. */
public final class AttributeIdUtil {
  private AttributeIdUtil() {}

  public static ResourceLocation fromLegacyUuid(UUID uuid) {
    return TConstruct.getResource("legacy_attribute/" + uuid);
  }
}
