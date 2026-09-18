package noppes.vc.init;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.recipes.CarpentryRecipe;
import noppes.vc.recipes.ReverseDaggerRecipe;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import noppes.vc.VariedCommodities;

/**
 * 配方序列化器注册。1.12 的 VCRecipes 是在代码里硬编码 IRecipe 实例，
 * 1.20.1 全部改为数据包配方；这里只注册那两个原版没有的序列化器。
 * 配方数据本身是静态资源（{@code data/variedcommodities/recipes/}，244 条），不走 DataGen。
 */
public final class VCRecipes {
   public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
           DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, VariedCommodities.MODID);

   public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
           DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, VariedCommodities.MODID);

   /**
    * 超过 3×3 的有序配方。<b>不是独立配方池</b>——木工台同样能做原版工作台的一切配方，
    * 这个类型只是为了绕开原版有序配方 JSON 的 3×3 上限，详见 {@link CarpentryRecipe}。
    */
   public static final RegistryObject<RecipeType<CarpentryRecipe>> CARPENTRY_TYPE =
           RECIPE_TYPES.register("carpentry", () -> new RecipeType<>() {
              @Override
              public String toString() {
                 return VariedCommodities.MODID + ":carpentry";
              }
           });

   public static final RegistryObject<RecipeSerializer<CarpentryRecipe>> CARPENTRY_SERIALIZER =
           RECIPE_SERIALIZERS.register("carpentry", CarpentryRecipe.Serializer::new);

   /** 匕首正反握互换（原作 RecipeReverse）。走原版 CRAFTING 类型，故不需要额外的 RecipeType。 */
   public static final RegistryObject<RecipeSerializer<ReverseDaggerRecipe>> REVERSE_DAGGER_SERIALIZER =
           RECIPE_SERIALIZERS.register("reverse_dagger",
                   () -> new SimpleCraftingRecipeSerializer<>(ReverseDaggerRecipe::new));

   private VCRecipes() {
   }

   public static void register(IEventBus bus) {
      RECIPE_TYPES.register(bus);
      RECIPE_SERIALIZERS.register(bus);
   }
}
