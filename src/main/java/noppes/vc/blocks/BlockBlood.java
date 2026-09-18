package noppes.vc.blocks;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 血迹：贴在相邻实心面上的装饰，无碰撞。
 *
 * <p>六个 {@code hide_*} 标志表示"该面不画血迹"，语义与命名都沿用原作字段
 * （{@code hideTop} 等）。放置时按邻面是否实心一次性算出：没有实心邻面就不画，
 * 若六面全被判为隐藏则强制显示底面，否则会得到一个完全看不见的方块——这条兜底也来自原作。
 *
 * <p>不需要方块实体：原作的 {@code TileBlood} 只有这六个布尔加一个朝向，全部适合放进 blockstate。
 * 六布尔 × 四朝向 = 每个方块 256 个状态，对 BlockState 池来说微不足道。
 */
public class BlockBlood extends BlockBasicRotated {

   private static final VoxelShape SHAPE = Block.box(0.16D, 0.16D, 0.16D, 15.84D, 15.84D, 15.84D);

   private static final Map<Direction, BooleanProperty> HIDE = hideProperties();

   public BlockBlood(Properties props) {
      super(props);
      BlockState state = defaultBlockState();
      for (BooleanProperty property : HIDE.values()) {
         state = state.setValue(property, true);
      }
      registerDefaultState(state);
   }

   private static Map<Direction, BooleanProperty> hideProperties() {
      EnumMap<Direction, BooleanProperty> map = new EnumMap<>(Direction.class);
      for (Direction dir : Direction.values()) {
         map.put(dir, BooleanProperty.create("hide_" + dir.getSerializedName()));
      }
      return map;
   }

   public static BooleanProperty hide(Direction dir) {
      return HIDE.get(dir);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      for (BooleanProperty property : HIDE.values()) {
         builder.add(property);
      }
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPE;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      BlockState state = super.getStateForPlacement(ctx);
      if (state == null) {
         return null;
      }
      LevelReader level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();

      boolean allHidden = true;
      for (Direction dir : Direction.values()) {
         BlockPos side = pos.relative(dir);
         // 邻面实心才画血迹；对应原作 hideX = !world.isSideSolid(...)
         boolean hidden = !level.getBlockState(side).isFaceSturdy(level, side, dir.getOpposite());
         state = state.setValue(HIDE.get(dir), hidden);
         allHidden &= hidden;
      }
      // 六面全隐藏会得到一个完全看不见的方块，兜底显示底面（原作同款）
      return allHidden ? state.setValue(HIDE.get(Direction.DOWN), false) : state;
   }
}
