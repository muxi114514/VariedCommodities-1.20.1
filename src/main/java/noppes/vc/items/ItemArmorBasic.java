package noppes.vc.items;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import noppes.vc.VariedCommodities;

/**
 * VC 护甲。贴图路径沿用原作：腿部用 {@code _2.png}，其余部位用 {@code _1.png}。
 */
public class ItemArmorBasic extends ArmorItem {

   protected final String texture;
   private final String namespace;

   public ItemArmorBasic(ArmorMaterial material, Type type, String texture, Properties props) {
      this(material, type, VariedCommodities.MODID, texture, props);
   }

   public ItemArmorBasic(ArmorMaterial material, Type type, String namespace, String texture, Properties props) {
      super(material, type, props);
      this.namespace = namespace;
      this.texture = texture;
   }

   @Override
   public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String layer) {
      String suffix = slot == EquipmentSlot.LEGS ? "_2.png" : "_1.png";
      return this.namespace + ":textures/models/armor/" + this.texture + suffix;
   }
}
