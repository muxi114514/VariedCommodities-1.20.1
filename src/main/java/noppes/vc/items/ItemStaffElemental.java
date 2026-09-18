package noppes.vc.items;

import java.util.function.Supplier;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

/**
 * 元素法杖（原作 {@code ItemStaffElemental}）：16 个颜色变体，射出同色的法球。
 *
 * <p>1.12 是一个物品带 16 个 metadata，靠 {@code getItemDamage()} 取色；
 * 1.20.1 变体已拆成 16 个独立注册项，颜色在构造时就定死。
 * 蓄力粒子只有一种颜色（第二个颜色传 -1 表示不要），与原作只调一次 spawnParticle 一致。
 */
public class ItemStaffElemental extends ItemStaff {

   public ItemStaffElemental(VCTier tier, DyeColor color, Supplier<Item> orb, Properties props) {
      super(tier, orb, color.getId(), -1, props);
   }
}
