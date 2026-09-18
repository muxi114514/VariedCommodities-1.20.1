package noppes.vc.client;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.blocks.VCDye;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import noppes.vc.VariedCommodities;
import noppes.vc.init.VCBlocks;
import noppes.vc.client.gui.ScreenCarpentry;
import noppes.vc.client.gui.ScreenStorage;
import noppes.vc.client.model.SkirtModel;
import noppes.vc.client.renderer.BookRenderer;
import noppes.vc.client.renderer.EmblemRenderer;
import noppes.vc.client.renderer.PedestalRenderer;
import noppes.vc.client.renderer.ProjectileRenderer;
import noppes.vc.client.renderer.TextBlockRenderer;
import noppes.vc.client.renderer.WeaponRackRenderer;
import noppes.vc.init.VCBlockEntities;
import noppes.vc.init.VCEntities;
import noppes.vc.init.VCItems;
import noppes.vc.init.VCWeapons;
import noppes.vc.init.VCMenus;

/**
 * 客户端注册。对应 1.12 的 {@code ClientProxy#postinit}。
 *
 * 水晶方块的 16 个颜色共用一张灰度贴图，靠 tintindex + 颜色处理器上色——
 * 模型里的 {@code "tintindex": 0} 必须有这里的处理器接着，否则一律渲染成原始灰。
 */
@Mod.EventBusSubscriber(modid = VariedCommodities.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class VCClient {

   private VCClient() {
   }

   @SubscribeEvent
   public static void clientSetup(FMLClientSetupEvent event) {
      // ItemBlockRenderTypes 内部是普通 HashMap，且这个 setRenderLayer 重载没有 synchronized；
      // FMLClientSetupEvent 在并行加载线程上触发，必须 enqueueWork 回主线程写，
      // 否则与其它模组并发写同一张表可能破坏其内部结构
      event.enqueueWork(() -> {
         VCBlocks.CRYSTAL_BLOCK.values().forEach(block ->
                 ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.translucent()));
         // MenuScreens 的注册表同样不是线程安全的，一并放进主线程
         MenuScreens.register(VCMenus.CARPENTRY.get(), ScreenCarpentry::new);
         MenuScreens.register(VCMenus.STORAGE.get(), ScreenStorage::new);
      });
   }

   /** 裙甲模型：整套裙甲共用一个实例，首次用到时才烘焙。 */
   private static SkirtModel skirt;

   @SubscribeEvent
   public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
      event.registerLayerDefinition(SkirtModel.LAYER, SkirtModel::createLayer);
   }

   /**
    * 供 {@code ItemArmorSkirt} 取模型。只在渲染线程调用，无需同步。
    *
    * <p>传入的原版护腿模型此刻已被 {@code HumanoidArmorLayer} 抄好了玩家的肢体角度，
    * 这里再转抄给裙甲；抄完补一次裙摆摆动——护甲层不会调 {@code setupAnim}。
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public static HumanoidModel<?> skirtModel(LivingEntity entity, HumanoidModel<?> original) {
      if (skirt == null) {
         skirt = new SkirtModel(Minecraft.getInstance().getEntityModels().bakeLayer(SkirtModel.LAYER));
      }
      ((HumanoidModel) original).copyPropertiesTo(skirt);
      skirt.sway(entity.tickCount);
      return skirt;
   }

   /**
    * 落座用的隐形坐骑也必须注册渲染器，否则实体一生成客户端就崩。
    * 它永远不可见，用原版的 NoopRenderer 即可。
    *
    * <p>方块实体渲染器只剩五个——家具本体全是 JSON 方块模型了，
    * 需要 BER 的只有"内容物"：展示品、武器、徽记、刻字、书。
    * 原作 27 个 TESR 里有 22 个画的是静态家具，那正是它在大基地掉帧的根因。
    */
   @SubscribeEvent
   public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(VCEntities.CHAIR_MOUNT.get(), NoopRenderer::new);
      event.registerEntityRenderer(VCEntities.PROJECTILE.get(), ProjectileRenderer::new);
      event.registerEntityRenderer(VCEntities.MAGIC_PROJECTILE.get(), ProjectileRenderer::new);
      event.registerEntityRenderer(VCEntities.HOLY_HAND_GRENADE.get(), ProjectileRenderer::new);
      event.registerBlockEntityRenderer(VCBlockEntities.PEDESTAL.get(), PedestalRenderer::new);
      event.registerBlockEntityRenderer(VCBlockEntities.WEAPON_RACK.get(), WeaponRackRenderer::new);
      event.registerBlockEntityRenderer(VCBlockEntities.BANNER.get(), EmblemRenderer::new);
      event.registerBlockEntityRenderer(VCBlockEntities.BIG_SIGN.get(), TextBlockRenderer::new);
      event.registerBlockEntityRenderer(VCBlockEntities.BOOK.get(), BookRenderer::new);
   }

   /**
    * 原作 {@code BlockRendererBasic.colorTable}，<b>下标即 DyeColor 的 id</b>
    * （1.12 的 {@code EnumDyeColor.byMetadata(dyeDamage).getDyeDamage()} 等价于 15-dyeDamage，
    * 而染料 damage 与羊毛 metadata 互为反序，净效果就是下标 == id）。
    * 沙发坐垫/高脚灯灯罩/横幅旗面共用这张表，色值逐字沿用原作，不换成原版染料色。
    */
   private static final float[][] COLOR_TABLE = {
           {1.0F, 1.0F, 1.0F}, {0.95F, 0.7F, 0.2F}, {0.9F, 0.5F, 0.85F}, {0.6F, 0.7F, 0.95F},
           {0.9F, 0.9F, 0.2F}, {0.5F, 0.8F, 0.1F}, {0.95F, 0.7F, 0.8F}, {0.3F, 0.3F, 0.3F},
           {0.6F, 0.6F, 0.6F}, {0.3F, 0.6F, 0.7F}, {0.7F, 0.4F, 0.9F}, {0.2F, 0.4F, 0.8F},
           {0.5F, 0.4F, 0.3F}, {0.4F, 0.5F, 0.2F}, {0.8F, 0.3F, 0.3F}, {0.1F, 0.1F, 0.1F},
   };

   /** 放置态取染料的地图色——与 1.12 的 {@code state.getMapColor(...).colorValue} 等价。 */
   @SubscribeEvent
   public static void blockColors(RegisterColorHandlersEvent.Block event) {
      VCBlocks.CRYSTAL_BLOCK.forEach((color, block) -> {
         int rgb = color.getMapColor().col;
         event.register((state, level, pos, tint) -> rgb, block.get());
      });

      // 布料件（沙发坐垫 / 灯罩 / 旗面）共用白贴图 + tintindex 0，颜色从 blockstate 读。
      // 颜色进 blockstate 是 P4 的决定，好处正在这里：着色回调不必去查方块实体。
      BlockColor dyed = (state, level, pos, tint) -> tint != 0 ? -1
              : packRgb(COLOR_TABLE[state.getValue(VCDye.COLOR).getId()]);
      registerDyedBlocks(event, dyed, VCBlocks.COUCH_WOOL, VCBlocks.TALL_LAMP,
              VCBlocks.BANNER, VCBlocks.WALL_BANNER);
   }

   @SafeVarargs
   private static void registerDyedBlocks(RegisterColorHandlersEvent.Block event, BlockColor handler,
                                          Map<?, RegistryObject<Block>>... families) {
      for (Map<?, RegistryObject<Block>> family : families) {
         family.values().forEach(block -> event.register(handler, block.get()));
      }
   }

   /**
    * 手持态取羊毛染色表。
    * 原作这两处用的就是两套色值（{@code ClientProxy:63} 用 {@code EntitySheep.getDyeRgb}、
    * {@code :67} 的方块用地图色），色调略有差别；此处忠实保留，不擅自统一。
    */
   @SubscribeEvent
   public static void itemColors(RegisterColorHandlersEvent.Item event) {
      VCBlocks.CRYSTAL_BLOCK.forEach((color, block) -> {
         int rgb = packRgb(Sheep.getColorArray(color));
         event.register((stack, tint) -> rgb, block.get());
      });
      registerDyedItems(event);

      // 物品栏图标：原作 setColor 在 tile 为 null 时走 c = 15 - meta，
      // 与放下去时的初始颜色（DyeColor.byId(15 - 材质序号)）是同一个式子，故手里和地上同色
      registerDyedBlockItems(event, VCBlocks.TALL_LAMP);
      registerDyedBlockItems(event, VCBlocks.BANNER);
      registerDyedBlockItems(event, VCBlocks.WALL_BANNER);
      // 沙发的变体轴是木材而非材质，原作图标同样用 15 - meta 取色
      registerDyedBlockItems(event, VCBlocks.COUCH_WOOL);
   }

   private static void registerDyedBlockItems(RegisterColorHandlersEvent.Item event,
                                              Map<? extends Enum<?>, RegistryObject<Block>> family) {
      family.forEach((variant, block) -> {
         int rgb = packRgb(COLOR_TABLE[15 - variant.ordinal()]);
         event.register((stack, tint) -> tint != 0 ? -1 : rgb, block.get());
      });
   }

   /**
    * 法球/破碎法球/元素法杖：与水晶同源，16 色共用一张灰度贴图靠 tint 着色
    * （原作 {@code ClientProxy:63} 把这四者注册在同一个回调里）。
    */
   private static void registerDyedItems(RegisterColorHandlersEvent.Item event) {
      VCItems.ORB.forEach((color, item) -> registerDyed(event, color, item));
      VCItems.ORB_BROKEN.forEach((color, item) -> registerDyed(event, color, item));
      VCWeapons.ELEMENTAL_STAFF.forEach((color, item) -> registerDyed(event, color, item));
   }

   private static void registerDyed(RegisterColorHandlersEvent.Item event,
                                    net.minecraft.world.item.DyeColor color,
                                    net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.Item> item) {
      int rgb = packRgb(Sheep.getColorArray(color));
      event.register((stack, tint) -> rgb, item.get());
   }

   private static int packRgb(float[] rgb) {
      return (Math.round(rgb[0] * 255.0F) << 16)
              | (Math.round(rgb[1] * 255.0F) << 8)
              | Math.round(rgb[2] * 255.0F);
   }
}
