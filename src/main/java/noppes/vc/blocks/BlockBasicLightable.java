package noppes.vc.blocks;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 可点燃/熄灭的光源家具（蜡烛、台灯、篝火）基类。
 *
 * <p>点燃态与熄灭态是<b>两个独立注册项</b>（原作即如此，契约里 {@code candle} / {@code candle_unlit}
 * 各占一项），右键在两者之间切换。因此这里<b>不保留 1.12 的 {@code LIT} 布尔属性</b>——
 * 方块身份本身已经表达了点燃与否，再挂一个 LIT 属性只会让每个方块凭空多出一倍永远用不到的状态。
 * 亮度烧进各自的 Properties（点燃态 15，熄灭态 0）。
 *
 * <p>安装面沿用原作语义但换了载体：1.12 把它塞在 {@code TileBasicRotation.color} 字段里
 * （0=地面 1=天花板 2=墙面，和染色共用同一个字段），这里换成原版的 {@link AttachFace}，
 * 与按钮/拉杆一致，也把"颜色"字段还给真正需要染色的方块。这三个方块因此<b>都不需要方块实体</b>。
 *
 * <p>形状由构造参数注入：蜡烛与台灯的选形逻辑结构完全相同（贴墙时按朝向选、其余用固定形状），
 * 只是数值不同，没必要各写一遍；台灯更是除此之外毫无特殊行为，直接用本类即可。
 */
public class BlockBasicLightable extends BlockBasicRotated {

   public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

   private final boolean lit;
   private final Supplier<Block> counterpart;
   private final VoxelShape floorShape;
   private final VoxelShape ceilingShape;
   /** 贴墙时按 8 档朝向索引；原作只用到 0/2/4/6 四个偶数档，其余档落到 index 的默认形状。 */
   private final VoxelShape[] wallShapes;

   public BlockBasicLightable(Properties props, boolean lit, Supplier<Block> counterpart,
                              VoxelShape floorShape, VoxelShape ceilingShape, VoxelShape[] wallShapes) {
      super(props);
      this.lit = lit;
      this.counterpart = counterpart;
      this.floorShape = floorShape;
      this.ceilingShape = ceilingShape;
      this.wallShapes = wallShapes;
      registerDefaultState(defaultBlockState().setValue(FACE, AttachFace.FLOOR));
   }

   /** 蜡烛/台灯/篝火都是 8 档细分旋转。 */
   @Override
   protected int maxRotation() {
      return 8;
   }

   /** 本方块是点燃态还是熄灭态。对应原作 {@code BlockCandle(boolean lit)} 的构造参数。 */
   public boolean isLit() {
      return this.lit;
   }

   /** 右键要切换到的另一半。注册期两者互相引用，故用 Supplier 延迟取值。 */
   public Block counterpart() {
      return this.counterpart.get();
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(FACE);
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return switch (state.getValue(FACE)) {
         case WALL -> this.wallShapes[state.getValue(rotation()) & 7];
         case CEILING -> this.ceilingShape;
         default -> this.floorShape;
      };
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      Direction face = ctx.getClickedFace();
      BlockState base = defaultBlockState();
      if (face == Direction.UP) {
         return base.setValue(FACE, AttachFace.FLOOR).setValue(rotation(), yawToRotation(ctx.getRotation()));
      }
      if (face == Direction.DOWN) {
         return base.setValue(FACE, AttachFace.CEILING).setValue(rotation(), yawToRotation(ctx.getRotation()));
      }
      // 贴墙时朝向由墙面决定而非玩家视角，档位取 8 档中的偶数位（原作 onPostBlockPlaced）
      return base.setValue(FACE, AttachFace.WALL).setValue(rotation(), wallRotation(face));
   }

   private static int wallRotation(Direction face) {
      return switch (face) {
         case NORTH -> 0;
         case EAST -> 2;
         case SOUTH -> 4;
         case WEST -> 6;
         default -> 0;
      };
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
      if (!level.isClientSide) {
         toggle(state, level, pos);
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   /** 切换点燃态，保留朝向与安装面。两态的 maxRotation 相同，故可共用同一个属性对象。 */
   protected void toggle(BlockState state, Level level, BlockPos pos) {
      BlockState next = counterpart().defaultBlockState()
              .setValue(rotation(), state.getValue(rotation()))
              .setValue(FACE, state.getValue(FACE));
      level.setBlock(pos, next, Block.UPDATE_ALL);
   }
}
