package noppes.vc.recipes;

import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import noppes.vc.init.VCRecipes;

/**
 * 木工台的有序配方，最大 4×4。
 *
 * <p><b>它不是「木工台专属配方池」。</b>原作 {@code RecipeContainer.add()} 造的是原版
 * {@code ShapedRecipes} 并注册进原版配方表，{@code VCRecipes.match()} 遍历的也是整个原版表，
 * 只是把匹配窗口放宽到 4×4 偏移扫描——木工台是万能工作台。
 * 之所以仍要自定义类型，唯一原因是<b>原版有序配方的 JSON 解析器把宽高限死在 3</b>
 * （{@code ShapedRecipe.MAX_WIDTH/MAX_HEIGHT}），VC 那 203 条超过 3×3 的配方根本写不进
 * {@code minecraft:crafting_shaped}。不超过 3×3 的 40 条则照常用原版类型，
 * 这样它们在普通工作台上也能做，与 1.12 行为一致。
 *
 * <p>JSON 格式与原版有序配方逐字段一致（{@code pattern} / {@code key} / {@code result}），
 * 只把上限放宽到 4。{@link #matches} 的偏移＋镜像扫描也照抄原版 {@code ShapedRecipe}，
 * 所以 4×4 格子里摆在哪个角落都认。
 */
public class CarpentryRecipe implements Recipe<CraftingContainer> {

   public static final int MAX_SIZE = 4;

   private final ResourceLocation id;
   private final String group;
   private final int width;
   private final int height;
   private final NonNullList<Ingredient> ingredients;
   private final ItemStack result;

   public CarpentryRecipe(ResourceLocation id, String group, int width, int height,
                          NonNullList<Ingredient> ingredients, ItemStack result) {
      this.id = id;
      this.group = group;
      this.width = width;
      this.height = height;
      this.ingredients = ingredients;
      this.result = result;
   }

   @Override
   public boolean matches(CraftingContainer container, Level level) {
      for (int x = 0; x <= container.getWidth() - this.width; x++) {
         for (int y = 0; y <= container.getHeight() - this.height; y++) {
            if (matchesAt(container, x, y, true) || matchesAt(container, x, y, false)) {
               return true;
            }
         }
      }
      return false;
   }

   /** 与原版有序配方同款：在每个偏移上试，并且左右镜像也算匹配。 */
   private boolean matchesAt(CraftingContainer container, int offsetX, int offsetY, boolean mirrored) {
      for (int x = 0; x < container.getWidth(); x++) {
         for (int y = 0; y < container.getHeight(); y++) {
            int localX = x - offsetX;
            int localY = y - offsetY;
            Ingredient expected = Ingredient.EMPTY;
            if (localX >= 0 && localY >= 0 && localX < this.width && localY < this.height) {
               int index = mirrored
                       ? (this.width - localX - 1) + localY * this.width
                       : localX + localY * this.width;
               expected = this.ingredients.get(index);
            }
            if (!expected.test(container.getItem(x + y * container.getWidth()))) {
               return false;
            }
         }
      }
      return true;
   }

   @Override
   public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
      return this.result.copy();
   }

   @Override
   public boolean canCraftInDimensions(int width, int height) {
      return width >= this.width && height >= this.height;
   }

   @Override
   public ItemStack getResultItem(RegistryAccess registries) {
      return this.result;
   }

   @Override
   public NonNullList<Ingredient> getIngredients() {
      return this.ingredients;
   }

   @Override
   public String getGroup() {
      return this.group;
   }

   @Override
   public ResourceLocation getId() {
      return this.id;
   }

   @Override
   public RecipeSerializer<?> getSerializer() {
      return VCRecipes.CARPENTRY_SERIALIZER.get();
   }

   @Override
   public RecipeType<?> getType() {
      return VCRecipes.CARPENTRY_TYPE.get();
   }

   public int width() {
      return this.width;
   }

   /** 产物本体。{@link #getResultItem} 要一个 RegistryAccess，而这里根本用不上它。 */
   public ItemStack result() {
      return this.result;
   }

   public int height() {
      return this.height;
   }

   public static class Serializer implements RecipeSerializer<CarpentryRecipe> {

      @Override
      public CarpentryRecipe fromJson(ResourceLocation id, JsonObject json) {
         String group = GsonHelper.getAsString(json, "group", "");
         Map<String, Ingredient> key = readKey(GsonHelper.getAsJsonObject(json, "key"));
         String[] pattern = readPattern(GsonHelper.getAsJsonArray(json, "pattern"));
         int width = pattern[0].length();
         int height = pattern.length;
         ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
         return new CarpentryRecipe(id, group, width, height, dissolve(pattern, key, width, height), result);
      }

      private static Map<String, Ingredient> readKey(JsonObject json) {
         Map<String, Ingredient> key = new java.util.HashMap<>();
         for (Map.Entry<String, com.google.gson.JsonElement> entry : json.entrySet()) {
            if (entry.getKey().length() != 1) {
               throw new com.google.gson.JsonSyntaxException(
                       "Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }
            if (" ".equals(entry.getKey())) {
               throw new com.google.gson.JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }
            key.put(entry.getKey(), Ingredient.fromJson(entry.getValue(), false));
         }
         key.put(" ", Ingredient.EMPTY);
         return key;
      }

      private static String[] readPattern(JsonArray json) {
         if (json.size() == 0 || json.size() > MAX_SIZE) {
            throw new com.google.gson.JsonSyntaxException(
                    "Invalid pattern: must be 1 to " + MAX_SIZE + " rows");
         }
         String[] pattern = new String[json.size()];
         for (int i = 0; i < pattern.length; i++) {
            String row = GsonHelper.convertToString(json.get(i), "pattern[" + i + "]");
            if (row.length() > MAX_SIZE) {
               throw new com.google.gson.JsonSyntaxException(
                       "Invalid pattern: too many columns, " + MAX_SIZE + " is maximum");
            }
            if (i > 0 && pattern[0].length() != row.length()) {
               throw new com.google.gson.JsonSyntaxException("Invalid pattern: each row must be the same width");
            }
            pattern[i] = row;
         }
         return pattern;
      }

      private static NonNullList<Ingredient> dissolve(String[] pattern, Map<String, Ingredient> key,
                                                      int width, int height) {
         NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
         Set<String> unused = new java.util.HashSet<>(key.keySet());
         unused.remove(" ");
         for (int y = 0; y < pattern.length; y++) {
            for (int x = 0; x < pattern[y].length(); x++) {
               String symbol = pattern[y].substring(x, x + 1);
               Ingredient ingredient = key.get(symbol);
               if (ingredient == null) {
                  throw new com.google.gson.JsonSyntaxException(
                          "Pattern references symbol '" + symbol + "' but it's not defined in the key");
               }
               unused.remove(symbol);
               ingredients.set(x + y * width, ingredient);
            }
         }
         if (!unused.isEmpty()) {
            throw new com.google.gson.JsonSyntaxException("Key defines symbols that aren't used in pattern: " + unused);
         }
         return ingredients;
      }

      @Override
      public CarpentryRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
         int width = buf.readVarInt();
         int height = buf.readVarInt();
         String group = buf.readUtf();
         NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
         for (int i = 0; i < ingredients.size(); i++) {
            ingredients.set(i, Ingredient.fromNetwork(buf));
         }
         return new CarpentryRecipe(id, group, width, height, ingredients, buf.readItem());
      }

      @Override
      public void toNetwork(FriendlyByteBuf buf, CarpentryRecipe recipe) {
         buf.writeVarInt(recipe.width);
         buf.writeVarInt(recipe.height);
         buf.writeUtf(recipe.group);
         for (Ingredient ingredient : recipe.ingredients) {
            ingredient.toNetwork(buf);
         }
         buf.writeItem(recipe.result);
      }
   }
}
