package noppes.vc.compat.jei;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import noppes.vc.VariedCommodities;
import noppes.vc.blocks.storage.StorageTier;
import noppes.vc.config.VCConfig;
import noppes.vc.containers.CarpentryMenu;
import noppes.vc.init.VCBlocks;
import noppes.vc.init.VCMenus;
import noppes.vc.init.VCRecipes;
import noppes.vc.recipes.CarpentryRecipe;

/**
 * JEI 插件。1.12 时代 VC 要装 VCFix 才有 JEI 支持，这里内建。
 *
 * <p><b>无 JEI 环境不会加载本包的任何类</b>：{@code @JeiPlugin} 由 JEI 自己扫描，
 * JEI 不在就没人扫，类加载器永远碰不到这些引用。build.gradle 里 JEI 也只是 compileOnly。
 */
@JeiPlugin
public class VCJeiPlugin implements IModPlugin {

   private static final ResourceLocation UID =
           new ResourceLocation(VariedCommodities.MODID, "jei");

   @Override
   public ResourceLocation getPluginUid() {
      return UID;
   }

   @Override
   public void registerCategories(IRecipeCategoryRegistration registration) {
      registration.addRecipeCategories(
              new CarpentryCategory(registration.getJeiHelpers().getGuiHelper()));
   }

   @Override
   public void registerRecipes(IRecipeRegistration registration) {
      addCrateUpgradeInfo(registration);

      ClientLevel level = Minecraft.getInstance().level;
      if (level == null) {
         return;   // 标题界面重载资源时没有世界，配方表也还没同步
      }
      List<CarpentryRecipe> recipes =
              new ArrayList<>(level.getRecipeManager().getAllRecipesFor(VCRecipes.CARPENTRY_TYPE.get()));
      recipes.sort(Comparator.comparing(r -> r.getId().getPath()));
      registration.addRecipes(CarpentryCategory.TYPE, recipes);
   }

   /**
    * 板条箱升级是潜行右键、没有配方，JEI 里看不到就无从发现，故挂一页信息。
    * 每档一行，容量与消耗都从 {@link StorageTier} 与配置实时取，lang 里不写死数字。
    */
   private static void addCrateUpgradeInfo(IRecipeRegistration registration) {
      if (!VCConfig.crateUpgradesEnabled()) {
         return;
      }
      List<ItemStack> crates = new ArrayList<>();
      VCBlocks.CRATE.values().forEach(block -> crates.add(new ItemStack(block.get())));
      List<Component> lines = new ArrayList<>();
      lines.add(Component.translatable("variedcommodities.jei.crate_upgrade"));
      VCBlocks.UPGRADED_CRATE.forEach((tier, block) -> {
         crates.add(new ItemStack(block.get()));
         lines.add(Component.translatable("variedcommodities.jei.crate_tier",
                 block.get().getName(), tier.size(), VCConfig.crateUpgradeCost(tier),
                 tier.displayMaterial().getDescription()));
      });
      registration.addItemStackInfo(crates, lines.toArray(Component[]::new));
   }

   @Override
   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      // 两款木工台都挂在原版合成页上：它们是 4×4 的万能工作台，原版配方照做不误
      registration.addRecipeCatalyst(new ItemStack(VCBlocks.CARPENTRY_BENCH.get()),
              CarpentryCategory.TYPE, RecipeTypes.CRAFTING);
      registration.addRecipeCatalyst(new ItemStack(VCBlocks.CARPENTRY_BENCH_ANVIL.get()),
              CarpentryCategory.TYPE, RecipeTypes.CRAFTING);
   }

   @Override
   public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
      // 4×4 配方：槽位一一对应，用基础版填充器即可
      registration.addRecipeTransferHandler(CarpentryMenu.class, VCMenus.CARPENTRY.get(),
              CarpentryCategory.TYPE,
              CarpentryMenu.GRID_START, CarpentryMenu.GRID * CarpentryMenu.GRID,
              CarpentryMenu.INV_START, CarpentryMenu.HOTBAR_END - CarpentryMenu.INV_START);
      // 原版 3×3 配方：需要换算行列，见 CraftingTransferInfo
      registration.addRecipeTransferHandler(new CraftingTransferInfo());
   }
}
