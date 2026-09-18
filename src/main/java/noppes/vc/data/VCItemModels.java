package noppes.vc.data;

import java.util.List;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.VariedCommodities;
import noppes.vc.init.VCBlocks;
import noppes.vc.init.VCItems;
import noppes.vc.init.VCWeapons;

/**
 * 物品模型。
 *
 * 目前只有方块物品：直接 parent 到对应方块模型即可。
 * 桌子例外——它的方块模型是 multipart（桌腿按邻接显隐），物品得用带完整四条腿的
 * {@code *_inventory} 模型，否则手上拿的桌子是缺腿的。
 *
 * P2 落地后这里再补 338 个物品：绝大多数是 item/generated + 单层贴图，
 * 需要特判的是 orb / orb_broken / elemental_staff（16 色共用一张灰度图，靠 IItemColor 着色，
 * 不能按变体名去找 16 张不存在的贴图）以及枪/法杖/盾的手持姿态。
 */
public class VCItemModels extends ItemModelProvider {

   public VCItemModels(PackOutput output, ExistingFileHelper helper) {
      super(output, VariedCommodities.MODID, helper);
   }

   @Override
   protected void registerModels() {
      colorVariants();
      VCBlocks.TABLE.values().forEach(block -> fromBlock(block, path(block) + "_inventory"));
      VCBlocks.BLOOD_BLOCK.forEach(block -> fromBlock(block, path(block)));
      VCBlocks.SIGN.values().forEach(block -> fromBlock(block, path(block)));
      for (RegistryObject<Block> block : List.of(VCBlocks.CANDLE, VCBlocks.CANDLE_UNLIT,
              VCBlocks.LAMP, VCBlocks.LAMP_UNLIT, VCBlocks.CAMPFIRE, VCBlocks.CAMPFIRE_UNLIT)) {
         fromBlock(block, path(block));
      }
      VCBlocks.CHAIR.values().forEach(block -> fromBlock(block, path(block)));
      VCBlocks.STOOL.values().forEach(block -> fromBlock(block, path(block)));
      VCBlocks.SHELF.values().forEach(block -> fromBlock(block, path(block)));
      VCBlocks.CRATE.values().forEach(block -> fromBlock(block, path(block)));
      VCBlocks.UPGRADED_CRATE.values().forEach(block -> fromBlock(block, path(block)));
      VCBlocks.BARREL.values().forEach(block -> fromBlock(block, path(block)));
      fromBlock(VCBlocks.BIG_SIGN, path(VCBlocks.BIG_SIGN));
      fromBlock(VCBlocks.BOOK, path(VCBlocks.BOOK));
      fromBlock(VCBlocks.CARPENTRY_BENCH, path(VCBlocks.CARPENTRY_BENCH));
      fromBlock(VCBlocks.CARPENTRY_BENCH_ANVIL, path(VCBlocks.CARPENTRY_BENCH_ANVIL));
      VCBlocks.TOMBSTONE.forEach(block -> fromBlock(block, path(block)));
      VCBlocks.COUCH_WOOD.values().forEach(block -> fromBlock(block, path(block)));
      VCBlocks.COUCH_WOOL.values().forEach(block -> fromBlock(block, path(block)));
      VCBlocks.BEAM.values().forEach(block -> fromBlock(block, path(block)));
      VCBlocks.PEDESTAL.values().forEach(block -> fromBlock(block, path(block)));
      // 双格方块的图标取下半格的模型（原作物品图标也只画下半截）
      VCBlocks.WEAPON_RACK.values().forEach(block -> fromBlock(block, path(block)));
      VCBlocks.TALL_LAMP.values().forEach(block -> fromBlock(block, path(block)));
      VCBlocks.BANNER.values().forEach(block -> fromBlock(block, path(block)));
      VCBlocks.WALL_BANNER.values().forEach(block -> fromBlock(block, path(block)));
      VCBlocks.CRYSTAL_BLOCK.values().forEach(block -> fromBlock(block, "crystal_block"));
      VCBlocks.PLACEHOLDER.forEach(block -> fromBlock(block, path(block)));
   }

   /**
    * orb / orb_broken / elemental_staff 的 16 色变体。
    *
    * 1.12 只有一个 {@code orb.json}，16 个 meta 共用它，靠 IItemColor 着色。
    * 1.20.1 变体是独立注册项、各自需要一个模型文件，但仍指向同一张灰度贴图；
    * {@code item/generated} 的 layer0 自带 tintindex 0，着色器（VCClient）接上即可。
    * 其余 339 个物品模型是从 1.12 原样搬运的静态资源（含武器手持姿态），不在这里生成。
    */
   private void colorVariants() {
      VCItems.ORB.values().forEach(item -> tinted(item, "orb"));
      VCItems.ORB_BROKEN.values().forEach(item -> tinted(item, "orb_broken"));
      VCWeapons.ELEMENTAL_STAFF.values().forEach(item -> tinted(item, "elemental_staff"));
   }

   private void tinted(RegistryObject<Item> item, String texture) {
      withExistingParent(item.getId().getPath(), mcLoc("item/generated"))
              .texture("layer0", modLoc("item/" + texture));
   }

   private void fromBlock(RegistryObject<Block> block, String blockModel) {
      withExistingParent(path(block), modLoc("block/" + blockModel));
   }

   private static String path(RegistryObject<Block> block) {
      return block.getId().getPath();
   }
}
