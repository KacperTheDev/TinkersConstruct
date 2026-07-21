package slimeknights.tconstruct.test;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class BaseMcTest {

  /** Creates the registry-aware buffer required by 1.21 payload and component codecs. */
  protected static RegistryFriendlyByteBuf createRegistryBuffer() {
    RegistryAccess access = new RegistryAccess.ImmutableRegistryAccess(BuiltInRegistries.REGISTRY.stream().toList());
    return new RegistryFriendlyByteBuf(Unpooled.buffer(), access);
  }

  /** No need to set it up multiple times */
  private static boolean setupTiers = false;

  /** Sets up the forge tier sorting registry */
  public static void setupTierSorting() {
    if (setupTiers) {
      return;
    }
    setupTiers = true;
    // Mining tiers are tag-based in 1.21.1 and no longer require a global recalculation.
  }
}
