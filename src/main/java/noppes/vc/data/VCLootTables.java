package noppes.vc.data;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import noppes.vc.blocks.BlockBasicDouble;
import noppes.vc.blocks.BlockBasicLightable;
import noppes.vc.init.VCBlocks;

/**
 * 方块战利品表。
 *
 * 1.12 靠 Block#damageDropped 返回 meta 来掉落对应子方块；1.20.1 没有 meta，
 * 变体已经拆成独立方块，因此绝大多数就是"挖啥掉啥"。
 * 这里默认对全部已注册方块 dropSelf，好处是 P3 每加一个方块就自动有表，
 * 不会出现"漏写战利品表 → 挖了不掉东西"这种移植版经典事故。
 * 少数例外（熄灭态掉点燃态、血迹方块不掉落等）留到 P3 单独覆写。
 */
public class VCLootTables extends LootTableProvider {

   public VCLootTables(PackOutput output) {
      super(output, Set.of(), List.of(
              new SubProviderEntry(VCBlockLoot::new, LootContextParamSets.BLOCK)));
   }

   private static final class VCBlockLoot extends BlockLootSubProvider {

      private VCBlockLoot() {
         super(Set.of(), FeatureFlags.REGISTRY.allFlags());
      }

      @Override
      protected void generate() {
         getKnownBlocks().forEach(block -> {
            if (block instanceof BlockBasicLightable lightable && !lightable.isLit()) {
               // 熄灭态掉落点燃态，对应原作 getItemDropped 返回 litBlock()
               add(block, createSingleItemTable(lightable.counterpart()));
            } else if (block instanceof BlockBasicDouble) {
               // 两格高方块共用一张战利品表，必须按 HALF 过滤，
               // 否则上下两半各掉一次，拆一个武器架能拿到两个
               add(block, createSinglePropConditionTable(block, BlockBasicDouble.HALF, DoubleBlockHalf.LOWER));
            } else {
               dropSelf(block);
            }
         });
      }

      /** 只认本模组的方块，避免把原版方块的表也重写一遍。 */
      @Override
      protected Iterable<Block> getKnownBlocks() {
         return VCBlocks.BLOCKS.getEntries().stream()
                 .map(RegistryObject::get)
                 .collect(Collectors.toList());
      }
   }
}
