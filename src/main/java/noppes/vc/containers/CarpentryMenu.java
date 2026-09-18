package noppes.vc.containers;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import noppes.vc.blocks.BlockCarpentryBench;
import noppes.vc.init.VCMenus;
import noppes.vc.init.VCRecipes;

/**
 * 木工台菜单：4×4 合成格 + 一个产出格。
 *
 * <p>槽位坐标逐个取自原作 {@code ContainerCarpentryBench}，所以界面贴图 carpentry.png 能直接对上。
 *
 * <p><b>木工台是「4×4 的万能工作台」，不是独立配方池。</b>原作 {@code VCRecipes.match()} 遍历的是
 * <i>整个原版配方注册表</i>（{@code registry.getValues()}），只是把匹配窗口从 3×3 放宽到 4×4 偏移扫描；
 * 而 {@code RecipeContainer.add()} 造的又是原版 {@code ShapedRecipes} 并注册进原版表。
 * 所以在木工台上能做出原版的一切配方，VC 自己那 40 条不超过 3×3 的配方也能在普通工作台上做。
 * 这里据此两次查询：先查 4×4 专用池，没有再回退原版 {@link RecipeType#CRAFTING}。
 */
public class CarpentryMenu extends AbstractContainerMenu {

   public static final int GRID = 4;

   private static final int RESULT_SLOT = 0;
   /** 合成格首个槽位下标。JEI 的配方填充要按下标定位，故这三个常量对外可见。 */
   public static final int GRID_START = 1;
   public static final int INV_START = GRID_START + GRID * GRID;
   private static final int HOTBAR_START = INV_START + 27;
   public static final int HOTBAR_END = HOTBAR_START + 9;

   /** 合成格第 row 行第 col 列对应的槽位下标。 */
   public static int gridSlot(int row, int col) {
      return GRID_START + row * GRID + col;
   }

   private final CraftingContainer craftSlots = new TransientCraftingContainer(this, GRID, GRID);
   private final ResultContainer resultSlots = new ResultContainer();
   private final ContainerLevelAccess access;
   private final Player player;

   /** 客户端侧由 MenuType 调用，没有世界坐标。 */
   public CarpentryMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
      this(containerId, inventory, ContainerLevelAccess.NULL);
   }

   public CarpentryMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
      super(VCMenus.CARPENTRY.get(), containerId);
      this.access = access;
      this.player = inventory.player;

      addSlot(new ResultSlot(inventory.player, this.craftSlots, this.resultSlots, 0, 133, 41));
      for (int y = 0; y < GRID; y++) {
         for (int x = 0; x < GRID; x++) {
            addSlot(new Slot(this.craftSlots, x + y * GRID, 17 + x * 18, 14 + y * 18));
         }
      }
      for (int y = 0; y < 3; y++) {
         for (int x = 0; x < 9; x++) {
            addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 98 + y * 18));
         }
      }
      for (int x = 0; x < 9; x++) {
         addSlot(new Slot(inventory, x, 8 + x * 18, 156));
      }
   }

   @Override
   public void slotsChanged(net.minecraft.world.Container container) {
      this.access.execute(this::refreshResult);
   }

   private void refreshResult(Level level, BlockPos pos) {
      if (level.isClientSide || !(this.player instanceof ServerPlayer serverPlayer)) {
         return;
      }
      ItemStack result = ItemStack.EMPTY;
      // 先 4×4 专用池，再回退原版工作台配方——木工台是万能台，见类注释
      Optional<? extends Recipe<CraftingContainer>> recipe = level.getRecipeManager()
              .getRecipeFor(VCRecipes.CARPENTRY_TYPE.get(), this.craftSlots, level);
      if (recipe.isEmpty()) {
         recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, this.craftSlots, level);
      }
      // 三参重载会按 doLimitedCrafting 游戏规则判定玩家是否已解锁该配方，与原版工作台一致
      if (recipe.isPresent() && this.resultSlots.setRecipeUsed(level, serverPlayer, recipe.get())) {
         result = recipe.get().assemble(this.craftSlots, level.registryAccess());
      }
      this.resultSlots.setItem(0, result);
      setRemoteSlot(RESULT_SLOT, result);
      serverPlayer.connection.send(
              new ClientboundContainerSetSlotPacket(this.containerId, incrementStateId(), RESULT_SLOT, result));
   }

   /** 关闭界面时把合成格里的东西还给玩家，否则物品就永久留在临时容器里没了。 */
   @Override
   public void removed(Player player) {
      super.removed(player);
      this.access.execute((level, pos) -> clearContainer(player, this.craftSlots));
   }

   @Override
   public boolean stillValid(Player player) {
      return this.access.evaluate(
              (level, pos) -> level.getBlockState(pos).getBlock() instanceof BlockCarpentryBench
                      && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D,
              true);
   }

   /**
    * Shift 点击。分支照搬原版工作台：产出格往背包塞、合成格往背包塞、背包与快捷栏互换。
    *
    * <p>末尾那句 {@code stack.getCount() == result.getCount()} 判断不能省——
    * 它是"这次什么都没搬动"的标记，缺了它调用方会以为还有得搬而反复调用，造成死循环。
    * CLAUDE.md 里把 quickMoveStack 列为历史 bug 重灾区，指的就是这类遗漏。
    */
   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack result = ItemStack.EMPTY;
      Slot slot = this.slots.get(index);
      if (!slot.hasItem()) {
         return result;
      }

      ItemStack stack = slot.getItem();
      result = stack.copy();

      if (index == RESULT_SLOT) {
         this.access.execute((level, pos) -> stack.getItem().onCraftedBy(stack, level, player));
         if (!moveItemStackTo(stack, INV_START, HOTBAR_END, true)) {
            return ItemStack.EMPTY;
         }
         slot.onQuickCraft(stack, result);
      } else if (index >= INV_START) {
         if (index < HOTBAR_START) {
            if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!moveItemStackTo(stack, INV_START, HOTBAR_START, false)) {
            return ItemStack.EMPTY;
         }
      } else if (!moveItemStackTo(stack, INV_START, HOTBAR_END, false)) {
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
      if (index == RESULT_SLOT) {
         player.drop(stack, false);
      }
      return result;
   }

   @Override
   public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
      return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
   }
}
