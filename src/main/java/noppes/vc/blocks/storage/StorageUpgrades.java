package noppes.vc.blocks.storage;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.blocks.BlockCrate;
import noppes.vc.blocks.tiles.TileCrate;
import noppes.vc.config.VCConfig;
import noppes.vc.init.VCBlocks;

/**
 * 板条箱就地升级：潜行 + 手持更高档材料右键，换成对应档位的方块并保留全部内容。
 *
 * <p>判定两端各跑一遍：客户端只负责拦下原版的放置预测并挥手，真正的替换与扣料只在服务端做。
 */
public final class StorageUpgrades {

   private static final String MSG_COST = "variedcommodities.message.crate_upgrade_cost";

   private StorageUpgrades() {
   }

   /**
    * @return {@code PASS} 表示这次点击与升级无关、交还原版流程；其余结果表示点击已被升级消费。
    */
   public static InteractionResult tryUpgrade(Level level, BlockPos pos, Player player, InteractionHand hand) {
      // 先做不碰世界的廉价判断：这个方法对每一次右键方块都会调用
      if (!player.isSecondaryUseActive() || player.isSpectator() || !player.mayBuild()) {
         return InteractionResult.PASS;
      }
      ItemStack held = player.getItemInHand(hand);
      StorageTier target = held.isEmpty() ? null : StorageTier.byMaterial(held);
      if (target == null) {
         return InteractionResult.PASS;
      }
      // pos 是玩家刚点中的方块，所在区块必然已加载
      BlockState state = level.getBlockState(pos);
      if (!(state.getBlock() instanceof BlockCrate crate) || !crate.upgradable()
              || !target.isAbove(crate.tier()) || !VCConfig.crateUpgradesEnabled()) {
         return InteractionResult.PASS;
      }
      RegistryObject<Block> targetBlock = VCBlocks.UPGRADED_CRATE.get(target);
      if (targetBlock == null) {
         return InteractionResult.PASS;
      }

      int cost = VCConfig.crateUpgradeCost(target);
      boolean creative = player.getAbilities().instabuild;
      if (!creative && held.getCount() < cost) {
         if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable(MSG_COST, cost,
                    target.displayMaterial().getDescription()), true);
         }
         // 仍要消费掉这次点击，否则原版会接着把手里的铁块放到箱子旁边
         return InteractionResult.FAIL;
      }
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }
      if (!(level.getBlockEntity(pos) instanceof TileCrate tile)) {
         return InteractionResult.FAIL;
      }

      replace(level, pos, state, crate, targetBlock.get(), tile);
      if (!creative) {
         held.shrink(cost);
      }
      level.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
      level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
      return InteractionResult.CONSUME;
   }

   /**
    * 顺序不能乱：先摘空旧箱子再换方块，此时 {@code onRemove} 拿到的是空箱子，不会把东西撒一地；
    * 换完再写回新方块实体。朝向原样带过去。
    */
   private static void replace(Level level, BlockPos pos, BlockState old, BlockCrate from, Block to, TileCrate tile) {
      List<ItemStack> contents = tile.takeAll();

      IntegerProperty facing = from.rotation();
      BlockState next = to.defaultBlockState();
      if (next.hasProperty(facing)) {
         next = next.setValue(facing, old.getValue(facing));
      }
      level.setBlock(pos, next, Block.UPDATE_ALL);

      // 新方块必带方块实体，拿不到只可能是被别的模组截走了——那就掉在地上，绝不吞物品
      List<ItemStack> overflow = level.getBlockEntity(pos) instanceof TileCrate fresh
              ? fresh.restore(contents)
              : contents;
      for (ItemStack stack : overflow) {
         Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, stack);
      }
   }
}
