package noppes.vc.containers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import noppes.vc.blocks.storage.StorageTier;
import noppes.vc.init.VCMenus;

/**
 * 任意 列×行 的箱子菜单，给超过 54 格的升级板条箱用——原版 MenuType 最大只有 9×6。
 *
 * <p>与原版 {@code ChestMenu} 同构：箱子格在前、玩家背包与快捷栏在后，Shift 点击在两段之间互搬。
 * 槽位坐标全部来自 {@link StorageLayout}，与界面背景共用一份几何。
 */
public class StorageMenu extends AbstractContainerMenu {

   private final Container container;
   private final StorageTier tier;
   private final int containerSlots;

   /**
    * 客户端由 MenuType 调用：档位随开界面的包下发，本地建一个同尺寸的空壳容器，
    * 内容由原版的槽位同步逐格填进来。
    */
   public static StorageMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buf) {
      StorageTier tier = StorageTier.byId(buf.readByte());
      return new StorageMenu(containerId, inventory, new SimpleContainer(tier.size()), tier);
   }

   public StorageMenu(int containerId, Inventory inventory, Container container, StorageTier tier) {
      super(VCMenus.STORAGE.get(), containerId);
      checkContainerSize(container, tier.size());
      this.container = container;
      this.tier = tier;
      this.containerSlots = tier.size();
      container.startOpen(inventory.player);

      StorageLayout layout = StorageLayout.of(tier);
      int slot = StorageLayout.SLOT;
      for (int row = 0; row < tier.rows(); row++) {
         for (int col = 0; col < tier.columns(); col++) {
            addSlot(new Slot(container, col + row * tier.columns(),
                    layout.gridX() + col * slot, layout.gridY() + row * slot));
         }
      }
      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col + row * 9 + 9,
                    layout.inventoryX() + col * slot, layout.inventoryY() + row * slot));
         }
      }
      for (int col = 0; col < 9; col++) {
         addSlot(new Slot(inventory, col, layout.inventoryX() + col * slot, layout.hotbarY()));
      }
   }

   public StorageTier tier() {
      return this.tier;
   }

   @Override
   public boolean stillValid(Player player) {
      return this.container.stillValid(player);
   }

   /**
    * Shift 点击：箱子 → 背包（从快捷栏末尾倒着填，同原版箱子）；背包 → 箱子。
    * 末尾的数量比较是"这次什么都没搬动"的标记，缺了它调用方会反复调用，见 CarpentryMenu 同处说明。
    */
   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      Slot slot = this.slots.get(index);
      if (!slot.hasItem()) {
         return ItemStack.EMPTY;
      }
      ItemStack stack = slot.getItem();
      ItemStack result = stack.copy();

      if (index < this.containerSlots) {
         if (!moveItemStackTo(stack, this.containerSlots, this.slots.size(), true)) {
            return ItemStack.EMPTY;
         }
      } else if (!moveItemStackTo(stack, 0, this.containerSlots, false)) {
         return ItemStack.EMPTY;
      }

      if (stack.isEmpty()) {
         slot.setByPlayer(ItemStack.EMPTY);
      } else {
         slot.setChanged();
      }
      if (stack.getCount() == result.getCount()) {
         return ItemStack.EMPTY;
      }
      slot.onTake(player, stack);
      return result;
   }

   @Override
   public void removed(Player player) {
      super.removed(player);
      this.container.stopOpen(player);
   }
}
