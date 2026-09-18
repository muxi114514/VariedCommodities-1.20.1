package noppes.vc.data;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.VariedCommodities;
import noppes.vc.init.VCBlocks;
import noppes.vc.init.VCTags;

/**
 * 方块标签。
 *
 * 除本模组自用的 {@link VCTags#TABLES} 外，还要补 mineable 标签：1.12 的"哪种工具挖得快"
 * 由 Material 隐式决定（WOOD→斧、ROCK→镐），1.20.1 改成显式标签，
 * 不加的话所有家具都退化成徒手速度——这不是忠实移植，是丢行为。
 */
public class VCBlockTags extends BlockTagsProvider {

   public VCBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                      ExistingFileHelper helper) {
      super(output, lookup, VariedCommodities.MODID, helper);
   }

   @Override
   protected void addTags(HolderLookup.Provider provider) {
      tag(VCTags.TABLES).add(blocks(VCBlocks.TABLE.values()));

      tag(BlockTags.MINEABLE_WITH_AXE)
              .add(blocks(VCBlocks.TABLE.values()))
              .add(blocks(VCBlocks.BEAM.values()))
              .add(blocks(VCBlocks.WEAPON_RACK.values()))
              .add(blocks(VCBlocks.TALL_LAMP.values()))
              .add(blocks(VCBlocks.SIGN.values()))
              .add(blocks(VCBlocks.CHAIR.values()))
              .add(blocks(VCBlocks.STOOL.values()))
              .add(blocks(VCBlocks.SHELF.values()))
              .add(blocks(VCBlocks.COUCH_WOOD.values()))
              .add(blocks(VCBlocks.COUCH_WOOL.values()))
              .add(blocks(VCBlocks.CRATE.values()))
              .add(blocks(VCBlocks.BARREL.values()))
              .add(VCBlocks.CANDLE.get(), VCBlocks.CANDLE_UNLIT.get(),
                      VCBlocks.LAMP.get(), VCBlocks.LAMP_UNLIT.get(),
                      VCBlocks.BIG_SIGN.get(), VCBlocks.BOOK.get(),
                      VCBlocks.CARPENTRY_BENCH.get());

      tag(BlockTags.MINEABLE_WITH_PICKAXE)
              .add(blocks(VCBlocks.PLACEHOLDER))
              .add(blocks(VCBlocks.UPGRADED_CRATE.values()))
              .add(blocks(VCBlocks.PEDESTAL.values()))
              .add(blocks(VCBlocks.BANNER.values()))
              .add(blocks(VCBlocks.WALL_BANNER.values()))
              .add(VCBlocks.CAMPFIRE.get(), VCBlocks.CAMPFIRE_UNLIT.get())
              .add(blocks(VCBlocks.TOMBSTONE))
              .add(VCBlocks.CARPENTRY_BENCH_ANVIL.get());
   }

   private static Block[] blocks(Collection<RegistryObject<Block>> entries) {
      return entries.stream().map(RegistryObject::get).toArray(Block[]::new);
   }
}
