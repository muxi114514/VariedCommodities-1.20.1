package noppes.vc.recipes;

import java.util.Map;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.init.VCRecipes;
import noppes.vc.init.VCWeapons;

/**
 * 匕首正/反握互换（原作 {@code VCRecipes.RecipeReverse}）。
 *
 * <p>把一把匕首单独放进合成格，产出同材质的反握版本，<b>耐久与附魔原样保留</b>——
 * 原作 {@code CommonUtils.ChangeItemStack} 是把整份 NBT 抄过去只改 {@code id}，
 * 1.20.1 的耐久与附魔都在 tag 里，故复制 tag 即等价。
 *
 * <p>它走原版 {@link net.minecraft.world.item.crafting.RecipeType#CRAFTING}，
 * 因此普通工作台与木工台都能用，与原作把它注册进原版配方表的效果一致。
 *
 * <p><b>一处刻意的偏离：要求格子里只有这一件物品。</b>原作 {@code getCraftingResult}
 * 扫到第一把匕首就返回，而 {@code getRemainingItems} 会把其余格子一并消耗——
 * 匕首旁边随手放的东西会凭空消失，那是原作的 bug，不照抄。
 */
public class ReverseDaggerRecipe extends CustomRecipe {

   public ReverseDaggerRecipe(ResourceLocation id, CraftingBookCategory category) {
      super(id, category);
   }

   @Override
   public boolean matches(CraftingContainer container, Level level) {
      return !find(container).isEmpty();
   }

   @Override
   public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
      ItemStack input = find(container);
      if (input.isEmpty()) {
         return ItemStack.EMPTY;
      }
      ItemStack result = new ItemStack(Pairs.MAP.get(input.getItem()), input.getCount());
      if (input.getTag() != null) {
         result.setTag(input.getTag().copy());
      }
      return result;
   }

   /** 返回唯一一件非空且在互换表里的物品；不满足则空。 */
   private static ItemStack find(CraftingContainer container) {
      ItemStack found = ItemStack.EMPTY;
      for (int i = 0; i < container.getContainerSize(); i++) {
         ItemStack stack = container.getItem(i);
         if (stack.isEmpty()) {
            continue;
         }
         if (!found.isEmpty() || !Pairs.MAP.containsKey(stack.getItem())) {
            return ItemStack.EMPTY;
         }
         found = stack;
      }
      return found;
   }

   @Override
   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= 1;
   }

   @Override
   public RecipeSerializer<?> getSerializer() {
      return VCRecipes.REVERSE_DAGGER_SERIALIZER.get();
   }

   /**
    * 供配方书展示：20 条单向转换（双向各算一条）。
    * 特殊配方的 {@code getIngredients()} 是空的，界面拿不到料，只能由配方自己交出来。
    */
   public static Map<Item, Item> conversions() {
      return Pairs.MAP;
   }

   /**
    * 双向互换表。放在嵌套类里靠 JVM 的类初始化锁做懒加载：
    * 注册对象要等注册表冻结后才能 {@code get()}，而配方匹配必然发生在那之后。
    */
   private static final class Pairs {

      @SuppressWarnings("unchecked")
      private static final RegistryObject<Item>[] ITEMS = new RegistryObject[]{
              VCWeapons.WOODEN_DAGGER, VCWeapons.WOODEN_DAGGER_REVERSED,
              VCWeapons.STONE_DAGGER, VCWeapons.STONE_DAGGER_REVERSED,
              VCWeapons.IRON_DAGGER, VCWeapons.IRON_DAGGER_REVERSED,
              VCWeapons.GOLDEN_DAGGER, VCWeapons.GOLDEN_DAGGER_REVERSED,
              VCWeapons.DIAMOND_DAGGER, VCWeapons.DIAMOND_DAGGER_REVERSED,
              VCWeapons.BRONZE_DAGGER, VCWeapons.BRONZE_DAGGER_REVERSED,
              VCWeapons.EMERALD_DAGGER, VCWeapons.EMERALD_DAGGER_REVERSED,
              VCWeapons.DEMONIC_DAGGER, VCWeapons.DEMONIC_DAGGER_REVERSED,
              VCWeapons.FROST_DAGGER, VCWeapons.FROST_DAGGER_REVERSED,
              VCWeapons.MITHRIL_DAGGER, VCWeapons.MITHRIL_DAGGER_REVERSED,
      };

      private static final Map<Item, Item> MAP = build();

      private Pairs() {
      }

      private static Map<Item, Item> build() {
         // LinkedHashMap 而非 HashMap：配方书按插入序展示，顺序要稳定
         Map<Item, Item> map = new java.util.LinkedHashMap<>();
         for (int i = 0; i < ITEMS.length; i += 2) {
            Item a = ITEMS[i].get();
            Item b = ITEMS[i + 1].get();
            map.put(a, b);
            map.put(b, a);
         }
         return java.util.Collections.unmodifiableMap(map);
      }
   }
}
