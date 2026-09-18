package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * 羊毛沙发：在 {@link BlockCouch} 的左右拼接之外，还能与背后成直角的沙发拼出转角。
 *
 * 转角判定：背后那一格是同种沙发，且其朝向恰好是自身朝向 ±1（模 4）。
 * 逐字取自原作 {@code compareCornerTiles} 的 {@code (rotation + (isLeft ? 3 : 1)) % 4}。
 */
public class BlockCouchWool extends BlockCouch {

   public static final BooleanProperty CORNER_LEFT = BooleanProperty.create("corner_left");
   public static final BooleanProperty CORNER_RIGHT = BooleanProperty.create("corner_right");

   public BlockCouchWool(Properties props, DyeColor defaultColor) {
      super(props, defaultColor);
      registerDefaultState(defaultBlockState()
              .setValue(CORNER_LEFT, false)
              .setValue(CORNER_RIGHT, false));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(CORNER_LEFT, CORNER_RIGHT);
   }

   private boolean corner(BlockState self, BlockState neighbor, boolean left) {
      if (!neighbor.is(this)) {
         return false;
      }
      int expected = (self.getValue(rotation()) + (left ? 3 : 1)) % 4;
      return neighbor.getValue(rotation()).intValue() == expected;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      BlockState state = super.getStateForPlacement(ctx);
      if (state == null) {
         return null;
      }
      LevelReader level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      BlockState behind = level.getBlockState(pos.relative(behindOf(state.getValue(rotation()))));
      return state
              .setValue(CORNER_LEFT, corner(state, behind, true))
              .setValue(CORNER_RIGHT, corner(state, behind, false));
   }

   @Override
   public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor,
                                 LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      BlockState updated = super.updateShape(state, dir, neighbor, level, pos, neighborPos);
      if (dir == behindOf(updated.getValue(rotation()))) {
         return updated
                 .setValue(CORNER_LEFT, corner(updated, neighbor, true))
                 .setValue(CORNER_RIGHT, corner(updated, neighbor, false));
      }
      return updated;
   }
}
