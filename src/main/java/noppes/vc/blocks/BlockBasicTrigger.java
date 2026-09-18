package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 单格的红石源家具（基座）。
 *
 * 信号值来自方块实体，但这里只依赖 {@link IPowerProvider} 抽象，不认具体实现类。
 * 双格的红石源（武器架）继承 {@link BlockBasicDouble}，另行调用 {@link IPowerProvider} 的静态助手。
 */
public abstract class BlockBasicTrigger extends BlockBasicRotated {

   protected BlockBasicTrigger(Properties props) {
      super(props);
   }

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
}
