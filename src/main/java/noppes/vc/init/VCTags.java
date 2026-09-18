package noppes.vc.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import noppes.vc.VariedCommodities;

/** 本模组的标签键。用标签而非 instanceof 判定，附属模组/数据包可直接扩展。 */
public final class VCTags {

   /** 桌子：互相拼接时会隐藏内侧桌腿（见 {@link noppes.vc.blocks.BlockTable}）。 */
   public static final TagKey<Block> TABLES = block("tables");

   private VCTags() {
   }

   private static TagKey<Block> block(String path) {
      return BlockTags.create(new ResourceLocation(VariedCommodities.MODID, path));
   }
}
