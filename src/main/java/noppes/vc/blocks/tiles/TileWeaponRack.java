package noppes.vc.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import noppes.vc.blocks.IPowerProvider;
import noppes.vc.init.VCBlockEntities;

/**
 * 武器架：三个展示格，每格占用贡献 5 级红石信号（满三格 15）。
 *
 * 只挂在方块的下半格上，上半格没有方块实体（对应原作
 * {@code createNewTileEntity} 里 {@code meta < 7 ? new TileWeaponRack() : null} 的写法）。
 */
public class TileWeaponRack extends TileBasicContainer implements IPowerProvider {

   private static final int SLOTS = 3;
   private static final int POWER_PER_SLOT = 5;

   public TileWeaponRack(BlockPos pos, BlockState state) {
      super(VCBlockEntities.WEAPON_RACK.get(), pos, state, SLOTS);
   }

   /** 武器要在世界里画出来，必须同步给客户端。 */
   @Override
   protected boolean syncItems() {
      return true;
   }

   /** 不收方块——架子是挂武器的。原作在物品栏层面就做了这个限制（与基座不同）。 */
   @Override
   public boolean canPlaceItem(int slot, ItemStack stack) {
      return !(stack.getItem() instanceof BlockItem);
   }

   @Override
   public int powerProvided() {
      int power = 0;
      for (int slot = 0; slot < SLOTS; slot++) {
         if (!getItem(slot).isEmpty()) {
            power += POWER_PER_SLOT;
         }
      }
      return power;
   }
}
