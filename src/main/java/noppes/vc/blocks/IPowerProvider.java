package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 能对外提供红石信号的方块实体。
 *
 * 1.12 的 {@code BlockBasicTrigger} 直接把方块实体强转成 {@code TileBasicRotation} 再调
 * {@code powerProvided()}，方块层因此硬依赖具体的方块实体类。这里抽成最小接口：
 * 方块层只认这个契约，具体由哪个方块实体实现、怎么算信号强度，都与方块无关。
 *
 * <p>配套的两个静态助手放在这里而非某个方块基类上，是因为 Java 单继承——
 * 武器架要同时具备"双格方块"与"红石源"两种行为，只能继承其一，另一边靠调用这些助手补上。
 */
public interface IPowerProvider {

   /** 0~15，0 表示不发出信号。 */
   int powerProvided();

   /** 读取该位置方块实体提供的信号；没有方块实体或它不提供信号时为 0。 */
   static int signalAt(BlockGetter level, BlockPos pos) {
      BlockEntity be = level.getBlockEntity(pos);
      return be instanceof IPowerProvider provider ? provider.powerProvided() : 0;
   }

   /**
    * 信号变化后通知六邻居（对应 1.12 的 {@code updateSurrounding}）。
    * 原作逐个 notify 了六个方向再加自身，1.20.1 的 {@code updateNeighborsAt} 已涵盖这一整套。
    */
   static void updateSurrounding(Level level, BlockPos pos, Block block) {
      level.updateNeighborsAt(pos, block);
      for (Direction dir : Direction.values()) {
         level.updateNeighborsAt(pos.relative(dir), block);
      }
   }
}
