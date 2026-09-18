package noppes.vc.init;

import java.util.Arrays;
import java.util.Collection;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.VariedCommodities;
import noppes.vc.blocks.tiles.TileBanner;
import noppes.vc.blocks.tiles.TileBigSign;
import noppes.vc.blocks.tiles.TileBook;
import noppes.vc.blocks.tiles.TileCrate;
import noppes.vc.blocks.tiles.TilePedestal;
import noppes.vc.blocks.tiles.TileWeaponRack;

/**
 * 方块实体注册。
 *
 * 1.12 为每个木材/材质变体建了一个空子类（TileTable1..5），只是为了让 TESR 认出该画哪种贴图；
 * 1.20.1 变体已是独立方块，一个 BlockEntityType 直接绑定整族方块即可，
 * 27 个方块实体类因此大幅缩水（详见 CLAUDE.md P3 的调查结论）。
 */
public final class VCBlockEntities {

   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
           DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, VariedCommodities.MODID);

   public static final RegistryObject<BlockEntityType<TilePedestal>> PEDESTAL =
           BLOCK_ENTITIES.register("pedestal", () -> BlockEntityType.Builder
                   .of(TilePedestal::new, blocks(VCBlocks.PEDESTAL.values()))
                   .build(null));

   public static final RegistryObject<BlockEntityType<TileWeaponRack>> WEAPON_RACK =
           BLOCK_ENTITIES.register("weapon_rack", () -> BlockEntityType.Builder
                   .of(TileWeaponRack::new, blocks(VCBlocks.WEAPON_RACK.values()))
                   .build(null));

   /** 横幅、壁挂横幅、告示牌共用一个类型：三者的数据完全一样（徽记 + 编辑窗口）。 */
   public static final RegistryObject<BlockEntityType<TileBanner>> BANNER =
           BLOCK_ENTITIES.register("banner", () -> BlockEntityType.Builder
                   .of(TileBanner::new, blocks(VCBlocks.BANNER.values(), VCBlocks.WALL_BANNER.values(),
                           VCBlocks.SIGN.values()))
                   .build(null));

   /**
    * 板条箱（含升级档）与木桶共用：数据完全一致，槽位数由方块的档位决定，
    * 界面标题取自各自方块的 descriptionId。新方块必须列进来，否则放下时方块实体会被拒收。
    */
   public static final RegistryObject<BlockEntityType<TileCrate>> CRATE =
           BLOCK_ENTITIES.register("crate", () -> BlockEntityType.Builder
                   .of(TileCrate::new, blocks(VCBlocks.CRATE.values(), VCBlocks.UPGRADED_CRATE.values(),
                           VCBlocks.BARREL.values()))
                   .build(null));

   /** 大告示牌与前两款墓碑共用：数据都是"一段文字 + 一次性写入锁"。 */
   public static final RegistryObject<BlockEntityType<TileBigSign>> BIG_SIGN =
           BLOCK_ENTITIES.register("big_sign", () -> BlockEntityType.Builder
                   .of(TileBigSign::new, textBlocks())
                   .build(null));

   public static final RegistryObject<BlockEntityType<TileBook>> BOOK =
           BLOCK_ENTITIES.register("book", () -> BlockEntityType.Builder
                   .of(TileBook::new, VCBlocks.BOOK.get())
                   .build(null));

   private VCBlockEntities() {
   }

   public static void register(IEventBus bus) {
      BLOCK_ENTITIES.register(bus);
   }

   /** 大告示牌 + 可刻字的两款墓碑（第三款不可刻字，没有方块实体）。 */
   private static Block[] textBlocks() {
      return new Block[]{
              VCBlocks.BIG_SIGN.get(),
              VCBlocks.TOMBSTONE.get(0).get(),
              VCBlocks.TOMBSTONE.get(1).get(),
      };
   }

   /** 在 Supplier 内部调用，确保取值发生在 VCBlocks 类初始化完成之后。 */
   @SafeVarargs
   private static Block[] blocks(Collection<RegistryObject<Block>>... groups) {
      return Arrays.stream(groups)
              .flatMap(Collection::stream)
              .map(RegistryObject::get)
              .toArray(Block[]::new);
   }
}
