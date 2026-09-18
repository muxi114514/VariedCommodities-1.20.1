package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * 可染色家具的共用部件（横幅、壁挂横幅、高脚灯、羊毛沙发）。
 *
 * <p><b>颜色放进 blockstate 而非方块实体。</b>1.12 把它存在
 * {@code TileBasicRotation.color} 里，是因为 metadata 已被材质/木材变体占满。
 * 现在变体是独立方块、朝向也已进 blockstate，颜色没有理由再占一个方块实体：
 * 高脚灯除颜色外没有任何数据，颜色一搬走它就<b>完全不需要方块实体</b>；
 * 而且渲染时颜色可直接从方块状态读出，不必在每个面的着色回调里查一次方块实体。
 *
 * <p>做成工具类而不是再加一层基类：可染色与"双格/红石源/带物品栏"是正交的，
 * 塞进继承链会立刻撞上 Java 单继承。
 */
public final class VCDye {

   public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

   private VCDye() {
   }

   /** 手上拿的是不是染料；是则返回其颜色，否则 null。 */
   public static DyeColor heldDye(Player player, InteractionHand hand) {
      return player.getItemInHand(hand).getItem() instanceof DyeItem dye ? dye.getDyeColor() : null;
   }

   /** 消耗一个染料。原作 {@code CommonUtils#ConsumeItemStack} 同样跳过创造模式。 */
   public static void consume(Player player, InteractionHand hand) {
      if (!player.getAbilities().instabuild) {
         player.getItemInHand(hand).shrink(1);
      }
   }

   /**
    * 给该位置的方块改色；位置上不是目标方块时什么也不做。
    * 双格方块要对上下两格各调一次——这是"颜色放 blockstate"相对"放方块实体"的唯一额外开销。
    */
   public static void recolor(Level level, BlockPos pos, Block block, DyeColor color) {
      BlockState state = level.getBlockState(pos);
      if (state.is(block)) {
         level.setBlock(pos, state.setValue(COLOR, color), Block.UPDATE_ALL);
      }
   }
}
