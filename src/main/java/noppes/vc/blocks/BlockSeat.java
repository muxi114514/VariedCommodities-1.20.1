package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import noppes.vc.entity.EntityChairMount;

/**
 * 能坐的家具（椅子、凳子，沙发经 {@link BlockCouch} 继承）。
 *
 * 形状由构造参数给定——椅子和凳子除了高度之外完全一样，不值得各建一个类。
 * 这几个方块都不需要方块实体：原作的 {@code TileChair}/{@code TileStool} 是空子类，
 * 只为给 TESR 认木材，而木材现在就是方块身份。
 */
public class BlockSeat extends BlockBasicRotated {

   /** 椅子：原作 AABB (0.1,0,0.1)-(0.9,0.5,0.9)。 */
   public static final VoxelShape CHAIR = Block.box(1.6D, 0.0D, 1.6D, 14.4D, 8.0D, 14.4D);
   /** 凳子：原作 AABB_NORMAL，比椅子高一点。 */
   public static final VoxelShape STOOL = Block.box(1.6D, 0.0D, 1.6D, 14.4D, 9.6D, 14.4D);

   private final VoxelShape shape;

   public BlockSeat(Properties props, VoxelShape shape) {
      super(props);
      this.shape = shape;
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return this.shape;
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
      return EntityChairMount.sit(level, pos, player);
   }
}
