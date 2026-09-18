package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import noppes.vc.init.VCTags;

/**
 * 桌子。相邻的桌子会隐藏彼此之间的桌腿，拼起来是一张完整的大桌。
 *
 * <p>这个行为原作藏在渲染器里：{@code BlockTableRenderer} 每帧读四邻并设
 * {@code Shape1.showModel = !south && !east}。搬到 1.20.1 后改由四个方向布尔表达，
 * 桌腿的显隐交给 blockstate multipart，于是：渲染端不再需要每帧查四次邻居方块，
 * 模型也能被原版的模型烘焙缓存复用。
 *
 * <p>连接判定走 {@link VCTags#TABLES} 标签而非 {@code instanceof}：1.12 里六种木材本是
 * 同一个方块的不同 meta，本来就互相连接；用标签既保住这一点，也让附属模组能加入自己的桌子。
 */
public class BlockTable extends BlockBasicRotated {

   public BlockTable(Properties props) {
      super(props);
      BlockState state = stateDefinition.any().setValue(rotation(), 0);
      for (Direction dir : Direction.Plane.HORIZONTAL) {
         state = state.setValue(connection(dir), false);
      }
      registerDefaultState(state);
   }

   private static BooleanProperty connection(Direction dir) {
      return PipeBlock.PROPERTY_BY_DIRECTION.get(dir);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      for (Direction dir : Direction.Plane.HORIZONTAL) {
         builder.add(connection(dir));
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      BlockState state = super.getStateForPlacement(ctx);
      LevelReader level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      for (Direction dir : Direction.Plane.HORIZONTAL) {
         // 放置时四邻所在区块必然已加载（玩家就在现场），与原版栅栏/玻璃板同一写法
         state = state.setValue(connection(dir), level.getBlockState(pos.relative(dir)).is(VCTags.TABLES));
      }
      return state;
   }

   /** 邻居变化时只读传进来的那一个状态，不额外访问世界，天然没有跨区块加载风险。 */
   @Override
   public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor,
                                 LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if (dir.getAxis().isHorizontal()) {
         return state.setValue(connection(dir), neighbor.is(VCTags.TABLES));
      }
      return state;
   }

   @Override
   public BlockState rotate(BlockState state, Rotation rot) {
      BlockState out = super.rotate(state, rot);
      for (Direction dir : Direction.Plane.HORIZONTAL) {
         out = out.setValue(connection(rot.rotate(dir)), state.getValue(connection(dir)));
      }
      return out;
   }

   @Override
   public BlockState mirror(BlockState state, Mirror mirror) {
      BlockState out = super.mirror(state, mirror);
      for (Direction dir : Direction.Plane.HORIZONTAL) {
         out = out.setValue(connection(mirror.mirror(dir)), state.getValue(connection(dir)));
      }
      return out;
   }
}
