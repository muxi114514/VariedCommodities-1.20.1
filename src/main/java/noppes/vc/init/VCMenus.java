package noppes.vc.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.containers.CarpentryMenu;
import noppes.vc.containers.StorageMenu;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import noppes.vc.VariedCommodities;

/**
 * 容器菜单注册（P4）。1.12 的 IGuiHandler + GuiType 枚举在 1.20.1 由
 * MenuType + IForgeMenuType.create 取代，开界面走 NetworkHooks.openScreen。
 * 原版三个界面：CRATE / TRADING_BLOCK / CARPENTRY_BENCH。
 */
public final class VCMenus {
   public static final DeferredRegister<MenuType<?>> MENUS =
           DeferredRegister.create(ForgeRegistries.MENU_TYPES, VariedCommodities.MODID);

   /**
    * 木工台。54 格的木箱/木桶不在此列——它们直接用原版的 MenuType.GENERIC_9x6，
    * 因为原作的界面本来就是原版双箱贴图的抄写（见 TileCrate 的说明）。
    */
   public static final RegistryObject<MenuType<CarpentryMenu>> CARPENTRY =
           MENUS.register("carpentry", () -> IForgeMenuType.create(CarpentryMenu::new));

   /** 升级板条箱（81/108/135 格）：原版 MenuType 最大只有 9×6，只能自建。 */
   public static final RegistryObject<MenuType<StorageMenu>> STORAGE =
           MENUS.register("storage", () -> IForgeMenuType.create(StorageMenu::fromNetwork));

   private VCMenus() {
   }

   public static void register(IEventBus bus) {
      MENUS.register(bus);
   }
}
