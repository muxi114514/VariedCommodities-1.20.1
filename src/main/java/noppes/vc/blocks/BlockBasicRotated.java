package noppes.vc.blocks;

import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * 可旋转家具基类。
 *
 * <p><b>与 1.12 的关键差异：旋转从方块实体搬进了方块状态。</b>
 * 原作把朝向存在 {@code TileBasicRotation.rotation} 里，是因为 metadata 那 4 个 bit
 * 已经被木材/材质变体占满了，没地方放朝向。1.20.1 没有 metadata，变体已拆成独立方块，
 * 这 4 个 bit 的历史包袱随之消失，朝向理应回到 blockstate。这么改有三个实打实的好处：
 * <ul>
 *   <li>碰撞箱变成纯状态读取。原作 {@code BlockBeam#getBoundingBox} 每次碰撞检测都要
 *       {@code world.getTileEntity(pos)} 查一次方块实体，而碰撞检测是每 tick 每实体多次调用的热路径。</li>
 *   <li>静态家具可以直接用 JSON 模型 + blockstate 的 y 轴旋转渲染，不必为"仅仅是转了个向"而挂 BER。</li>
 *   <li>结构方块/克隆/piston 推动等原版旋转机制自动生效（见 {@link #rotate} / {@link #mirror}）。</li>
 * </ul>
 *
 * <p>旋转档数沿用 1.12 的 {@code maxRotation()}：默认 4 档，桶/篝火/蜡烛/台灯是 8 档。
 */
public class BlockBasicRotated extends Block {

   public static final IntegerProperty ROTATION_4 = IntegerProperty.create("rotation", 0, 3);
   public static final IntegerProperty ROTATION_8 = IntegerProperty.create("rotation", 0, 7);

   public BlockBasicRotated(Properties props) {
      super(props);
      registerDefaultState(stateDefinition.any().setValue(rotation(), 0));
   }

   /**
    * 旋转档数，必须是 4 或 8（都要求是 2 的幂，下面按位与取模依赖这点）。
    *
    * <p><b>覆写时只能返回字面常量。</b>父类 Block 的构造函数里就会虚调到
    * {@link #createBlockStateDefinition}，进而调到本方法，此刻子类字段尚未初始化——
    * 若在这里读子类字段，拿到的是 0。这也是 1.12 原作把它写成方法而非字段的原因。
    */
   protected int maxRotation() {
      return 4;
   }

   public final IntegerProperty rotation() {
      return maxRotation() == 8 ? ROTATION_8 : ROTATION_4;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(rotation());
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return defaultBlockState().setValue(rotation(), yawToRotation(ctx.getRotation()));
   }

   /** 1.12 BlockBasicRotated#onBlockPlacedBy 的取整公式，逐字保留。 */
   protected final int yawToRotation(float yaw) {
      int max = maxRotation();
      return Mth.floor((double) (yaw * (float) max / 360.0F) + 0.5D) & (max - 1);
   }

   @Override
   public BlockState rotate(BlockState state, Rotation rot) {
      return state.setValue(rotation(), rot.rotate(state.getValue(rotation()), maxRotation()));
   }

   @Override
   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.setValue(rotation(), mirror.mirror(state.getValue(rotation()), maxRotation()));
   }
}
