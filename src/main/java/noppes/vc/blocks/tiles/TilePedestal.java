package noppes.vc.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import noppes.vc.blocks.IPowerProvider;
import noppes.vc.init.VCBlockEntities;

/**
 * 基座：单格，放一把剑并对外发满级红石信号。
 *
 * 不限制可插入的物品类型——原作的剑判定在方块的右键逻辑里，物品栏本身来者不拒
 * （漏斗塞别的东西进来也认），此处保持一致。
 */
public class TilePedestal extends TileBasicContainer implements IPowerProvider {

   public TilePedestal(BlockPos pos, BlockState state) {
      super(VCBlockEntities.PEDESTAL.get(), pos, state, 1);
   }

   /** 内容物要在世界里画出来，必须同步给客户端。 */
   @Override
   protected boolean syncItems() {
      return true;
   }

   @Override
   public int powerProvided() {
      return getItem(0).isEmpty() ? 0 : 15;
   }
}
