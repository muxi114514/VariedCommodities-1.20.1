package noppes.vc.blocks.tiles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import noppes.vc.blocks.BlockCrate;
import noppes.vc.blocks.storage.StorageTier;
import noppes.vc.containers.StorageMenu;
import noppes.vc.init.VCBlockEntities;

/**
 * 箱子类容器的方块实体，槽位数随方块的 {@link StorageTier} 而定。
 *
 * <p><b>木桶与箱子共用本类。</b>原作 {@code TileCrate} 与 {@code TileBarrel} 的唯一差别是
 * {@code getName()} 返回不同的 lang 键；而 1.20.1 的界面标题直接取自方块的 descriptionId，
 * 这点差别自然消失，没必要留两个一模一样的类。
 *
 * <p>54 格的木质档仍用 {@link ChestMenu#sixRows}（即 {@code MenuType.GENERIC_9x6}）：
 * 原作的 {@code GuiCrate} 绑的就是原版贴图 {@code generic_54.png}，逐行抄的原版双箱界面。
 * 原版 MenuType 最大只到 9×6，更大的升级档位才走自建的 {@link StorageMenu}。
 */
public class TileCrate extends TileBasicContainer implements MenuProvider {

   private final StorageTier tier;

   public TileCrate(BlockPos pos, BlockState state) {
      this(pos, state, tierOf(state));
   }

   private TileCrate(BlockPos pos, BlockState state, StorageTier tier) {
      super(VCBlockEntities.CRATE.get(), pos, state, tier.size());
      this.tier = tier;
   }

   /** 构造期 state 已确定，按方块取档位是安全的（不涉及虚调子类方法）。 */
   private static StorageTier tierOf(BlockState state) {
      return state.getBlock() instanceof BlockCrate crate ? crate.tier() : StorageTier.WOOD;
   }

   /** 标题取方块自己的译名，箱子与木桶因此各显其名。 */
   @Override
   public Component getDisplayName() {
      return Component.translatable(getBlockState().getBlock().getDescriptionId());
   }

   /** 自建菜单的客户端要先知道档位才能摆出同样多的槽位，故随开界面的包一起下发。 */
   public void openFor(ServerPlayer player) {
      if (this.tier.usesVanillaMenu()) {
         player.openMenu(this);
      } else {
         NetworkHooks.openScreen(player, this, buf -> buf.writeByte(this.tier.ordinal()));
      }
   }

   @Override
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
      return this.tier.usesVanillaMenu()
              ? ChestMenu.sixRows(containerId, playerInventory, this)
              : new StorageMenu(containerId, playerInventory, this, this.tier);
   }

   // ── 升级搬运 ─────────────────────────────────────────────────────────

   /**
    * 摘出全部内容并清空自身。必须在替换方块<b>之前</b>调用：
    * 否则 {@code BlockCrate#onRemove} 会把内容物全撒到地上。
    */
   public List<ItemStack> takeAll() {
      List<ItemStack> taken = new ArrayList<>(items());
      clearContent();
      return taken;
   }

   /**
    * 按原槽位写回，返回放不下的部分交给调用方处理。
    * 升级只会变大，正常不会有剩余；留这条出口是为了永远不吞物品。
    */
   public List<ItemStack> restore(List<ItemStack> contents) {
      List<ItemStack> overflow = new ArrayList<>();
      for (int i = 0; i < contents.size(); i++) {
         ItemStack stack = contents.get(i);
         if (stack.isEmpty()) {
            continue;
         }
         if (i < getContainerSize()) {
            items().set(i, stack);
         } else {
            overflow.add(stack);
         }
      }
      setChanged();
      return overflow;
   }
}
