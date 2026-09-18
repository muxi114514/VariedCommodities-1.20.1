package noppes.vc.enchants;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import noppes.vc.items.ItemGun;
import noppes.vc.items.ItemStaff;

/**
 * VC 的四个附魔（原作 {@code enchants/} 下的 5 个 java，其中 VCEnchant 是基类）。
 *
 * <p>四者只差稀有度、附魔台消耗曲线、最高等级三项，故合成一个可参数化的类，不再拆四个子类。
 *
 * <p>原作用 {@code EnumHelper} 造了个自定义 {@code EnumEnchantmentType}，再靠覆写
 * {@code canApply} 逐个比对物品类。1.20.1 的 Forge 提供了
 * {@link EnchantmentCategory#create(String, java.util.function.Predicate)}，
 * 一句话就表达了同一件事，谓词也正是原作那份类名单。
 *
 * <p>⚠️ <b>注册名用短名</b>（damage/poison/confusion/infinite），不是原作的 {@code vc_damage}：
 * 1.20.1 的显示名键由注册名推出（{@code enchantment.variedcommodities.<名>}），
 * 而 P1 落地的 16 份 lang 里写的就是短名那一套。
 *
 * <p>⚠️ <b>默认整个不注册</b>，见 {@code VCEnchants#register}。
 * 取等级的助手故意放在 {@code VCEnchants} 而不是这里——关掉时本类连加载都不该发生，
 * 否则那句 {@code EnchantmentCategory.create} 会在一个不确定的时机执行。
 */
public class VCEnchant extends Enchantment {

   /** 只能附在 VC 的枪与法杖上——原作 {@code canApply} 比的就是这两个类。 */
   public static final EnchantmentCategory CATEGORY =
           EnchantmentCategory.create("variedcommodities:gun_or_staff", VCEnchant::applies);

   private final int maxLevel;
   private final int minCost;
   private final int costStep;
   private final int costRange;

   public VCEnchant(Rarity rarity, int maxLevel, int minCost, int costStep, int costRange) {
      super(rarity, CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
      this.maxLevel = maxLevel;
      this.minCost = minCost;
      this.costStep = costStep;
      this.costRange = costRange;
   }

   @Override
   public int getMinCost(int level) {
      return this.minCost + (level - 1) * this.costStep;
   }

   @Override
   public int getMaxCost(int level) {
      return getMinCost(level) + this.costRange;
   }

   @Override
   public int getMaxLevel() {
      return this.maxLevel;
   }


   private static boolean applies(Item item) {
      return item instanceof ItemGun || item instanceof ItemStaff;
   }
}
