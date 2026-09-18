package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 置物架：贴墙的架子，四个朝向各一套形状（取自原作 AABB0~AABB3）。
 *
 * 不需要方块实体——原作的 {@code TileShelf} 是不带任何字段的空类，
 * 五个嵌套子类只为让 TESR 认木材。
 */
public class BlockShelf extends BlockBasicRotated {

   private static final VoxelShape[] SHAPES = {
           Block.box(0.0D, 7.04D, 4.8D, 16.0D, 16.0D, 16.0D),   // 朝向 0：南
           Block.box(0.0D, 7.04D, 0.0D, 11.2D, 16.0D, 16.0D),   // 朝向 1：西
           Block.box(0.0D, 7.04D, 0.0D, 16.0D, 16.0D, 11.2D),   // 朝向 2：北
           Block.box(4.8D, 7.04D, 0.0D, 16.0D, 16.0D, 16.0D),   // 朝向 3：东
   };

   public BlockShelf(Properties props) {
      super(props);
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPES[state.getValue(rotation()) & 3];
   }
}
