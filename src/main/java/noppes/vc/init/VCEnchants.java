package noppes.vc.init;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.VariedCommodities;
import noppes.vc.config.VCEarlyConfig;
import noppes.vc.enchants.VCEnchant;

/**
 * 附魔注册（对应 1.12 的 4 个自定义附魔，只能附在 VC 的枪与法杖上）。
 *
 * <p>稀有度与附魔台消耗曲线逐字取自原作的四个 {@code Enchant*} 类。
 */
public final class VCEnchants {

   public static final DeferredRegister<Enchantment> ENCHANTMENTS =
           DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, VariedCommodities.MODID);

   /** 伤害 +50%/级，最高 5 级。 */
   public static final RegistryObject<Enchantment> DAMAGE = ENCHANTMENTS.register("damage",
           () -> new VCEnchant(Enchantment.Rarity.COMMON, 5, 1, 10, 15));

   /** 命中生物有 level/4 概率上中毒 5 秒。 */
   public static final RegistryObject<Enchantment> POISON = ENCHANTMENTS.register("poison",
           () -> new VCEnchant(Enchantment.Rarity.UNCOMMON, 2, 12, 20, 25));

   /** 命中生物有 level/4 概率上反胃 5 秒。 */
   public static final RegistryObject<Enchantment> CONFUSION = ENCHANTMENTS.register("confusion",
           () -> new VCEnchant(Enchantment.Rarity.RARE, 2, 12, 20, 25));

   /** 不消耗子弹/魔力。原作的消耗曲线是固定 20~50，故 step 取 0、range 取 30。 */
   public static final RegistryObject<Enchantment> INFINITE = ENCHANTMENTS.register("infinite",
           () -> new VCEnchant(Enchantment.Rarity.VERY_RARE, 1, 20, 0, 30));

   private VCEnchants() {
   }

   /**
    * ⚠️ <b>默认不注册</b>（{@code general.registerEnchantments = false}）。
    * 这四个附魔只对 VC 的枪与法杖生效，默认开着等于白白稀释所有人的附魔台。
    *
    * <p>注册事件早于 Forge 加载配置，所以这一项读的是 {@link VCEarlyConfig}
    * ——构造期直接读同一个 toml 的同一个键，见那个类的说明。
    */
   public static void register(IEventBus bus) {
      if (VCEarlyConfig.registerEnchantments()) {
         ENCHANTMENTS.register(bus);
      } else {
         VariedCommodities.LOG.info("附魔未注册（config/variedcommodities-common.toml 的 "
                 + "general.registerEnchantments = false）");
      }
   }

   // ⚠️ 下面四个一律先问 isPresent()：没注册时 RegistryObject 永远解析不了，
   // 直接 get() 会抛。枪与法杖每次开火都会调它们，不能让"关掉附魔"变成崩服。

   public static int damage(ItemStack stack) {
      return levelOf(DAMAGE, stack);
   }

   public static int poison(ItemStack stack) {
      return levelOf(POISON, stack);
   }

   public static int confusion(ItemStack stack) {
      return levelOf(CONFUSION, stack);
   }

   public static boolean infinite(ItemStack stack) {
      return levelOf(INFINITE, stack) > 0;
   }

   private static int levelOf(RegistryObject<Enchantment> enchant, ItemStack stack) {
      return enchant.isPresent()
              ? EnchantmentHelper.getItemEnchantmentLevel(enchant.get(), stack) : 0;
   }
}
