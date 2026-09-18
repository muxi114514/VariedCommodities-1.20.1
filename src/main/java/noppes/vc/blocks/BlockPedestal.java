package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import noppes.vc.blocks.tiles.TilePedestal;

/**
 * 剑座：单格物品栏，放入剑后对外发 15 级红石信号。
 *
 * 碰撞箱随朝向在两个方向间切换，数值取自 1.12 的 AABB1/AABB2。
 * 原作是从方块实体读朝向来选碰撞箱（每次碰撞检测查一次 TE），朝向进 blockstate 后变成纯状态读取。
 */
public class BlockPedestal extends BlockBasicTrigger implements EntityBlock {

   /** AABB1：x 通长、z 收窄（朝向 0/2） */
   private static final VoxelShape SHAPE_WIDE_X = Block.box(0.0D, 0.0D, 3.2D, 16.0D, 8.0D, 12.8D);
   /** AABB2：z 通长、x 收窄（朝向 1/3） */
   private static final VoxelShape SHAPE_WIDE_Z = Block.box(3.2D, 0.0D, 0.0D, 12.8D, 8.0D, 16.0D);

   public BlockPedestal(Properties props) {
      super(props);
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return state.getValue(rotation()) % 2 == 0 ? SHAPE_WIDE_X : SHAPE_WIDE_Z;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TilePedestal(pos, state);
   }

   /**
    * 空手取剑 / 持剑放剑。只认主手，且只收剑——与原作一致。
    * 注意"只收剑"是右键交互层的限制，物品栏本身不设限（漏斗仍可塞入任意物品），原作即如此。
    */
   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
      if (hand != InteractionHand.MAIN_HAND) {
         return InteractionResult.PASS;
      }
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }
      if (!(level.getBlockEntity(pos) instanceof TilePedestal tile)) {
         return InteractionResult.PASS;
      }

      ItemStack held = player.getMainHandItem();
      ItemStack stored = tile.getItem(0);
      if (held.isEmpty() && !stored.isEmpty()) {
         tile.setItem(0, ItemStack.EMPTY);
         player.setItemInHand(InteractionHand.MAIN_HAND, stored);
      } else if (stored.isEmpty() && held.getItem() instanceof SwordItem) {
         tile.setItem(0, held.copy());
         player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
      } else {
         return InteractionResult.CONSUME;
      }

      tile.setChanged();
      level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
      IPowerProvider.updateSurrounding(level, pos, this);
      return InteractionResult.CONSUME;
   }

   /** 拆除时掉出内容物并刷新比较器。方块自身的掉落由战利品表负责。 */
   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof TilePedestal tile) {
            Containers.dropContents(level, pos, tile);
            level.updateNeighbourForOutputSignal(pos, this);
         }
         super.onRemove(state, level, pos, newState, moving);
      }
   }
}
