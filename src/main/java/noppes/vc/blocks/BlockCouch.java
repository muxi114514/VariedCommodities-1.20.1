package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import noppes.vc.entity.EntityChairMount;

/**
 * 沙发：能坐、可染色，相邻同朝向的沙发会拼接成一排。
 *
 * <p>拼接标志原作存在方块实体里（{@code hasLeft}/{@code hasRight}），并且判断时要
 * {@code world.getTileEntity(邻居)} 才能拿到邻居的朝向。朝向进 blockstate 之后，
 * {@link #updateShape} 传进来的邻居状态本身就带朝向，于是整套判断变成纯状态比较，
 * 既不查方块实体也不碰世界——这个方块实体也就没有存在的必要了。
 *
 * <p>左右是相对沙发自身朝向的，映射逐字取自原作 {@code updateModel}。
 */
public class BlockCouch extends BlockSeat {

   public static final BooleanProperty LEFT = BooleanProperty.create("left");
   public static final BooleanProperty RIGHT = BooleanProperty.create("right");

   private final DyeColor defaultColor;

   public BlockCouch(Properties props, DyeColor defaultColor) {
      super(props, Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D));
      this.defaultColor = defaultColor;
      registerDefaultState(defaultBlockState()
              .setValue(LEFT, false)
              .setValue(RIGHT, false)
              .setValue(VCDye.COLOR, defaultColor));
   }

   public DyeColor defaultColor() {
      return this.defaultColor;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(LEFT, RIGHT, VCDye.COLOR);
   }

   /** 朝向 0→西 1→北 2→东 3→南，取自原作 updateModel 的分支。 */
   protected static Direction leftOf(int rotation) {
      return switch (rotation & 3) {
         case 0 -> Direction.WEST;
         case 1 -> Direction.NORTH;
         case 2 -> Direction.EAST;
         default -> Direction.SOUTH;
      };
   }

   protected static Direction rightOf(int rotation) {
      return leftOf(rotation).getOpposite();
   }

   /** 背后那一格，羊毛沙发的转角判定要用。 */
   protected static Direction behindOf(int rotation) {
      return switch (rotation & 3) {
         case 0 -> Direction.NORTH;
         case 1 -> Direction.EAST;
         case 2 -> Direction.SOUTH;
         default -> Direction.WEST;
      };
   }

   /**
    * 同一方块且同朝向才算拼上。
    * 原作还比了 meta（木材/颜色档），而现在那一档就是方块身份，{@code is()} 已经覆盖。
    */
   protected boolean connects(BlockState self, BlockState neighbor) {
      return neighbor.is(this)
              && neighbor.getValue(rotation()).intValue() == self.getValue(rotation()).intValue();
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      BlockState state = super.getStateForPlacement(ctx);
      if (state == null) {
         return null;
      }
      LevelReader level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      int rot = state.getValue(rotation());
      return state
              .setValue(LEFT, connects(state, level.getBlockState(pos.relative(leftOf(rot)))))
              .setValue(RIGHT, connects(state, level.getBlockState(pos.relative(rightOf(rot)))));
   }

   @Override
   public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor,
                                 LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      int rot = state.getValue(rotation());
      if (dir == leftOf(rot)) {
         return state.setValue(LEFT, connects(state, neighbor));
      }
      if (dir == rightOf(rot)) {
         return state.setValue(RIGHT, connects(state, neighbor));
      }
      return state;
   }

   /** 先判染料，否则落座——与原作的分支顺序一致。 */
   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
      DyeColor dye = VCDye.heldDye(player, hand);
      if (dye == null) {
         return EntityChairMount.sit(level, pos, player);
      }
      if (state.getValue(VCDye.COLOR) != dye) {
         if (!level.isClientSide) {
            VCDye.recolor(level, pos, this, dye);
            VCDye.consume(player, hand);
         }
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
   }
}
