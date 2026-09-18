package noppes.vc.recipes;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import noppes.vc.VariedCommodities;
import noppes.vc.config.VCConfig;

/**
 * 「这条配方开着吗」——挂在每一份 VC 配方 json 的 {@code conditions} 里。
 *
 * <p>Forge 在数据包加载时求值，<b>判定为假的配方根本不会进配方表</b>：
 * JEI 看不到、配方书里也没有，比"能合成但藏起来"干净。
 * 配方条件求值远晚于配置加载，所以这里可以放心读 {@link VCConfig}。
 */
public record RecipeEnabledCondition(String recipe) implements ICondition {

   public static final ResourceLocation ID =
           new ResourceLocation(VariedCommodities.MODID, "recipe_enabled");
   public static final Serializer SERIALIZER = new Serializer();

   @Override
   public ResourceLocation getID() {
      return ID;
   }

   @Override
   public boolean test(IContext context) {
      return VCConfig.recipeEnabled(this.recipe);
   }

   @Override
   public String toString() {
      return "recipe_enabled(\"" + this.recipe + "\")";
   }

   public static final class Serializer implements IConditionSerializer<RecipeEnabledCondition> {

      private Serializer() {
      }

      @Override
      public void write(JsonObject json, RecipeEnabledCondition condition) {
         json.addProperty("recipe", condition.recipe());
      }

      @Override
      public RecipeEnabledCondition read(JsonObject json) {
         return new RecipeEnabledCondition(GsonHelper.getAsString(json, "recipe"));
      }

      @Override
      public ResourceLocation getID() {
         return RecipeEnabledCondition.ID;
      }
   }
}
