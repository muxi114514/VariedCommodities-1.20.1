package noppes.vc;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import noppes.vc.config.VCConfig;
import noppes.vc.recipes.RecipeEnabledCondition;
import noppes.vc.init.*;
import noppes.vc.network.VCNetwork;
import org.slf4j.Logger;

/**
 * Varied Commodities —— Noppes 原作（CC BY-NC 3.0）的 1.20.1 忠实移植。
 *
 * 1.12 的 @Mod 生命周期（preInit/init/postInit + SidedProxy）在 1.20.1 已被
 * 「构造期挂注册器 + 事件总线」取代：所有 DeferredRegister 必须在 mod 构造函数里
 * 挂到 mod 事件总线，注册时机才由 Forge 统一调度。
 */
@Mod(VariedCommodities.MODID)
public class VariedCommodities {
   public static final String MODID = "variedcommodities";
   public static final Logger LOG = LogUtils.getLogger();

   public VariedCommodities() {
      IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

      // 各层注册器（内容由 P1~P7 逐层填充）
      VCSounds.register(modBus);
      VCBlocks.register(modBus);
      VCItems.register(modBus);
      VCBlockEntities.register(modBus);
      VCEntities.register(modBus);
      VCMenus.register(modBus);
      VCEnchants.register(modBus);
      VCRecipes.register(modBus);
      VCCreativeTabs.register(modBus);

      VCNetwork.register();

      modBus.addListener(VariedCommodities::commonSetup);

      ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, VCConfig.SPEC);
      MinecraftForge.EVENT_BUS.register(this);
   }

   /**
    * 注册配方条件序列化器——每份 VC 配方的 {@code conditions} 里那个开关靠它认出来。
    *
    * <p>⚠️ 必须 {@code enqueueWork} 回主线程：1.20.1 的 mod 构造与 setup 都在并行线程上跑，
    * 而 {@code CraftingHelper} 存序列化器用的是普通 HashMap，并发写会坏表。
    * 放在 setup 也够早——数据包要等进世界才加载。
    */
   private static void commonSetup(FMLCommonSetupEvent event) {
      event.enqueueWork(() -> CraftingHelper.register(RecipeEnabledCondition.SERIALIZER));
   }
}
