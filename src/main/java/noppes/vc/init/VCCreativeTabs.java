package noppes.vc.init;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.VariedCommodities;
import noppes.vc.blocks.VCWood;

/**
 * 创造标签页。
 *
 * 1.12 的四个 CreativeTabs 在 1.20.1 变成注册表项，页内容由 displayItems 回调填充
 * 而不再是"物品自己声明属于哪一页"，因此顺序完全由我们控制——这里沿用各注册文件里的
 * 字段声明顺序，即 1.12 的注册顺序，保证老玩家看到的排列与原版一致。
 *
 * 四个图标逐一对应原作：`new ItemStack(couch_wool, 1, 1)`（**meta1 是云杉木沙发**）、
 * coin_gold、shuriken、cow_leather_head。
 */
public final class VCCreativeTabs {

   public static final DeferredRegister<CreativeModeTab> TABS =
           DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VariedCommodities.MODID);

   public static final RegistryObject<CreativeModeTab> BLOCKS = tab("blocks",
           () -> VCBlocks.COUCH_WOOL.get(VCWood.SPRUCE).get(), VCBlocks::creativeOrder);

   public static final RegistryObject<CreativeModeTab> ITEMS = tab("items",
           () -> VCItems.COIN_GOLD.get(), VCItems::creativeOrder);

   public static final RegistryObject<CreativeModeTab> WEAPONS = tab("weapons",
           () -> VCWeapons.SHURIKEN.get(), VCWeapons::creativeOrder);

   public static final RegistryObject<CreativeModeTab> ARMORS = tab("armors",
           () -> VCArmors.COW_LEATHER_HEAD.get(), VCArmors::creativeOrder);

   private VCCreativeTabs() {
   }

   public static void register(IEventBus bus) {
      TABS.register(bus);
   }

   private static <T extends ItemLike> RegistryObject<CreativeModeTab> tab(
           String name, Supplier<T> icon, Supplier<List<? extends RegistryObject<? extends ItemLike>>> contents) {
      return TABS.register(name, () -> CreativeModeTab.builder()
              .title(Component.translatable("itemGroup." + VariedCommodities.MODID + "." + name))
              .icon(() -> new ItemStack(icon.get()))
              .displayItems((params, output) ->
                      contents.get().forEach(entry -> output.accept(new ItemStack(entry.get()))))
              .build());
   }
}
