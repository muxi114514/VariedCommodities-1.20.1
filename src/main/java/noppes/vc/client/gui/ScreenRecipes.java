package noppes.vc.client.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import noppes.vc.VariedCommodities;
import noppes.vc.init.VCRecipes;
import noppes.vc.recipes.CarpentryRecipe;
import noppes.vc.recipes.ReverseDaggerRecipe;

/**
 * 配方书界面（原作 {@code GuiRecipes}）。纯展示，不涉及服务端。
 *
 * <p>版式逐个坐标照搬原作，所以 recipes.png / slot.png 能直接对上：
 * 256×182 背景，每页 2×2 共 4 格，格内配方居中、产物固定在右侧。
 *
 * <p>数据源与原作不同但语义一致：原作读自己维护的 {@code VCRecipes.List}，
 * 这里读客户端已同步的 {@link RecipeManager}，按命名空间筛出 VC 的配方。
 * 同一格里的变体（六种木材、十种材质…）靠配方的 {@code group} 字段聚合，
 * 那正是原作一个 {@code RecipeContainer} 装多条配方的等价物，每 2 秒轮播一条。
 */
public class ScreenRecipes extends Screen {

   private static final ResourceLocation BACKGROUND =
           new ResourceLocation(VariedCommodities.MODID, "textures/gui/recipes.png");
   private static final ResourceLocation SLOT =
           new ResourceLocation(VariedCommodities.MODID, "textures/gui/slot.png");

   private static final int BG_WIDTH = 256;
   private static final int BG_HEIGHT = 182;
   private static final int PER_PAGE = 4;
   private static final int LABEL_COLOR = 0x404040;
   /** 变体轮播周期，与原作 {@code System.currentTimeMillis() / 2000L} 一致。 */
   private static final long VARIANT_MS = 2000L;
   /** 标签类材料（如 #minecraft:planks）在格子里轮播可选项，原作只画第一个。 */
   private static final long INGREDIENT_MS = 1000L;

   private final List<List<Entry>> cells;
   private int page;
   private int left;
   private int top;
   private PageButton prev;
   private PageButton next;

   public ScreenRecipes(ClientLevel level) {
      super(Component.translatable("variedcommodities.gui.recipe_list"));
      this.cells = collect(level);
   }

   @Override
   protected void init() {
      this.left = (this.width - BG_WIDTH) / 2;
      this.top = (this.height - BG_HEIGHT) / 2;
      this.prev = addRenderableWidget(new PageButton(this.left + 80, this.top + 164, false,
              b -> turn(-1), true));
      this.next = addRenderableWidget(new PageButton(this.left + 150, this.top + 164, true,
              b -> turn(1), true));
      updateButtons();
   }

   private void turn(int delta) {
      this.page = Mth.clamp(this.page + delta, 0, pages() - 1);
      updateButtons();
   }

   private int pages() {
      return Math.max(1, Mth.ceil(this.cells.size() / (float) PER_PAGE));
   }

   private void updateButtons() {
      this.prev.visible = this.page > 0;
      this.next.visible = this.page + 1 < pages();
   }

   @Override
   public void render(GuiGraphics gui, int mouseX, int mouseY, float partial) {
      renderBackground(gui);
      gui.blit(BACKGROUND, this.left, this.top, 0, 0, BG_WIDTH, BG_HEIGHT);
      gui.drawString(this.font, this.title, this.left + 5, this.top + 5, LABEL_COLOR, false);

      String indicator = (this.page + 1) + "/" + pages();
      gui.drawString(this.font, indicator,
              this.left + (BG_WIDTH - this.font.width(indicator)) / 2, this.top + 168, LABEL_COLOR, false);

      // 先整页画完物品，再统一画悬浮提示——提示必须压在所有物品之上
      ItemStack hovered = ItemStack.EMPTY;
      long now = System.currentTimeMillis();
      for (int i = 0; i < PER_PAGE; i++) {
         int index = i + this.page * PER_PAGE;
         if (index >= this.cells.size()) {
            break;
         }
         List<Entry> variants = this.cells.get(index);
         Entry entry = variants.get(variants.size() == 1 ? 0 : (int) (now / VARIANT_MS % variants.size()));
         ItemStack hit = drawCell(gui, entry,
                 this.left + 5 + i / 2 * 126, this.top + 15 + i % 2 * 76, mouseX, mouseY);
         if (!hit.isEmpty()) {
            hovered = hit;
         }
      }
      super.render(gui, mouseX, mouseY, partial);
      if (!hovered.isEmpty()) {
         gui.renderTooltip(this.font, hovered, mouseX, mouseY);
      }
   }

   /** 画一格，返回鼠标悬停的物品（没有则空）。 */
   private ItemStack drawCell(GuiGraphics gui, Entry entry, int x, int y, int mouseX, int mouseY) {
      ItemStack hovered = draw(gui, entry.result(), x + 98, y + 28, mouseX, mouseY);

      int gx = x + (72 - entry.width() * 18) / 2;
      int gy = y + (72 - entry.height() * 18) / 2;
      long now = System.currentTimeMillis();
      for (int i = 0; i < entry.input().size(); i++) {
         int sx = gx + i % entry.width() * 18;
         int sy = gy + i / entry.width() * 18;
         gui.blit(SLOT, sx, sy, 0, 0, 18, 18);
         ItemStack[] options = entry.input().get(i).getItems();
         if (options.length == 0) {
            continue;
         }
         ItemStack option = options[(int) (now / INGREDIENT_MS % options.length)];
         ItemStack hit = draw(gui, option, sx + 1, sy + 1, mouseX, mouseY);
         if (!hit.isEmpty()) {
            hovered = hit;
         }
      }
      return hovered;
   }

   private ItemStack draw(GuiGraphics gui, ItemStack stack, int x, int y, int mouseX, int mouseY) {
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      }
      gui.renderItem(stack, x, y);
      gui.renderItemDecorations(this.font, stack, x, y);
      boolean over = mouseX >= x - 1 && mouseX < x + 17 && mouseY >= y - 1 && mouseY < y + 17;
      return over ? stack : ItemStack.EMPTY;
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   // ── 取数 ─────────────────────────────────────────────────────────────

   /**
    * 一格 = 一个 {@code group}（没有 group 的配方自成一格）。
    * 先按注册名排序再分组，保证每次打开顺序一致。
    */
   private static List<List<Entry>> collect(ClientLevel level) {
      List<Recipe<?>> all = new ArrayList<>();
      RecipeManager manager = level.getRecipeManager();
      all.addAll(manager.getAllRecipesFor(RecipeType.CRAFTING));
      all.addAll(manager.getAllRecipesFor(VCRecipes.CARPENTRY_TYPE.get()));
      all.addAll(manager.getAllRecipesFor(RecipeType.SMELTING));
      all.removeIf(r -> !VariedCommodities.MODID.equals(r.getId().getNamespace()));
      all.sort(Comparator.comparing((Recipe<?> r) -> r.getId().getPath()));

      Map<String, List<Entry>> byGroup = new LinkedHashMap<>();
      for (Recipe<?> recipe : all) {
         if (recipe instanceof ReverseDaggerRecipe) {
            byGroup.put(recipe.getId().toString(), reverseEntries());
            continue;
         }
         Entry entry = toEntry(recipe, level);
         if (entry == null) {
            continue;
         }
         String key = recipe.getGroup().isEmpty() ? recipe.getId().toString() : recipe.getGroup();
         byGroup.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
      }
      return List.copyOf(byGroup.values());
   }

   private static Entry toEntry(Recipe<?> recipe, ClientLevel level) {
      List<Ingredient> input = recipe.getIngredients();
      ItemStack result = recipe.getResultItem(level.registryAccess());
      if (input.isEmpty() || result.isEmpty()) {
         return null;   // 特殊配方没有可展示的料，跳过
      }
      int width;
      int height;
      if (recipe instanceof CarpentryRecipe carpentry) {
         width = carpentry.width();
         height = carpentry.height();
      } else if (recipe instanceof ShapedRecipe shaped) {
         width = shaped.getWidth();
         height = shaped.getHeight();
      } else {
         width = Math.min(input.size(), 3);
         height = Mth.ceil(input.size() / (float) width);
      }
      return new Entry(width, height, input, result);
   }

   /** 匕首正反握互换：特殊配方，料表是空的，从配方自己那儿取互换表。 */
   private static List<Entry> reverseEntries() {
      List<Entry> entries = new ArrayList<>();
      for (Map.Entry<Item, Item> pair : ReverseDaggerRecipe.conversions().entrySet()) {
         entries.add(new Entry(1, 1, List.of(Ingredient.of(pair.getKey())),
                 new ItemStack(pair.getValue())));
      }
      return entries;
   }

   /** 格子里实际画出来的一条配方。 */
   private record Entry(int width, int height, List<Ingredient> input, ItemStack result) {
   }
}
