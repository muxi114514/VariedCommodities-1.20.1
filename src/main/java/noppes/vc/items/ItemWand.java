package noppes.vc.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 魔杖（原作 {@code ItemWand}）。
 *
 * <p>整个类在原作里<b>只有一件事</b>：{@code hasEffect} 返回 true，让它永远带附魔光效。
 * 没有右键行为、没有攻击加成——它是个纯装饰品。1.20.1 对应的是 {@link #isFoil}。
 */
public class ItemWand extends Item {

   public ItemWand(Properties props) {
      super(props);
   }

   @Override
   public boolean isFoil(ItemStack stack) {
      return true;
   }
}
