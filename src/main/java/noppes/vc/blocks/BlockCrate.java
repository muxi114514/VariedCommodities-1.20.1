package noppes.vc.blocks;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import noppes.vc.blocks.storage.StorageTier;
import noppes.vc.blocks.tiles.TileCrate;
import noppes.vc.config.VCConfig;

/**
 * 板条箱：整方块容器，容量由 {@link StorageTier} 决定（木质 54 格，升级后 81/108/135）。
 *
 * <p>木桶（{@link BlockBarrel}）只是旋转档数不同，故继承本类。
 * 升级逻辑不在这里：潜行 + 手持物品时原版根本不调 {@link #use}，见 {@code StorageUpgradeHandler}。
 */
public class BlockCrate extends BlockBasicRotated implements EntityBlock {

   private static final String TOOLTIP_CAPACITY = "variedcommodities.tooltip.crate_capacity";
   private static final String TOOLTIP_UPGRADE = "variedcommodities.tooltip.crate_upgrade";

   private final StorageTier tier;

   public BlockCrate(Properties props, StorageTier tier) {
      super(props);
      this.tier = tier;
   }

   public StorageTier tier() {
      return this.tier;
   }

   /** 能否就地升级。木桶覆写为 false（用户决定只做板条箱）。 */
   public boolean upgradable() {
      return true;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileCrate(pos, state);
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }
      if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof TileCrate tile) {
         level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS,
                 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
         tile.openFor(serverPlayer);
      }
      return InteractionResult.CONSUME;
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof TileCrate tile) {
            Containers.dropContents(level, pos, tile);
            level.updateNeighbourForOutputSignal(pos, this);
         }
         super.onRemove(state, level, pos, newState, moving);
      }
   }

   // 比较器：原先调了 updateNeighbourForOutputSignal 却没声明有输出，比较器读不到满度
   @Override
   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   @Override
   public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
   }

   @Override
   public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable(TOOLTIP_CAPACITY, this.tier.size()).withStyle(ChatFormatting.GRAY));
      if (upgradable() && VCConfig.crateUpgradesEnabled()) {
         List<Component> materials = this.tier.higher().stream()
                 .map(target -> target.displayMaterial().getDescription())
                 .toList();
         if (!materials.isEmpty()) {
            tooltip.add(Component.translatable(TOOLTIP_UPGRADE,
                    ComponentUtils.formatList(materials, Component.literal(" / "))).withStyle(ChatFormatting.GRAY));
         }
      }
   }
}
