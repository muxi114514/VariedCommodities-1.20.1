package noppes.vc.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

/**
 * VC 盾牌：耐久 = 材质耐久 × 5（原作 {@code setMaxDamage(material.getMaxUses() * 5)}）。
 *
 * 格挡行为完全由原版 {@link ShieldItem} 提供——原作那个
 * {@code isShield()} 返回 true 是 1.12 Forge 的钩子，1.20.1 里继承 ShieldItem 即已具备。
 */
public class ItemShieldBasic extends ShieldItem {

   /** 原作的耐久倍率。 */
   public static final int DURABILITY_FACTOR = 5;

   public ItemShieldBasic(Properties props) {
      super(props);
   }

   /** 原版盾牌用木板修，VC 盾牌的材质各异且原作未设修复材料，故一律不可修。 */
   @Override
   public boolean isValidRepairItem(ItemStack shield, ItemStack material) {
      return false;
   }
}
