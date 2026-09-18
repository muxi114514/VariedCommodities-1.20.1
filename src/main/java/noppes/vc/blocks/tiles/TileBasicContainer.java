package noppes.vc.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

/**
 * 带物品栏的方块实体基类（箱子、木桶、基座、武器架）。
 *
 * <p>NBT 沿用 1.12 的 {@code "Items"} + 每项 {@code "Slot"} 字节布局，
 * {@link ContainerHelper} 读写的正是这个格式，因此存档结构与原作一致。
 *
 * <p>与 1.12 的三点差异：
 * <ul>
 *   <li><b>槽位数由构造参数传入</b>，不再像原作那样在构造函数里虚调 {@code getSizeInventory()}
 *       ——那是个隐患：父类构造期调用被子类覆写的方法，子类字段尚未初始化。</li>
 *   <li><b>修掉 {@code removeStackFromSlot} 的原作 bug。</b>原作写的是
 *       {@code chestContents.remove(par1)}，这会把元素<i>删除</i>导致后续槽位整体前移、
 *       容量缩水，而不是把该槽置空。正确做法是 {@link ContainerHelper#takeItem}。</li>
 *   <li>额外暴露 Forge 的 {@code ITEM_HANDLER} 能力，让管道/漏斗类模组能接上。
 *       原版漏斗认 {@link Container} 本身，这条是给模组用的。</li>
 * </ul>
 */
public abstract class TileBasicContainer extends BlockEntity implements Container {

   private final NonNullList<ItemStack> items;
   private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new InvWrapper(this));

   protected TileBasicContainer(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
      super(type, pos, state);
      this.items = NonNullList.withSize(size, ItemStack.EMPTY);
   }

   protected NonNullList<ItemStack> items() {
      return this.items;
   }

   /**
    * 是否把物品同步到客户端。
    * 只有需要在世界里把内容物画出来的（基座、武器架、置物架）才要，
    * 箱子类不同步——54 格内容每次更新都广播纯属浪费带宽，原作也是这么分的
    * （{@code TileBasicRotation#getUpdateTag} 显式 removeTag("Items")）。
    */
   protected boolean syncItems() {
      return false;
   }

   // ── NBT ──────────────────────────────────────────────────────────────

   @Override
   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      ContainerHelper.saveAllItems(tag, this.items);
   }

   @Override
   public void load(CompoundTag tag) {
      super.load(tag);
      this.items.clear();
      ContainerHelper.loadAllItems(tag, this.items);
   }

   @Override
   public CompoundTag getUpdateTag() {
      CompoundTag tag = new CompoundTag();
      if (syncItems()) {
         ContainerHelper.saveAllItems(tag, this.items);
      }
      return tag;
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   // ── Container ────────────────────────────────────────────────────────

   @Override
   public int getContainerSize() {
      return this.items.size();
   }

   @Override
   public boolean isEmpty() {
      for (ItemStack stack : this.items) {
         if (!stack.isEmpty()) {
            return false;
         }
      }
      return true;
   }

   @Override
   public ItemStack getItem(int slot) {
      return this.items.get(slot);
   }

   @Override
   public ItemStack removeItem(int slot, int count) {
      ItemStack taken = ContainerHelper.removeItem(this.items, slot, count);
      if (!taken.isEmpty()) {
         setChanged();
      }
      return taken;
   }

   @Override
   public ItemStack removeItemNoUpdate(int slot) {
      return ContainerHelper.takeItem(this.items, slot);
   }

   @Override
   public void setItem(int slot, ItemStack stack) {
      this.items.set(slot, stack);
      if (stack.getCount() > getMaxStackSize()) {
         stack.setCount(getMaxStackSize());
      }
      setChanged();
   }

   @Override
   public boolean stillValid(Player player) {
      return Container.stillValidBlockEntity(this, player);
   }

   @Override
   public void clearContent() {
      this.items.clear();
   }

   // ── Forge 能力 ───────────────────────────────────────────────────────

   @Override
   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      if (!isRemoved() && cap == ForgeCapabilities.ITEM_HANDLER) {
         return this.itemHandler.cast();
      }
      return super.getCapability(cap, side);
   }

   @Override
   public void invalidateCaps() {
      super.invalidateCaps();
      this.itemHandler.invalidate();
   }
}
