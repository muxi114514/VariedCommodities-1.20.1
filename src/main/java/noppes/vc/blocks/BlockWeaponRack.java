package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import noppes.vc.blocks.tiles.TileWeaponRack;

/**
 * 武器架：两格高，三个展示格，按点击位置决定操作哪一格；每格占用贡献 5 级红石信号。
 *
 * <p>双格方块的通用逻辑在 {@link BlockBasicDouble}。红石部分没法一起继承
 * （Java 单继承，{@link BlockBasicTrigger} 在另一条链上），所以这里直接覆写三个钩子，
 * 实际逻辑仍然只有 {@link IPowerProvider} 里那一份。
 */
public class BlockWeaponRack extends BlockBasicDouble implements EntityBlock {

   private static final int SLOTS = 3;
   /** 原作的分段常数：hit/0.34 得到 0/1/2，再用 2 减得到槽位。 */
   private static final double SLOT_WIDTH = 0.34D;

   /** 下半格整格高，上半格 0.8 格高，合计 1.8 格——与原作 AABB 的总高一致。 */
   private static final VoxelShape[] LOWER = {
           Block.box(0.0D, 0.0D, 11.2D, 16.0D, 16.0D, 16.0D),   // 朝向 0：南
           Block.box(0.0D, 0.0D, 0.0D, 4.8D, 16.0D, 16.0D),     // 朝向 1：西
           Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.8D),     // 朝向 2：北
           Block.box(11.2D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D),   // 朝向 3：东
   };
   private static final VoxelShape[] UPPER = {
           Block.box(0.0D, 0.0D, 11.2D, 16.0D, 12.8D, 16.0D),
           Block.box(0.0D, 0.0D, 0.0D, 4.8D, 12.8D, 16.0D),
           Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.8D, 4.8D),
           Block.box(11.2D, 0.0D, 0.0D, 16.0D, 12.8D, 16.0D),
   };

   public BlockWeaponRack(Properties props) {
      super(props);
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      VoxelShape[] shapes = state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER : UPPER;
      return shapes[state.getValue(rotation()) & 3];
   }

   // ── 红石源（逻辑在 IPowerProvider，此处只是接线）────────────────────────

   @Override
   public boolean isSignalSource(BlockState state) {
      return true;
   }

   @Override
   public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
      return IPowerProvider.signalAt(level, pos);
   }

   @Override
   public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
      return getSignal(state, level, pos, side);
   }

   // ── 方块实体只挂在下半格 ─────────────────────────────────────────────

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new TileWeaponRack(pos, state) : null;
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof TileWeaponRack tile) {
            Containers.dropContents(level, pos, tile);
            level.updateNeighbourForOutputSignal(pos, this);
         }
         super.onRemove(state, level, pos, newState, moving);
      }
   }

   // ── 交互 ─────────────────────────────────────────────────────────────

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
      if (hand != InteractionHand.MAIN_HAND) {
         return InteractionResult.PASS;
      }
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }

      BlockPos base = baseOf(pos, state);
      BlockState baseState = level.getBlockState(base);
      if (!(level.getBlockEntity(base) instanceof TileWeaponRack tile)) {
         return InteractionResult.PASS;
      }

      int slot = slotFor(state, hit);
      ItemStack held = player.getMainHandItem();
      ItemStack stored = tile.getItem(slot);
      if (held.isEmpty() && !stored.isEmpty()) {
         tile.setItem(slot, ItemStack.EMPTY);
         player.setItemInHand(InteractionHand.MAIN_HAND, stored);
      } else if (stored.isEmpty() && !held.isEmpty() && !(held.getItem() instanceof BlockItem)) {
         tile.setItem(slot, held.copy());
         player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
      } else {
         return InteractionResult.CONSUME;
      }

      tile.setChanged();
      level.sendBlockUpdated(base, baseState, baseState, Block.UPDATE_ALL);
      IPowerProvider.updateSurrounding(level, base, this);
      return InteractionResult.CONSUME;
   }

   /**
    * 按点击位置在三格之间取舍，公式逐字沿用原作。
    * 沿架子方向的坐标随朝向在 hitX / hitZ 之间切换并按需取反，使"最左格"始终是 0 号槽。
    */
   private int slotFor(BlockState state, BlockHitResult hit) {
      BlockPos hitPos = hit.getBlockPos();
      double hx = hit.getLocation().x - hitPos.getX();
      double hz = hit.getLocation().z - hitPos.getZ();
      double along = switch (state.getValue(rotation()) & 3) {
         case 1 -> hz;
         case 2 -> 1.0D - hx;
         case 3 -> 1.0D - hz;
         default -> hx;
      };
      return Mth.clamp(2 - (int) (along / SLOT_WIDTH), 0, SLOTS - 1);
   }
}
