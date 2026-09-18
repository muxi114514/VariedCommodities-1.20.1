package noppes.vc.compat.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import noppes.vc.containers.CarpentryMenu;
import noppes.vc.init.VCMenus;

/**
 * 让 JEI 的"+"能把<b>原版 3×3 配方</b>填进木工台的 4×4 格。
 *
 * <p>不能用 JEI 的基础版填充器：它按下标平铺（配方槽 i → 容器槽 start+i），
 * 而 3×3 行主序摊进 4 宽的格子会整体错位——第二行的头一个会落到第一行的第四格。
 * 这里改为按行列换算：配方第 i 格 → 木工台第 (i/3) 行第 (i%3) 列。
 *
 * <p>JEI 的原版合成页固定建 9 个输入槽（{@code CraftingGridHelper} 里写死 3×3 双重循环），
 * 与配方本身的宽高无关，所以这个换算对任意原版有序/无序配方都成立。
 */
public class CraftingTransferInfo implements IRecipeTransferInfo<CarpentryMenu, CraftingRecipe> {

   private static final int VANILLA_GRID = 3;

   @Override
   public Class<? extends CarpentryMenu> getContainerClass() {
      return CarpentryMenu.class;
   }

   @Override
   public Optional<MenuType<CarpentryMenu>> getMenuType() {
      return Optional.of(VCMenus.CARPENTRY.get());
   }

   @Override
   public RecipeType<CraftingRecipe> getRecipeType() {
      return RecipeTypes.CRAFTING;
   }

   @Override
   public boolean canHandle(CarpentryMenu container, CraftingRecipe recipe) {
      return true;
   }

   @Override
   public List<Slot> getRecipeSlots(CarpentryMenu container, CraftingRecipe recipe) {
      List<Slot> slots = new ArrayList<>(VANILLA_GRID * VANILLA_GRID);
      for (int i = 0; i < VANILLA_GRID * VANILLA_GRID; i++) {
         slots.add(container.getSlot(CarpentryMenu.gridSlot(i / VANILLA_GRID, i % VANILLA_GRID)));
      }
      return slots;
   }

   @Override
   public List<Slot> getInventorySlots(CarpentryMenu container, CraftingRecipe recipe) {
      List<Slot> slots = new ArrayList<>(CarpentryMenu.HOTBAR_END - CarpentryMenu.INV_START);
      for (int i = CarpentryMenu.INV_START; i < CarpentryMenu.HOTBAR_END; i++) {
         slots.add(container.getSlot(i));
      }
      return slots;
   }
}
