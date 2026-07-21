package slimeknights.tconstruct.library.tools.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.registration.object.IdAwareObject;

import java.util.List;
import java.util.Map;

/** Armor material that returns 0 except for name, since we bypass all the usages */
public class DummyArmorMaterial implements IdAwareObject {
  private final ResourceLocation id;
  private final SoundEvent equipSound;
  private Holder<ArmorMaterial> holder;

  public DummyArmorMaterial(ResourceLocation id, SoundEvent equipSound) {
    this.id = id;
    this.equipSound = equipSound;
  }

  @Override
  public ResourceLocation getId() {
    return id;
  }

  /** Gets the vanilla armor material holder required by 1.21 armor items. */
  public Holder<ArmorMaterial> asArmorMaterial() {
    if (holder == null) {
      holder = Holder.direct(new ArmorMaterial(
        Map.of(),
        0,
        Holder.direct(equipSound),
        () -> Ingredient.EMPTY,
        List.of(new ArmorMaterial.Layer(id)),
        0,
        0
      ));
    }
    return holder;
  }
}
