package noppes.vc.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import noppes.vc.init.VCBlockEntities;

/**
 * 书台：单格，放一本书。
 *
 * 复用 {@link TileBasicContainer}——原作 {@code TileBook} 自己写了一遍单物品的读写，
 * 而这里的物品栏基类已经把 NBT、同步、掉落、能力全办好了。
 * 内容要在世界里画出来（P5 的 BER 会用），故 {@link #syncItems()} 为真。
 */
public class TileBook extends TileBasicContainer {

   public TileBook(BlockPos pos, BlockState state) {
      super(VCBlockEntities.BOOK.get(), pos, state, 1);
   }

   @Override
   protected boolean syncItems() {
      return true;
   }

   /** 只收书。原作放置的是一本空的可写书，这里同样只接受书类物品。 */
   @Override
   public boolean canPlaceItem(int slot, ItemStack stack) {
      return stack.is(Items.WRITABLE_BOOK) || stack.is(Items.WRITTEN_BOOK) || stack.is(Items.BOOK);
   }
}
