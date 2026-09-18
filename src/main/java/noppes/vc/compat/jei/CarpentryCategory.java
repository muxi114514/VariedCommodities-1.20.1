package noppes.vc.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import noppes.vc.init.VCBlocks;
import noppes.vc.recipes.CarpentryRecipe;

/**
 * JEI 里的木工台配方页：4×4 格 + 箭头 + 产物。
 *
 * <p>只收 {@code variedcommodities:carpentry} 这一类，也就是<b>超过 3×3 的那 203 条</b>；
 * 不超过 3×3 的 40 条本身就是原版有序配方，JEI 的原版合成页已经在显示了，
 * 这里再显示一遍等于重复。木工台会作为"催化剂"同时挂在两个页上（见 {@link VCJeiPlugin}），
 * 因为它是万能 4×4 工作台，原版配方在它上面照样能做。
 */
public class CarpentryCategory implements IRecipeCategory<CarpentryRecipe> {

   public static final RecipeType<CarpentryRecipe> TYPE =
           RecipeType.create("variedcommodities", "carpentry", CarpentryRecipe.class);

   /** 原版工作台界面里的那支箭，直接借用，省一张贴图。 */
   private static final ResourceLocation VANILLA_CRAFTING =
           new ResourceLocation("textures/gui/container/crafting_table.png");

   private static final int GRID = CarpentryRecipe.MAX_SIZE;
   private static final int OUTPUT_X = 108;
   private static final int OUTPUT_Y = 28;

   private final IDrawableStatic slot;
   private final IDrawableStatic arrow;
   private final IDrawable icon;

   public CarpentryCategory(IGuiHelper helper) {
      this.slot = helper.getSlotDrawable();
      this.arrow = helper.createDrawable(VANILLA_CRAFTING, 90, 35, 22, 15);
      this.icon = helper.createDrawableItemStack(new ItemStack(VCBlocks.CARPENTRY_BENCH.get()));
   }

   @Override
   public RecipeType<CarpentryRecipe> getRecipeType() {
      return TYPE;
   }

   @Override
   public Component getTitle() {
      // 借方块自己的显示名，不另起 lang 键
      return VCBlocks.CARPENTRY_BENCH.get().getName();
   }

   @Override
   public int getWidth() {
      return OUTPUT_X + 17;
   }

   @Override
   public int getHeight() {
      return GRID * 18;
   }

   @Override
   public IDrawable getIcon() {
      return this.icon;
   }

   @Override
   public void draw(CarpentryRecipe recipe, IRecipeSlotsView slots, GuiGraphics gui,
                    double mouseX, double mouseY) {
      this.arrow.draw(gui, GRID * 18 + 5, OUTPUT_Y);
   }

   @Override
   public void setRecipe(IRecipeLayoutBuilder builder, CarpentryRecipe recipe, IFocusGroup focuses) {
      // 16 个格子全部建出来（空格也建），这样 JEI 的配方填充按下标一一对应木工台的槽位
      for (int y = 0; y < GRID; y++) {
         for (int x = 0; x < GRID; x++) {
            IRecipeSlotBuilder builderSlot = builder
                    .addSlot(RecipeIngredientRole.INPUT, x * 18 + 1, y * 18 + 1)
                    .setBackground(this.slot, -1, -1);
            if (x < recipe.width() && y < recipe.height()) {
               builderSlot.addIngredients(recipe.getIngredients().get(x + y * recipe.width()));
            }
         }
      }
      builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
              .setBackground(this.slot, -1, -1)
              .addItemStack(recipe.result());
   }
}
