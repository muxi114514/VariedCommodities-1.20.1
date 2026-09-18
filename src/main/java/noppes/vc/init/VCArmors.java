package noppes.vc.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.items.ItemArmorBasic;
import noppes.vc.items.ItemArmorBasicColorable;
import noppes.vc.items.ItemArmorSkirt;
import noppes.vc.items.VCArmorMaterial;

import static noppes.vc.items.VCItemProps.single;

/**
 * 护甲注册（对应 1.12 的 VCArmors，76 项）。
 * 
 * 贴图路径沿用原作（腿部 _2.png、其余 _1.png）；裙甲的专用模型属 P5。
 * 三档自定义材质见 VCArmorMaterial。
 */
public final class VCArmors {

   /** 展示顺序按字段声明顺序填充，即 1.12 的注册顺序。 */
   private static final List<RegistryObject<Item>> ORDER = new ArrayList<>();

   public static final RegistryObject<Item> COW_LEATHER_HEAD = armor("cow_leather_head", ArmorMaterials.LEATHER, Type.HELMET, "cow_leather");
   public static final RegistryObject<Item> COW_LEATHER_CHEST = armor("cow_leather_chest", ArmorMaterials.LEATHER, Type.CHESTPLATE, "cow_leather");
   public static final RegistryObject<Item> COW_LEATHER_LEGS = armor("cow_leather_legs", ArmorMaterials.LEATHER, Type.LEGGINGS, "cow_leather");
   public static final RegistryObject<Item> COW_LEATHER_BOOTS = armor("cow_leather_boots", ArmorMaterials.LEATHER, Type.BOOTS, "cow_leather");
   public static final RegistryObject<Item> NANORUM_HEAD = armor("nanorum_head", ArmorMaterials.IRON, Type.HELMET, "nanorum");
   public static final RegistryObject<Item> NANORUM_CHEST = armor("nanorum_chest", ArmorMaterials.IRON, Type.CHESTPLATE, "nanorum");
   public static final RegistryObject<Item> NANORUM_LEGS = armor("nanorum_legs", ArmorMaterials.IRON, Type.LEGGINGS, "nanorum");
   public static final RegistryObject<Item> NANORUM_BOOTS = armor("nanorum_boots", ArmorMaterials.IRON, Type.BOOTS, "nanorum");
   public static final RegistryObject<Item> TACTICAL_HEAD = armor("tactical_head", ArmorMaterials.IRON, Type.HELMET, "tactical");
   public static final RegistryObject<Item> TACTICAL_CHEST = armor("tactical_chest", ArmorMaterials.IRON, Type.CHESTPLATE, "tactical");
   public static final RegistryObject<Item> FULL_LEATHER_HEAD = armor("full_leather_head", ArmorMaterials.LEATHER, Type.HELMET, "full_cloth");
   public static final RegistryObject<Item> FULL_LEATHER_CHEST = armor("full_leather_chest", ArmorMaterials.LEATHER, Type.CHESTPLATE, "full_cloth");
   public static final RegistryObject<Item> FULL_IRON_HEAD = armor("full_iron_head", ArmorMaterials.IRON, Type.HELMET, "full_iron");
   public static final RegistryObject<Item> FULL_IRON_CHEST = armor("full_iron_chest", ArmorMaterials.IRON, Type.CHESTPLATE, "full_iron");
   public static final RegistryObject<Item> FULL_GOLDEN_HEAD = armor("full_golden_head", ArmorMaterials.GOLD, Type.HELMET, "full_golden");
   public static final RegistryObject<Item> FULL_GOLDEN_CHEST = armor("full_golden_chest", ArmorMaterials.GOLD, Type.CHESTPLATE, "full_golden");
   public static final RegistryObject<Item> FULL_DIAMOND_HEAD = armor("full_diamond_head", ArmorMaterials.DIAMOND, Type.HELMET, "full_diamond");
   public static final RegistryObject<Item> FULL_DIAMOND_CHEST = armor("full_diamond_chest", ArmorMaterials.DIAMOND, Type.CHESTPLATE, "full_diamond");
   public static final RegistryObject<Item> FULL_BRONZE_HEAD = armor("full_bronze_head", VCArmorMaterial.BRONZE, Type.HELMET, "full_bronze");
   public static final RegistryObject<Item> FULL_BRONZE_CHEST = armor("full_bronze_chest", VCArmorMaterial.BRONZE, Type.CHESTPLATE, "full_bronze");
   public static final RegistryObject<Item> FULL_BRONZE_LEGS = armor("full_bronze_legs", VCArmorMaterial.BRONZE, Type.LEGGINGS, "full_bronze");
   public static final RegistryObject<Item> FULL_BRONZE_BOOTS = armor("full_bronze_boots", VCArmorMaterial.BRONZE, Type.BOOTS, "full_bronze");
   public static final RegistryObject<Item> FULL_EMERALD_HEAD = armor("full_emerald_head", VCArmorMaterial.EMERALD, Type.HELMET, "full_emerald");
   public static final RegistryObject<Item> FULL_EMERALD_CHEST = armor("full_emerald_chest", VCArmorMaterial.EMERALD, Type.CHESTPLATE, "full_emerald");
   public static final RegistryObject<Item> FULL_EMERALD_LEGS = armor("full_emerald_legs", VCArmorMaterial.EMERALD, Type.LEGGINGS, "full_emerald");
   public static final RegistryObject<Item> FULL_EMERALD_BOOTS = armor("full_emerald_boots", VCArmorMaterial.EMERALD, Type.BOOTS, "full_emerald");
   public static final RegistryObject<Item> FULL_WOODEN_HEAD = armor("full_wooden_head", ArmorMaterials.LEATHER, Type.HELMET, "full_wooden");
   public static final RegistryObject<Item> FULL_WOODEN_CHEST = armor("full_wooden_chest", ArmorMaterials.LEATHER, Type.CHESTPLATE, "full_wooden");
   public static final RegistryObject<Item> FULL_WOODEN_LEGS = armor("full_wooden_legs", ArmorMaterials.LEATHER, Type.LEGGINGS, "full_wooden");
   public static final RegistryObject<Item> FULL_WOODEN_BOOTS = armor("full_wooden_boots", ArmorMaterials.LEATHER, Type.BOOTS, "full_wooden");
   public static final RegistryObject<Item> TUXEDO_CHEST = armor("tuxedo_chest", ArmorMaterials.CHAIN, Type.CHESTPLATE, "tuxedo");
   public static final RegistryObject<Item> TUXEDO_PANTS = armor("tuxedo_pants", ArmorMaterials.CHAIN, Type.LEGGINGS, "tuxedo");
   public static final RegistryObject<Item> TUXEDO_BOTTOM = armor("tuxedo_bottom", ArmorMaterials.CHAIN, Type.BOOTS, "tuxedo");
   public static final RegistryObject<Item> WIZARD_HEAD = armor("wizard_head", ArmorMaterials.CHAIN, Type.HELMET, "wizard");
   public static final RegistryObject<Item> WIZARD_CHEST = armor("wizard_chest", ArmorMaterials.CHAIN, Type.CHESTPLATE, "wizard");
   public static final RegistryObject<Item> WIZARD_PANTS = armor("wizard_pants", ArmorMaterials.CHAIN, Type.LEGGINGS, "wizard");
   public static final RegistryObject<Item> ASSASSIN_HEAD = armor("assassin_head", ArmorMaterials.IRON, Type.HELMET, "assassin");
   public static final RegistryObject<Item> ASSASSIN_CHEST = armor("assassin_chest", ArmorMaterials.IRON, Type.CHESTPLATE, "assassin");
   public static final RegistryObject<Item> ASSASSIN_LEGS = armor("assassin_legs", ArmorMaterials.IRON, Type.LEGGINGS, "assassin");
   public static final RegistryObject<Item> ASSASSIN_BOOTS = armor("assassin_boots", ArmorMaterials.IRON, Type.BOOTS, "assassin");
   public static final RegistryObject<Item> SOLDIER_HEAD = armor("soldier_head", ArmorMaterials.IRON, Type.HELMET, "soldier");
   public static final RegistryObject<Item> SOLDIER_CHEST = armor("soldier_chest", ArmorMaterials.IRON, Type.CHESTPLATE, "soldier");
   public static final RegistryObject<Item> SOLDIER_LEGS = armor("soldier_legs", ArmorMaterials.IRON, Type.LEGGINGS, "soldier");
   public static final RegistryObject<Item> SOLDIER_BOTTOM = armor("soldier_bottom", ArmorMaterials.IRON, Type.BOOTS, "soldier");
   public static final RegistryObject<Item> X407_HEAD = armor("x407_head", ArmorMaterials.DIAMOND, Type.HELMET, "x407");
   public static final RegistryObject<Item> X407_CHEST = armor("x407_chest", ArmorMaterials.DIAMOND, Type.CHESTPLATE, "x407");
   public static final RegistryObject<Item> X407_LEGS = armor("x407_legs", ArmorMaterials.DIAMOND, Type.LEGGINGS, "x407");
   public static final RegistryObject<Item> X407_BOOTS = armor("x407_boots", ArmorMaterials.DIAMOND, Type.BOOTS, "x407");
   public static final RegistryObject<Item> MITHRIL_HEAD = armor("mithril_head", VCArmorMaterial.MITHRIL, Type.HELMET, "mithril");
   public static final RegistryObject<Item> MITHRIL_CHEST = armor("mithril_chest", VCArmorMaterial.MITHRIL, Type.CHESTPLATE, "mithril");
   public static final RegistryObject<Item> MITHRIL_LEGS = armor("mithril_legs", VCArmorMaterial.MITHRIL, Type.LEGGINGS, "mithril");
   public static final RegistryObject<Item> MITHRIL_BOOTS = armor("mithril_boots", VCArmorMaterial.MITHRIL, Type.BOOTS, "mithril");
   public static final RegistryObject<Item> DEMONIC_HEAD = armor("demonic_head", ArmorMaterials.DIAMOND, Type.HELMET, "demonic");
   public static final RegistryObject<Item> DEMONIC_CHEST = armor("demonic_chest", ArmorMaterials.DIAMOND, Type.CHESTPLATE, "demonic");
   public static final RegistryObject<Item> DEMONIC_LEGS = armor("demonic_legs", ArmorMaterials.DIAMOND, Type.LEGGINGS, "demonic");
   public static final RegistryObject<Item> DEMONIC_BOOTS = armor("demonic_boots", ArmorMaterials.DIAMOND, Type.BOOTS, "demonic");
   public static final RegistryObject<Item> COMMISSAR_HEAD = armor("commissar_head", ArmorMaterials.GOLD, Type.HELMET, "commissar");
   public static final RegistryObject<Item> COMMISSAR_CHEST = armor("commissar_chest", ArmorMaterials.GOLD, Type.CHESTPLATE, "commissar");
   public static final RegistryObject<Item> COMMISSAR_LEGS = armor("commissar_legs", ArmorMaterials.GOLD, Type.LEGGINGS, "commissar");
   public static final RegistryObject<Item> COMMISSAR_BOTTOM = armor("commissar_bottom", ArmorMaterials.GOLD, Type.BOOTS, "commissar");
   public static final RegistryObject<Item> INFANTRY_HELMET = armor("infantry_helmet", ArmorMaterials.IRON, Type.HELMET, "infantry");
   public static final RegistryObject<Item> OFFICER_CHEST = armor("officer_chest", ArmorMaterials.DIAMOND, Type.CHESTPLATE, "officer");
   public static final RegistryObject<Item> CROWN = armor("crown", ArmorMaterials.DIAMOND, Type.HELMET, "crown2");
   public static final RegistryObject<Item> CROWN2 = armor("crown2", ArmorMaterials.DIAMOND, Type.HELMET, "crown1");
   public static final RegistryObject<Item> NINJA_HEAD = armor("ninja_head", ArmorMaterials.IRON, Type.HELMET, "ninja");
   public static final RegistryObject<Item> NINJA_CHEST = armor("ninja_chest", ArmorMaterials.IRON, Type.CHESTPLATE, "ninja");
   public static final RegistryObject<Item> NINJA_LEGS = armor("ninja_legs", ArmorMaterials.IRON, Type.LEGGINGS, "ninja");
   public static final RegistryObject<Item> CHAIN_SKIRT = skirt("chain_skirt", ArmorMaterials.CHAIN, "chainmail_layer");
   public static final RegistryObject<Item> LEATHER_SKIRT = skirt("leather_skirt", ArmorMaterials.LEATHER, "leather_layer");
   public static final RegistryObject<Item> GOLDEN_SKIRT = skirt("golden_skirt", ArmorMaterials.GOLD, "gold_layer");
   public static final RegistryObject<Item> IRON_SKIRT = skirt("iron_skirt", ArmorMaterials.IRON, "iron_layer");
   public static final RegistryObject<Item> DIAMOND_SKIRT = skirt("diamond_skirt", ArmorMaterials.DIAMOND, "diamond_layer");
   public static final RegistryObject<Item> EMERALD_SKIRT = skirt("emerald_skirt", VCArmorMaterial.EMERALD, "variedcommodities", "full_emerald");
   public static final RegistryObject<Item> BRONZE_SKIRT = skirt("bronze_skirt", VCArmorMaterial.BRONZE, "variedcommodities", "full_bronze");
   public static final RegistryObject<Item> DEMONIC_SKIRT = skirt("demonic_skirt", ArmorMaterials.DIAMOND, "variedcommodities", "demonic");
   public static final RegistryObject<Item> MITHRIL_SKIRT = skirt("mithril_skirt", VCArmorMaterial.MITHRIL, "variedcommodities", "mithril");

   private VCArmors() {
   }

   /** 由主类触碰以触发类初始化——所有注册都在 clinit 里发生。 */
   public static void init() {
   }

   /** 本页创造栏的展示顺序。 */
   public static List<RegistryObject<Item>> creativeOrder() {
      return Collections.unmodifiableList(ORDER);
   }


   // ── 注册辅助 ─────────────────────────────────────────────────────────

   private static RegistryObject<Item> armor(String name, ArmorMaterial material, Type type, String texture) {
      return VCItems.register(name, () -> new ItemArmorBasic(material, type, texture, single()), ORDER);
   }

   private static RegistryObject<Item> colorable(String name, ArmorMaterial material, Type type, String texture) {
      return VCItems.register(name, () -> new ItemArmorBasicColorable(material, type, texture, single()), ORDER);
   }

   private static RegistryObject<Item> skirt(String name, ArmorMaterial material, String texture) {
      return VCItems.register(name, () -> new ItemArmorSkirt(material, texture, single()), ORDER);
   }

   /** 带命名空间的重载：少数裙甲引用原版贴图（原作传 "minecraft"）。 */
   private static RegistryObject<Item> skirt(String name, ArmorMaterial material, String namespace, String texture) {
      return VCItems.register(name, () -> new ItemArmorSkirt(material, namespace, texture, single()), ORDER);
   }

}
