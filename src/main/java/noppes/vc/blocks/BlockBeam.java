package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 横梁。四个朝向各有一套碰撞箱，数值逐字取自 1.12 的 AABB0~AABB3。
 *
 * 原作在 {@code getBoundingBox} 里每次都 {@code world.getTileEntity(pos)} 取朝向，
 * 而碰撞箱查询位于每 tick 每实体的热路径上；朝向搬进 blockstate 后这里变成纯状态读取，
 * 连带也不用再担心方块实体尚未加载时拿到 null 而回退成整方块碰撞箱（原作的既有缺陷）。
 */
public class BlockBeam extends BlockBasicRotated {

   private static final VoxelShape[] SHAPES = {
           Block.box(5.28D, 5.28D, 4.0D, 10.72D, 10.72D, 16.0D),   // rotation 0
           Block.box(0.0D, 5.28D, 5.28D, 12.0D, 10.72D, 10.72D),   // rotation 1
           Block.box(5.28D, 5.28D, 0.0D, 10.72D, 10.72D, 12.0D),   // rotation 2
           Block.box(4.0D, 5.28D, 5.28D, 16.0D, 10.72D, 10.72D),   // rotation 3
   };

   public BlockBeam(Properties props) {
      super(props);
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPES[state.getValue(rotation()) & 3];
   }
}
