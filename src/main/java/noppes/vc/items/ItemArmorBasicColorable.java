package noppes.vc.items;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import noppes.vc.VariedCommodities;

/**
 * 可染色护甲（纸王冠）。
 *
 * 原作自己实现了 {@code getColor}/{@code removeColor} 并把默认色写成 10511680；
 * 1.20.1 的 {@link DyeableLeatherItem} 接口默认实现一模一样（同样的 display/color NBT、
 * 同样的默认色），直接实现接口即可，不必再抄一遍。
 */
public class ItemArmorBasicColorable extends ItemArmorBasic implements DyeableLeatherItem {

   public ItemArmorBasicColorable(ArmorMaterial material, Type type, String texture, Properties props) {
      super(material, type, texture, props);
   }

   @Override
   public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String layer) {
      String suffix = "overlay".equals(layer) ? "_1_overlay.png" : "_1.png";
      return VariedCommodities.MODID + ":textures/models/armor/" + this.texture + suffix;
   }
}
