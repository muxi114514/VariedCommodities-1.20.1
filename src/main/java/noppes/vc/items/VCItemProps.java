package noppes.vc.items;

import net.minecraft.world.item.Item;

/**
 * 物品属性工厂。
 *
 * 1.12 靠 {@code setMaxStackSize}/{@code setMaxDamage}/{@code setFull3D} 逐个方法链设置，
 * 1.20.1 改成不可变的 Properties；集中在这里，免得 339 个注册项各写各的。
 *
 * <p>{@code setFull3D()} 在 1.20.1 没有对应项——那是 1.12 用来让物品在手上以 3D 姿态
 * 而非平面贴图渲染的开关，现在由物品模型的 {@code display} 段决定，属 P5。
 */
public final class VCItemProps {

   private VCItemProps() {
   }

   /** 普通物品，可堆叠 64。 */
   public static Item.Properties basic() {
      return new Item.Properties();
   }

   /** 不可堆叠但无耐久（乐器、法杖等）。 */
   public static Item.Properties single() {
      return new Item.Properties().stacksTo(1);
   }

   /** 带耐久的单件物品。 */
   public static Item.Properties durable(int durability) {
      return new Item.Properties().durability(durability);
   }

   /** 武器/工具：耐久取自材质档。 */
   public static Item.Properties tool(VCTier tier) {
      return durable(tier.getUses());
   }

   /** 盾牌：材质耐久 ×5（原作倍率）。 */
   public static Item.Properties shield(VCTier tier) {
      return durable(tier.getUses() * ItemShieldBasic.DURABILITY_FACTOR);
   }
}
