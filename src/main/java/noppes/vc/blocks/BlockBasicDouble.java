package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * 两格高的家具基类（武器架、高脚灯、横幅）。
 *
 * <p>1.12 把"上半格"塞进 metadata（{@code meta = damage + (isTop ? 7 : 0)}），
 * 这里改用原版的 {@link DoubleBlockHalf}，与门/高草共用同一套机制。相应修掉原作两个缺陷：
 * <ul>
 *   <li><b>放置失败会吞物品</b>：原作 {@code onBlockPlacedBy} 发现上方非空气就
 *       {@code setBlockToAir(pos)}——此时物品已被消耗。这里改为 {@link #getStateForPlacement}
 *       返回 null，放置被干净拒绝，物品留在手里。</li>
 *   <li><b>拆除联动不全</b>：原作只在 {@code onBlockHarvested} 里拆另一半，
 *       爆炸、{@code /setblock}、活塞等路径都绕过它，会留下半截幽灵方块。
 *       这里用 {@link #updateShape}：另一半不在了自己就变空气，覆盖所有拆除路径。</li>
 * </ul>
 *
 * <p>战利品表必须按 {@code HALF=LOWER} 过滤（见 {@code VCLootTables}），否则拆一次掉两个。
 */
public abstract class BlockBasicDouble extends BlockBasicRotated {

   public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

   protected BlockBasicDouble(Properties props) {
      super(props);
      registerDefaultState(defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(HALF);
   }

   /** 取下半格的位置——方块实体、朝向、内容物都挂在那里。 */
   public static BlockPos baseOf(BlockPos pos, BlockState state) {
      return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      BlockPos pos = ctx.getClickedPos();
      Level level = ctx.getLevel();
      if (pos.getY() >= level.getMaxBuildHeight() - 1
              || !level.getBlockState(pos.above()).canBeReplaced(ctx)) {
         return null;
      }
      BlockState state = super.getStateForPlacement(ctx);
      return state == null ? null : state.setValue(HALF, DoubleBlockHalf.LOWER);
   }

   @Override
   public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
   }

   @Override
   public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor,
                                 LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      DoubleBlockHalf half = state.getValue(HALF);
      boolean towardsPartner = (half == DoubleBlockHalf.LOWER) == (dir == Direction.UP);
      if (dir.getAxis() == Direction.Axis.Y && towardsPartner) {
         return neighbor.is(this) && neighbor.getValue(HALF) != half
                 ? state
                 : Blocks.AIR.defaultBlockState();
      }
      return state;
   }

   /**
    * 玩家敲上半格时转成"敲下半格"处理：掉落与内容物都挂在下半格，
    * 交给原版 destroyBlock 走完整流程；上半格随后由 updateShape 自动变空气，不会重复掉落。
    */
   @Override
   public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (!level.isClientSide && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
         BlockPos below = pos.below();
         BlockState belowState = level.getBlockState(below);
         if (belowState.is(this) && belowState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            level.destroyBlock(below, !player.isCreative(), player);
         }
      }
      super.playerWillDestroy(level, pos, state, player);
   }
}
