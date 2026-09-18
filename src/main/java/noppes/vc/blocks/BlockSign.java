package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import noppes.vc.VariedCommodities;
import noppes.vc.blocks.tiles.TileBanner;

/**
 * 告示牌。
 *
 * <p><b>注意：它不是刻字牌，而是徽记牌。</b>原作的 {@code TileSign extends TileBanner}——
 * 和横幅共用同一套数据（徽记 ItemStack + 10 秒编辑窗口），因此这里直接复用 {@link TileBanner}。
 * 真正能刻字的只有大告示牌与墓碑（{@code TileBigSign}）。
 *
 * <p>与横幅的差别是它不能染色：原作的 {@code onBlockActivated} 只返回 {@code canEdit()}，
 * 没有染料分支。木材档只影响贴图。
 */
public class BlockSign extends BlockBasicRotated implements EntityBlock {

   /** 朝向奇数用 AABB1（沿 Z 展开），偶数用 AABB2（沿 X 展开）。 */
   private static final VoxelShape ALONG_Z = Block.box(4.8D, 4.8D, 0.0D, 11.2D, 16.0D, 16.0D);
   private static final VoxelShape ALONG_X = Block.box(0.0D, 4.8D, 4.8D, 16.0D, 16.0D, 11.2D);

   public BlockSign(Properties props) {
      super(props);
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return state.getValue(rotation()) % 2 == 1 ? ALONG_Z : ALONG_X;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileBanner(pos, state);
   }

   @Override
   public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      if (level.isClientSide) {
         return;
      }
      if (level.getBlockEntity(pos) instanceof TileBanner tile) {
         tile.startEditWindow();
      }
      if (placer instanceof Player player) {
         player.displayClientMessage(
                 Component.translatable(VariedCommodities.MODID + ".message.edit_icon"), true);
      }
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
      if (hand != InteractionHand.MAIN_HAND) {
         return InteractionResult.PASS;
      }
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }
      if (level.getBlockEntity(pos) instanceof TileBanner tile
              && tile.tryEditIcon(player.getItemInHand(hand))) {
         level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
         return InteractionResult.CONSUME;
      }
      return InteractionResult.PASS;
   }
}
