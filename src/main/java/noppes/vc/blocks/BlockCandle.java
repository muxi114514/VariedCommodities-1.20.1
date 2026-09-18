package noppes.vc.blocks;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 蜡烛。三种安装面各有形状，点燃时冒火焰与烟。
 *
 * 天花板上是吊灯造型，四角各一簇火焰；贴墙的单簇火焰要朝墙面偏移；地面居中一簇。
 * 偏移量逐字取自原作 {@code randomDisplayTick}。
 */
public class BlockCandle extends BlockBasicLightable {

   private static final VoxelShape FLOOR = Block.box(4.8D, 0.0D, 4.8D, 11.2D, 8.0D, 11.2D);
   private static final VoxelShape CEILING = Block.box(1.6D, 1.6D, 1.6D, 14.4D, 12.8D, 14.4D);
   private static final VoxelShape WALL_DEFAULT = Block.box(3.2D, 6.4D, 3.2D, 12.8D, 14.4D, 12.8D);
   private static final VoxelShape[] WALL = {
           Block.box(3.2D, 6.4D, 6.4D, 12.8D, 14.4D, 16.0D),   // 档 0（北面）
           WALL_DEFAULT,
           Block.box(0.0D, 6.4D, 3.2D, 9.6D, 14.4D, 12.8D),    // 档 2（东面）
           WALL_DEFAULT,
           Block.box(3.2D, 6.4D, 0.0D, 12.8D, 14.4D, 9.6D),    // 档 4（南面）
           WALL_DEFAULT,
           Block.box(6.4D, 6.4D, 3.2D, 16.0D, 14.4D, 12.8D),   // 档 6（西面）
           WALL_DEFAULT,
   };

   /** 天花板吊灯的四簇火焰：朝向为偶数档时贴四边中点，奇数档时贴四角。 */
   private static final double[][] CEILING_EVEN = {{0.5D, 0.13D}, {0.5D, 0.87D}, {0.13D, 0.5D}, {0.87D, 0.5D}};
   private static final double[][] CEILING_ODD = {{0.24D, 0.24D}, {0.76D, 0.76D}, {0.24D, 0.76D}, {0.76D, 0.24D}};

   public BlockCandle(Properties props, boolean lit, Supplier<Block> counterpart) {
      super(props, lit, counterpart, FLOOR, CEILING, WALL);
   }

   @Override
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (!isLit()) {
         return;
      }
      AttachFace face = state.getValue(FACE);
      int rot = state.getValue(rotation());

      if (face == AttachFace.CEILING) {
         double[][] spots = rot % 2 == 0 ? CEILING_EVEN : CEILING_ODD;
         for (double[] spot : spots) {
            flame(level, pos.getX() + spot[0], pos.getY() + 0.65D, pos.getZ() + spot[1]);
         }
         return;
      }

      double x = 0.5D;
      double y = 0.45D;
      double z = 0.5D;
      if (face == AttachFace.WALL) {
         y = 1.05D;
         switch (rot) {
            case 0 -> z += 0.12D;
            case 4 -> z -= 0.12D;
            case 6 -> x += 0.12D;
            case 2 -> x -= 0.12D;
            default -> {
            }
         }
      }
      flame(level, pos.getX() + x, pos.getY() + y, pos.getZ() + z);
   }

   /** 烟比火焰高 0.01，沿用原作的错位。 */
   private static void flame(Level level, double x, double y, double z) {
      level.addParticle(ParticleTypes.SMOKE, x, y + 0.01D, z, 0.0D, 0.0D, 0.0D);
      level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
   }
}
