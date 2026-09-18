package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 高脚灯：两格高，可用染料改色，恒亮。
 *
 * <p><b>没有方块实体。</b>1.12 的 {@code TileTallLamp} 除了继承来的 {@code color} 与
 * {@code rotation} 之外没有任何字段——朝向已进 blockstate（P3），颜色也进 blockstate（见 {@link VCDye}），
 * 于是这个方块实体就整个消失了。原作那五个 {@code TileTallLamp1..4} 空子类纯粹是给 TESR 认材质用的。
 *
 * <p>初始颜色按材质档位取 {@code DyeColor.byId(15 - 材质序号)}，逐字沿用原作
 * （{@code tile.color = 15 - stack.getItemDamage()}）。这不是随手写的：
 * 物品图标的着色用的是同一个式子（{@code setColor} 在 tile 为 null 时走 {@code c = 15 - meta}），
 * 所以放下去的灯与手里那个颜色一致。
 */
public class BlockTallLamp extends BlockBasicDouble {

   private final DyeColor defaultColor;

   public BlockTallLamp(Properties props, DyeColor defaultColor) {
      super(props);
      this.defaultColor = defaultColor;
      registerDefaultState(defaultBlockState().setValue(VCDye.COLOR, defaultColor));
   }

   /** 物品图标着色用（P5 的 ItemColor 回调会取它）。 */
   public DyeColor defaultColor() {
      return this.defaultColor;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(VCDye.COLOR);
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
      DyeColor dye = VCDye.heldDye(player, hand);
      if (dye == null) {
         return InteractionResult.PASS;
      }
      if (state.getValue(VCDye.COLOR) == dye) {
         return InteractionResult.CONSUME;   // 同色不重复消耗染料，与原作一致
      }
      if (!level.isClientSide) {
         // 颜色在 blockstate 里，上下两格都要改——这是相对"颜色存方块实体"的唯一额外开销
         BlockPos base = baseOf(pos, state);
         VCDye.recolor(level, base, this, dye);
         VCDye.recolor(level, base.above(), this, dye);
         VCDye.consume(player, hand);
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
   }

}
