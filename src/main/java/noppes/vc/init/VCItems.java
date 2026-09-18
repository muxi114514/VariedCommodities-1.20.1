package noppes.vc.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.VariedCommodities;
import noppes.vc.items.ItemArmorBasic;
import noppes.vc.items.ItemArmorBasicColorable;
import noppes.vc.items.ItemMusic;
import noppes.vc.items.ItemRecipesBook;
import noppes.vc.items.ItemWand;

import static noppes.vc.items.VCItemProps.basic;
import static noppes.vc.items.VCItemProps.durable;
import static noppes.vc.items.VCItemProps.single;

/**
 * 物品注册总入口，并持有整个模组唯一的物品 DeferredRegister。
 *
 * <p>本类只登记「杂项」创造页的 69 项（对应 1.12 的 VCItems）；
 * 武器在 {@link VCWeapons}（194 项）、护甲在 {@link VCArmors}（76 项），
 * 与原作的三个文件一一对应，也避免单个文件过长。
 * 方块物品由 {@link VCBlocks} 挂在同一个 register 上。
 *
 * <p>展开 16 色变体后物品共 <b>384</b> 项（339 个 1.12 名 − 3 + 3×16）。
 *
 * <p>所有物品机制均已落地（乐器、点火、魔杖等见 {@code noppes.vc.items}）。
 * ⚠️ 法球（orb/orb_broken）在原作里<b>没有任何行为</b>——{@code ItemOrb} 只有 getSubItems，
 * 它是纯合成材料兼法杖弹药的贴图来源，不要为它凭空补机制。
 */
public final class VCItems {

   public static final DeferredRegister<Item> ITEMS =
           DeferredRegister.create(ForgeRegistries.ITEMS, VariedCommodities.MODID);

   /** 展示顺序按字段声明顺序填充，即 1.12 的注册顺序。 */
   private static final List<RegistryObject<Item>> ORDER = new ArrayList<>();

   public static final RegistryObject<Item> VCRECIPES_BOOK =
           register("vcrecipes_book", () -> new ItemRecipesBook(single()), ORDER);
   public static final RegistryObject<Item> COIN_WOOD = item("coin_wood", basic());
   public static final RegistryObject<Item> COIN_STONE = item("coin_stone", basic());
   public static final RegistryObject<Item> COIN_BRONZE = item("coin_bronze", basic());
   public static final RegistryObject<Item> COIN_IRON = item("coin_iron", basic());
   public static final RegistryObject<Item> COIN_GOLD = item("coin_gold", basic());
   public static final RegistryObject<Item> COIN_DIAMOND = item("coin_diamond", basic());
   public static final RegistryObject<Item> COIN_EMERALD = item("coin_emerald", basic());
   public static final RegistryObject<Item> GEM_SAPPHIRE = item("gem_sapphire", basic());
   public static final RegistryObject<Item> GEM_RUBY = item("gem_ruby", basic());
   public static final RegistryObject<Item> GEM_AMETHYST = item("gem_amethyst", basic());
   public static final RegistryObject<Item> INGOT_BRONZE = item("ingot_bronze", basic());
   public static final RegistryObject<Item> INGOT_STEEL = item("ingot_steel", basic());
   public static final RegistryObject<Item> INGOT_DEMONIC = item("ingot_demonic", basic());
   public static final RegistryObject<Item> INGOT_MITHRIL = item("ingot_mithril", basic());
   public static final RegistryObject<Item> BANJO = music("banjo", false);
   public static final RegistryObject<Item> VIOLIN = music("violin", true);
   public static final RegistryObject<Item> VIOLIN_BOW = item("violin_bow", single());  // 原作 setFull3D()，1.20.1 由物品模型 display 决定（P5）
   public static final RegistryObject<Item> HARP = music("harp", true);
   public static final RegistryObject<Item> GUITAR = music("guitar", false);
   public static final RegistryObject<Item> FRENCH_HORN = music("french_horn", false);
   public static final RegistryObject<Item> OCARINA = music("ocarina", true);
   public static final RegistryObject<Item> CLARINET = music("clarinet", true);
   public static final RegistryObject<Item> ELEMENT_EARTH = item("element_earth", basic());
   public static final RegistryObject<Item> ELEMENT_WATER = item("element_water", basic());
   public static final RegistryObject<Item> ELEMENT_FIRE = item("element_fire", basic());
   public static final RegistryObject<Item> ELEMENT_AIR = item("element_air", basic());
   public static final RegistryObject<Item> SPELL_NATURE = item("spell_nature", basic());
   public static final RegistryObject<Item> SPELL_ARCANE = item("spell_arcane", basic());
   public static final RegistryObject<Item> SPELL_LIGHTNING = item("spell_lightning", basic());
   public static final RegistryObject<Item> SPELL_ICE = item("spell_ice", basic());
   public static final RegistryObject<Item> SPELL_FIRE = item("spell_fire", basic());
   public static final RegistryObject<Item> SPELL_DARK = item("spell_dark", basic());
   public static final RegistryObject<Item> SPELL_HOLY = item("spell_holy", basic());
   /** 16 色变体：共用一张灰度贴图 + IItemColor 着色（非 16 张图）。
    原作 {@code ItemOrb} 只有 getSubItems，<b>没有任何行为</b>，是纯合成材料。 */
   public static final Map<DyeColor, RegistryObject<Item>> ORB =
           colorFamily("orb", basic());
   /** 16 色变体：共用一张灰度贴图 + IItemColor 着色（非 16 张图）。
    原作 {@code ItemOrb} 只有 getSubItems，<b>没有任何行为</b>，是纯合成材料。 */
   public static final Map<DyeColor, RegistryObject<Item>> ORB_BROKEN =
           colorFamily("orb_broken", basic());
   public static final RegistryObject<Item> ANCIENT_SCROLL = item("ancient_scroll", basic());
   public static final RegistryObject<Item> ARTIFACT = item("artifact", basic());
   public static final RegistryObject<Item> LOCKET = item("locket", basic());
   public static final RegistryObject<Item> SILK = item("silk", basic());
   public static final RegistryObject<Item> STATUETTE = item("statuette", basic());
   public static final RegistryObject<Item> TABLET = item("tablet", basic());
   public static final RegistryObject<Item> HEART = item("heart", basic());
   public static final RegistryObject<Item> MONEY = item("money", basic());
   public static final RegistryObject<Item> NECKLACE = item("necklace", basic());
   public static final RegistryObject<Item> USB_STICK = item("usb_stick", basic());
   public static final RegistryObject<Item> ANCIENT_COIN = item("ancient_coin", basic());
   public static final RegistryObject<Item> LETTER = item("letter", basic());
   public static final RegistryObject<Item> PLANS = item("plans", basic());
   public static final RegistryObject<Item> SATCHEL = item("satchel", basic());
   public static final RegistryObject<Item> BAG = item("bag", basic());
   public static final RegistryObject<Item> CRYSTAL = item("crystal", basic());
   public static final RegistryObject<Item> SEVERED_EAR = item("severed_ear", basic());
   public static final RegistryObject<Item> PHONE = item("phone", basic());
   public static final RegistryObject<Item> BANDIT_MASK = armor("bandit_mask", ArmorMaterials.IRON, Type.HELMET, "bandit_mask");
   public static final RegistryObject<Item> PAPER_CROWN = colorable("paper_crown", ArmorMaterials.LEATHER, Type.HELMET, "paper_crown");
   public static final RegistryObject<Item> BROKEN_ARROW = item("broken_arrow", basic());
   public static final RegistryObject<Item> CAR_KEY = item("car_key", basic());
   public static final RegistryObject<Item> KEY = item("key", basic());
   public static final RegistryObject<Item> KEY2 = item("key2", basic());
   public static final RegistryObject<Item> PENDANT = item("pendant", basic());
   public static final RegistryObject<Item> BLUEPRINT = item("blueprint", basic());
   public static final RegistryObject<Item> RING = item("ring", basic());
   public static final RegistryObject<Item> SKULL = item("skull", basic());
   public static final RegistryObject<Item> LIGHTER =
           register("lighter", () -> new FlintAndSteelItem(durable(64)), ORDER);
   public static final RegistryObject<Item> CHICKEN_SWORD = item("chicken_sword", single());  // 原作 setFull3D()，1.20.1 由物品模型 display 决定（P5）
   public static final RegistryObject<Item> HANDCUFFS = item("handcuffs", single());  // 原作 setFull3D()，1.20.1 由物品模型 display 决定（P5）
   public static final RegistryObject<Item> MAGIC_WAND =
           register("magic_wand", () -> new ItemWand(single()), ORDER);
   public static final RegistryObject<Item> CROSSBOW_BOLT = item("crossbow_bolt", basic());

   private VCItems() {
   }

   public static void register(IEventBus bus) {
      // 先触碰另外两个类以触发其 clinit（注册都在静态字段初始化时发生），再挂到总线
      VCWeapons.init();
      VCArmors.init();
      ITEMS.register(bus);
   }

   /** 本页创造栏的展示顺序。 */
   public static List<RegistryObject<Item>> creativeOrder() {
      return Collections.unmodifiableList(ORDER);
   }

   // ── 注册辅助 ─────────────────────────────────────────────────────────

   /** 三个注册文件共用的登记原语：入注册表，并记进各自的展示顺序表。 */
   static RegistryObject<Item> register(String name, Supplier<Item> factory,
                                        List<RegistryObject<Item>> order) {
      RegistryObject<Item> item = ITEMS.register(name, factory);
      order.add(item);
      return item;
   }

   private static RegistryObject<Item> item(String name, Item.Properties props) {
      return register(name, () -> new Item(props), ORDER);
   }

   /** 乐器：{@code bowLike} 的三件在原作里多一个拉弓姿势。 */
   private static RegistryObject<Item> music(String name, boolean bowLike) {
      return register(name, () -> new ItemMusic(bowLike, single()), ORDER);
   }

   /** 少数护甲被原作显式放进杂项页（bandit_mask / paper_crown），故此处也要这两个助手。 */
   private static RegistryObject<Item> armor(String name, ArmorMaterial material, Type type, String texture) {
      return register(name, () -> new ItemArmorBasic(material, type, texture, single()), ORDER);
   }

   private static RegistryObject<Item> colorable(String name, ArmorMaterial material, Type type, String texture) {
      return register(name, () -> new ItemArmorBasicColorable(material, type, texture, single()), ORDER);
   }

   /** 按 1.12 的 meta 顺序展开 16 个染料色变体（meta0=黑 … meta15=白）。 */
   private static Map<DyeColor, RegistryObject<Item>> colorFamily(String base, Item.Properties props) {
      EnumMap<DyeColor, RegistryObject<Item>> map = new EnumMap<>(DyeColor.class);
      for (int meta = 0; meta < 16; meta++) {
         DyeColor color = DyeColor.byId(15 - meta);
         map.put(color, item(base + "_" + color.getSerializedName(), props));
      }
      return Collections.unmodifiableMap(map);
   }
}
