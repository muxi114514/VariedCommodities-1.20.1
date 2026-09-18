package noppes.vc.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.VariedCommodities;
import noppes.vc.blocks.BlockBanner;
import noppes.vc.blocks.BlockBarrel;
import noppes.vc.blocks.BlockBeam;
import noppes.vc.blocks.BlockBasicLightable;
import noppes.vc.blocks.BlockBlood;
import noppes.vc.blocks.BlockBook;
import noppes.vc.blocks.BlockCampfire;
import noppes.vc.blocks.BlockCarpentryBench;
import noppes.vc.blocks.BlockCandle;
import noppes.vc.blocks.BlockCouch;
import noppes.vc.blocks.BlockCrate;
import noppes.vc.blocks.BlockCouchWool;
import noppes.vc.blocks.BlockPedestal;
import noppes.vc.blocks.BlockSeat;
import noppes.vc.blocks.BlockShelf;
import noppes.vc.blocks.BlockSign;
import noppes.vc.blocks.BlockTable;
import noppes.vc.blocks.BlockTallLamp;
import noppes.vc.blocks.BlockTextEditable;
import noppes.vc.blocks.BlockWallBanner;
import noppes.vc.blocks.BlockWeaponRack;
import noppes.vc.blocks.VCBlockProps;
import noppes.vc.blocks.VCMaterial;
import noppes.vc.blocks.VCWood;
import noppes.vc.blocks.storage.StorageTier;

/**
 * 方块注册。1.12 的 29 个注册名展开成 135 个（见 reference/REGISTRY_EXPANDED.csv）。
 *
 * <p><b>字段声明顺序 = 1.12 VCBlocks#registerBlocks 的注册顺序。</b>
 * 创造栏排列取自 {@link #CREATIVE_ORDER}，而它按字段初始化顺序填充，
 * 所以声明顺序直接决定玩家在创造栏里看到的排列——保持与原版一致。
 * 尚未落地的族在下面留了占位注释，补的时候插回原位即可。
 */
public final class VCBlocks {

   public static final DeferredRegister<Block> BLOCKS =
           DeferredRegister.create(ForgeRegistries.BLOCKS, VariedCommodities.MODID);

   /**
    * 创造栏展示顺序。
    * 只在类初始化期间写入，之后纯读——JLS 保证 clinit happens-before 任何后续访问，
    * 故不需要并发容器；对外只暴露不可变视图，杜绝运行期被改。
    */
   private static final List<RegistryObject<Block>> CREATIVE_ORDER = new ArrayList<>();

   /** 台灯形状：地面与天花板共用 AABB_NORMAL，贴墙时按 8 档朝向取（原作只用偶数档）。 */
   private static final VoxelShape LAMP_FLOOR = Block.box(4.8D, 0.0D, 4.8D, 11.2D, 9.6D, 11.2D);
   private static final VoxelShape LAMP_WALL_DEFAULT = Block.box(4.8D, 3.2D, 4.8D, 11.2D, 11.2D, 11.2D);
   private static final VoxelShape[] LAMP_WALL = {
           Block.box(4.8D, 3.2D, 8.0D, 11.2D, 11.2D, 14.4D),   // 档 0（北面）
           LAMP_WALL_DEFAULT,
           Block.box(1.6D, 3.2D, 4.8D, 8.0D, 11.2D, 11.2D),    // 档 2（东面）
           LAMP_WALL_DEFAULT,
           Block.box(4.8D, 3.2D, 1.6D, 11.2D, 11.2D, 8.0D),    // 档 4（南面）
           LAMP_WALL_DEFAULT,
           Block.box(8.0D, 3.2D, 4.8D, 14.4D, 11.2D, 11.2D),   // 档 6（西面）
           LAMP_WALL_DEFAULT,
   };

   /** 大告示牌四个朝向的薄板形状（原作 AABB0~AABB3）。 */
   private static final VoxelShape[] BIG_SIGN_SHAPES = {
           Block.box(0.0D, 0.0D, 13.92D, 16.0D, 16.0D, 16.0D),
           Block.box(0.0D, 0.0D, 0.0D, 2.08D, 16.0D, 16.0D),
           Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.08D),
           Block.box(13.92D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D),
   };

   /** 墓碑只按朝向奇偶分两套形状（原作 AABB1/AABB2），这里摊成四项以便统一索引。 */
   private static final VoxelShape[] TOMBSTONE_SHAPES = {
           Block.box(0.0D, 0.0D, 4.8D, 16.0D, 16.0D, 11.2D),
           Block.box(4.8D, 0.0D, 0.0D, 11.2D, 16.0D, 16.0D),
           Block.box(0.0D, 0.0D, 4.8D, 16.0D, 16.0D, 11.2D),
           Block.box(4.8D, 0.0D, 0.0D, 11.2D, 16.0D, 16.0D),
   };

   // ⬜ trading_block —— 用户决定不移植（见 CLAUDE.md"不移植的内容"）

   /** 木工台：4×4 合成台，无方块实体。配方数据属 P6（datapack JSON，补时不改代码）。 */
   public static final RegistryObject<Block> CARPENTRY_BENCH = reg("carpentry_bench",
           () -> new BlockCarpentryBench(VCBlockProps.wood(), null));

   /** 铁砧款：借用原版铁砧的译名（原作靠 ItemBlockNamed 的 names 数组实现）。 */
   public static final RegistryObject<Block> CARPENTRY_BENCH_ANVIL = reg("carpentry_bench_anvil",
           () -> new BlockCarpentryBench(VCBlockProps.stone(), "block.minecraft.anvil"));

   public static final List<RegistryObject<Block>> BLOOD_BLOCK =
           numberedFamily("blood_block", 3, () -> new BlockBlood(VCBlockProps.blood()));

   /** 横幅：两格高、可染色、带徽记方块实体。 */
   public static final Map<VCMaterial, RegistryObject<Block>> BANNER =
           materialFamily("banner", material ->
                   new BlockBanner(VCBlockProps.stone(), DyeColor.byId(15 - material.ordinal())));

   /** 壁挂横幅：单格、无碰撞（原作 isPassable 为真）。 */
   public static final Map<VCMaterial, RegistryObject<Block>> WALL_BANNER =
           materialFamily("wall_banner", material ->
                   new BlockWallBanner(VCBlockProps.stone().noCollission(),
                           DyeColor.byId(15 - material.ordinal())));

   /** 告示牌：与横幅共用 TileBanner（徽记而非刻字），但不可染色。 */
   public static final Map<VCWood, RegistryObject<Block>> SIGN =
           woodFamily("sign", () -> new BlockSign(VCBlockProps.wood()));

   /**
    * 高脚灯：两格高、可染色、无方块实体。
    * 初始颜色 {@code DyeColor.byId(15 - 材质序号)} 沿用原作 {@code tile.color = 15 - meta}，
    * 与物品图标的着色式子相同，所以放下去的灯和手里那个同色。
    */
   public static final Map<VCMaterial, RegistryObject<Block>> TALL_LAMP =
           materialFamily("tall_lamp", material ->
                   new BlockTallLamp(VCBlockProps.tallLamp(), DyeColor.byId(15 - material.ordinal())));

   /**
    * 蜡烛 / 台灯 / 篝火：点燃态与熄灭态各是一个独立注册项，互为 counterpart。
    * 熄灭态用 {@link #regHidden} 注册——原作对它们 {@code setCreativeTab(null)}，
    * 玩家只能通过熄灭点燃态得到，创造栏里不该出现第二份。
    * 交叉引用必须写成 {@code VCBlocks.X} 的限定名：同类静态字段的简单名前向引用是编译错误。
    */
   public static final RegistryObject<Block> CANDLE = reg("candle",
           () -> new BlockCandle(VCBlockProps.woodLight(true), true, () -> VCBlocks.CANDLE_UNLIT.get()));
   public static final RegistryObject<Block> CANDLE_UNLIT = regHidden("candle_unlit",
           () -> new BlockCandle(VCBlockProps.woodLight(false), false, () -> VCBlocks.CANDLE.get()));

   /** 台灯除了形状之外没有任何特殊行为，直接用基类。 */
   public static final RegistryObject<Block> LAMP = reg("lamp",
           () -> new BlockBasicLightable(VCBlockProps.woodLight(true), true,
                   () -> VCBlocks.LAMP_UNLIT.get(), LAMP_FLOOR, LAMP_FLOOR, LAMP_WALL));
   public static final RegistryObject<Block> LAMP_UNLIT = regHidden("lamp_unlit",
           () -> new BlockBasicLightable(VCBlockProps.woodLight(false), false,
                   () -> VCBlocks.LAMP.get(), LAMP_FLOOR, LAMP_FLOOR, LAMP_WALL));

   public static final RegistryObject<Block> CAMPFIRE = reg("campfire",
           () -> new BlockCampfire(VCBlockProps.stoneLight(true), true, () -> VCBlocks.CAMPFIRE_UNLIT.get()));
   public static final RegistryObject<Block> CAMPFIRE_UNLIT = regHidden("campfire_unlit",
           () -> new BlockCampfire(VCBlockProps.stoneLight(false), false, () -> VCBlocks.CAMPFIRE.get()));

   public static final Map<VCWood, RegistryObject<Block>> CHAIR =
           woodFamily("chair", () -> new BlockSeat(VCBlockProps.wood(), BlockSeat.CHAIR));

   /** 羊毛沙发：左右拼接 + 转角拼接 + 可染色 + 能坐。 */
   public static final Map<VCWood, RegistryObject<Block>> COUCH_WOOL =
           woodFamily("couch_wool", wood ->
                   new BlockCouchWool(VCBlockProps.wood(), DyeColor.byId(15 - wood.ordinal())));

   /** 木沙发：只有左右拼接。 */
   public static final Map<VCWood, RegistryObject<Block>> COUCH_WOOD =
           woodFamily("couch_wood", wood ->
                   new BlockCouch(VCBlockProps.wood(), DyeColor.byId(15 - wood.ordinal())));

   /** 板条箱：54 格，界面直接用原版六行箱子（原作 GuiCrate 绑的就是原版贴图）。 */
   public static final Map<VCWood, RegistryObject<Block>> CRATE =
           woodFamily("crate", () -> new BlockCrate(VCBlockProps.wood(), StorageTier.WOOD));

   /**
    * 升级板条箱（本项目原创扩展，1.12 没有，不计入 REGISTRY_EXPANDED 契约）：不分木材，容量见 {@link StorageTier}。
    * 下界合金档对齐原版下界合金块：抗爆 1200、物品掉进岩浆不烧。
    */
   public static final RegistryObject<Block> CRATE_IRON = reg("crate_iron",
           () -> new BlockCrate(VCBlockProps.metalCrate(MapColor.METAL, SoundType.METAL, 10.0F),
                   StorageTier.IRON));
   public static final RegistryObject<Block> CRATE_DIAMOND = reg("crate_diamond",
           () -> new BlockCrate(VCBlockProps.metalCrate(MapColor.DIAMOND, SoundType.METAL, 10.0F),
                   StorageTier.DIAMOND));
   public static final RegistryObject<Block> CRATE_NETHERITE = reg("crate_netherite",
           () -> new BlockCrate(VCBlockProps.metalCrate(MapColor.COLOR_BLACK, SoundType.NETHERITE_BLOCK, 1200.0F),
                   StorageTier.NETHERITE),
           () -> new Item.Properties().fireResistant());

   /** 档位 → 升级板条箱。木质档就是上面六种木材的 CRATE，不在此表；EnumMap 保证按档位从低到高遍历。 */
   public static final Map<StorageTier, RegistryObject<Block>> UPGRADED_CRATE =
           Collections.unmodifiableMap(new EnumMap<>(Map.of(
                   StorageTier.IRON, CRATE_IRON,
                   StorageTier.DIAMOND, CRATE_DIAMOND,
                   StorageTier.NETHERITE, CRATE_NETHERITE)));

   /** 木桶：与箱子相同，只是旋转细分 8 档。 */
   public static final Map<VCWood, RegistryObject<Block>> BARREL =
           woodFamily("barrel", () -> new BlockBarrel(VCBlockProps.wood()));

   /** 武器架：两格高，方块实体只挂下半格。 */
   public static final Map<VCWood, RegistryObject<Block>> WEAPON_RACK =
           woodFamily("weapon_rack", () -> new BlockWeaponRack(VCBlockProps.wood()));

   /** 基座：五种材质只换贴图，方块属性都是石质（原作 BlockPedestal 一律 super(Blocks.STONE)）。 */
   public static final Map<VCMaterial, RegistryObject<Block>> PEDESTAL =
           materialFamily("pedestal", () -> new BlockPedestal(VCBlockProps.stone()));

   /** 1.12 的 BlockBreakable(GLASS,false) 对应 1.20.1 的 HalfTransparentBlock：相邻同种方块之间不画面。 */
   public static final Map<DyeColor, RegistryObject<Block>> CRYSTAL_BLOCK =
           colorFamily("crystal_block", color -> new HalfTransparentBlock(VCBlockProps.crystal(color)));

   public static final List<RegistryObject<Block>> PLACEHOLDER =
           numberedFamily("placeholder", 16, () -> new Block(VCBlockProps.placeholder()));

   public static final Map<VCWood, RegistryObject<Block>> STOOL =
           woodFamily("stool", () -> new BlockSeat(VCBlockProps.wood(), BlockSeat.STOOL));

   public static final Map<VCWood, RegistryObject<Block>> TABLE =
           woodFamily("table", () -> new BlockTable(VCBlockProps.wood()));

   public static final Map<VCWood, RegistryObject<Block>> SHELF =
           woodFamily("shelf", () -> new BlockShelf(VCBlockProps.wood()));

   public static final Map<VCWood, RegistryObject<Block>> BEAM =
           woodFamily("beam", () -> new BlockBeam(VCBlockProps.wood()));

   /** 大告示牌：贴墙的薄板，四个朝向各一套形状；无碰撞（原作 isPassable 为真）。 */
   public static final RegistryObject<Block> BIG_SIGN = reg("big_sign",
           () -> new BlockTextEditable(VCBlockProps.wood().noCollission(), BIG_SIGN_SHAPES, true));

   /** 书台：借用原版书本的译名（原作 getTranslationKey 返回 "item.book"）。 */
   public static final RegistryObject<Block> BOOK = reg("book",
           () -> new BlockBook(VCBlockProps.wood()));

   /** 墓碑：三款造型，**第三款不可刻字**（原作 meta>=2 既不开界面也不响应 wand）。 */
   public static final List<RegistryObject<Block>> TOMBSTONE =
           numberedFamily("tombstone", 3, index ->
                   new BlockTextEditable(VCBlockProps.stone(), TOMBSTONE_SHAPES, index < 2));

   private VCBlocks() {
   }

   public static void register(IEventBus bus) {
      BLOCKS.register(bus);
   }

   /** 创造栏用的注册顺序视图。 */
   public static List<RegistryObject<Block>> creativeOrder() {
      return Collections.unmodifiableList(CREATIVE_ORDER);
   }

   // ── 注册辅助 ─────────────────────────────────────────────────────────

   /**
    * 注册方块并同时挂上对应的 BlockItem。
    *
    * 这里会触碰 {@link VCItems#ITEMS}，从而在 VCBlocks 的类初始化阶段连带触发 VCItems 的类初始化。
    * 这是安全的：VCItems 自身的初始化不反向依赖 VCBlocks，不构成循环；
    * 且 DeferredRegister 允许先攒条目、后 register(bus)。
    */
   private static RegistryObject<Block> reg(String name, Supplier<Block> factory) {
      return reg(name, factory, Item.Properties::new);
   }

   /** 物品需要特殊属性时用这个重载（如下界合金板条箱的防火）。 */
   private static RegistryObject<Block> reg(String name, Supplier<Block> factory,
                                            Supplier<Item.Properties> itemProps) {
      RegistryObject<Block> block = BLOCKS.register(name, factory);
      VCItems.ITEMS.register(name, () -> new BlockItem(block.get(), itemProps.get()));
      CREATIVE_ORDER.add(block);
      return block;
   }

   /** 注册但不进创造栏（对应 1.12 的 setCreativeTab(null)）。 */
   private static RegistryObject<Block> regHidden(String name, Supplier<Block> factory) {
      RegistryObject<Block> block = BLOCKS.register(name, factory);
      VCItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
      return block;
   }

   private static Map<VCWood, RegistryObject<Block>> woodFamily(String base, Supplier<Block> factory) {
      return woodFamily(base, wood -> factory.get());
   }

   /** 各变体属性不同时用这个重载（如沙发的初始颜色随木材而变）。 */
   private static Map<VCWood, RegistryObject<Block>> woodFamily(String base, Function<VCWood, Block> factory) {
      EnumMap<VCWood, RegistryObject<Block>> map = new EnumMap<>(VCWood.class);
      for (VCWood wood : VCWood.values()) {
         map.put(wood, reg(base + "_" + wood.getSerializedName(), () -> factory.apply(wood)));
      }
      return Collections.unmodifiableMap(map);
   }

   private static Map<VCMaterial, RegistryObject<Block>> materialFamily(String base, Supplier<Block> factory) {
      return materialFamily(base, material -> factory.get());
   }

   /** 各变体属性不同时用这个重载（如高脚灯的初始颜色随材质而变）。 */
   private static Map<VCMaterial, RegistryObject<Block>> materialFamily(
           String base, Function<VCMaterial, Block> factory) {
      EnumMap<VCMaterial, RegistryObject<Block>> map = new EnumMap<>(VCMaterial.class);
      for (VCMaterial material : VCMaterial.values()) {
         map.put(material, reg(base + "_" + material.getSerializedName(), () -> factory.apply(material)));
      }
      return Collections.unmodifiableMap(map);
   }

   /** 按 1.12 的 meta 顺序注册：EnumDyeColor.byDyeDamage(meta) ⇒ meta0=黑 … meta15=白。 */
   private static Map<DyeColor, RegistryObject<Block>> colorFamily(String base, Function<DyeColor, Block> factory) {
      EnumMap<DyeColor, RegistryObject<Block>> map = new EnumMap<>(DyeColor.class);
      for (int meta = 0; meta < 16; meta++) {
         DyeColor color = DyeColor.byId(15 - meta);
         map.put(color, reg(base + "_" + color.getSerializedName(), () -> factory.apply(color)));
      }
      return Collections.unmodifiableMap(map);
   }

   private static List<RegistryObject<Block>> numberedFamily(String base, int count, IntFunction<Block> factory) {
      List<RegistryObject<Block>> list = new ArrayList<>(count);
      for (int i = 0; i < count; i++) {
         int index = i;
         list.add(reg(base + "_" + i, () -> factory.apply(index)));
      }
      return Collections.unmodifiableList(list);
   }

   private static List<RegistryObject<Block>> numberedFamily(String base, int count, Supplier<Block> factory) {
      List<RegistryObject<Block>> list = new ArrayList<>(count);
      for (int i = 0; i < count; i++) {
         list.add(reg(base + "_" + i, factory));
      }
      return Collections.unmodifiableList(list);
   }
}
