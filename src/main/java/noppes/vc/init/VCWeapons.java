package noppes.vc.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.items.ItemShieldBasic;
import noppes.vc.items.ItemGun;
import noppes.vc.items.ItemKunai;
import noppes.vc.items.ItemSlingshot;
import noppes.vc.items.ItemStaff;
import noppes.vc.items.ItemStaffElemental;
import noppes.vc.items.ItemThrowingWeapon;
import noppes.vc.items.ItemCrossbowVC;
import noppes.vc.items.ItemHolyHandGrenade;
import noppes.vc.items.ItemMachineGun;
import noppes.vc.items.ItemMusket;
import noppes.vc.items.ItemWeaponBasic;
import noppes.vc.items.VCItemProps;
import noppes.vc.items.VCTier;

import static noppes.vc.items.VCItemProps.basic;
import static noppes.vc.items.VCItemProps.durable;
import static noppes.vc.items.VCItemProps.single;
import static noppes.vc.items.VCItemProps.tool;

/**
 * 武器注册（对应 1.12 的 VCWeapons，194 项）。
 * 
 * 近战武器全是剑类，伤害/攻速已对齐原作；盾牌耐久 = 材质耐久 ×5。
 * 枪械、法杖、投掷物、机枪、火枪、弩、圣手雷的机制均已落地（见 {@code noppes.vc.items}）。
 */
public final class VCWeapons {

   /** 展示顺序按字段声明顺序填充，即 1.12 的注册顺序。 */
   private static final List<RegistryObject<Item>> ORDER = new ArrayList<>();

   public static final RegistryObject<Item> BULLET = item("bullet", basic());
   public static final RegistryObject<Item> WOODEN_GUN = gun("wooden_gun", VCTier.WOOD);
   public static final RegistryObject<Item> STONE_GUN = gun("stone_gun", VCTier.STONE);
   public static final RegistryObject<Item> IRON_GUN = gun("iron_gun", VCTier.IRON);
   public static final RegistryObject<Item> GOLDEN_GUN = gun("golden_gun", VCTier.GOLD);
   public static final RegistryObject<Item> DIAMOND_GUN = gun("diamond_gun", VCTier.DIAMOND);
   public static final RegistryObject<Item> BRONZE_GUN = gun("bronze_gun", VCTier.BRONZE);
   public static final RegistryObject<Item> EMERALD_GUN = gun("emerald_gun", VCTier.EMERALD);
   public static final RegistryObject<Item> MACHINE_GUN =
           VCItems.register("machine_gun", () -> new ItemMachineGun(durable(80)), ORDER);
   public static final RegistryObject<Item> MANA = item("mana", basic());
   public static final RegistryObject<Item> WOODEN_STAFF =
           staff("wooden_staff", VCTier.WOOD, VCItems.SPELL_NATURE, 5, 12);
   public static final RegistryObject<Item> STONE_STAFF =
           staff("stone_staff", VCTier.STONE, VCItems.SPELL_DARK, 5649239, 4400964);
   public static final RegistryObject<Item> IRON_STAFF =
           staff("iron_staff", VCTier.IRON, VCItems.SPELL_HOLY, 16580553, 15728535);
   public static final RegistryObject<Item> GOLDEN_STAFF =
           staff("golden_staff", VCTier.GOLD, VCItems.SPELL_FIRE, 1, 14);
   public static final RegistryObject<Item> DIAMOND_STAFF =
           staff("diamond_staff", VCTier.DIAMOND, VCItems.SPELL_ICE, 9756653, 4503295);
   public static final RegistryObject<Item> BRONZE_STAFF =
           staff("bronze_staff", VCTier.BRONZE, VCItems.SPELL_LIGHTNING, 8648694, 6091007);
   public static final RegistryObject<Item> EMERALD_STAFF =
           staff("emerald_staff", VCTier.EMERALD, VCItems.SPELL_ARCANE, 16761831, 16487167);
   public static final RegistryObject<Item> DEMONIC_STAFF =
           staff("demonic_staff", VCTier.DEMONIC, VCItems.SPELL_DARK, 5649239, 4400964);
   public static final RegistryObject<Item> FROST_STAFF =
           staff("frost_staff", VCTier.FROST, VCItems.SPELL_ICE, 9756653, 4503295);
   public static final RegistryObject<Item> MITHRIL_STAFF =
           staff("mithril_staff", VCTier.MITHRIL, VCItems.SPELL_HOLY, 16580553, 15728535);
   /** 16 色变体：共用一张灰度贴图 + IItemColor 着色（非 16 张图），各射同色法球。 */
   public static final Map<DyeColor, RegistryObject<Item>> ELEMENTAL_STAFF =
           elementalStaffs();
   public static final RegistryObject<Item> WOODEN_TRIDENT = sword("wooden_trident", VCTier.WOOD, 1);
   public static final RegistryObject<Item> STONE_TRIDENT = sword("stone_trident", VCTier.STONE, 1);
   public static final RegistryObject<Item> IRON_TRIDENT = sword("iron_trident", VCTier.IRON, 1);
   public static final RegistryObject<Item> GOLDEN_TRIDENT = sword("golden_trident", VCTier.GOLD, 1);
   public static final RegistryObject<Item> DIAMOND_TRIDENT = sword("diamond_trident", VCTier.DIAMOND, 1);
   public static final RegistryObject<Item> BRONZE_TRIDENT = sword("bronze_trident", VCTier.BRONZE, 1);
   public static final RegistryObject<Item> EMERALD_TRIDENT = sword("emerald_trident", VCTier.EMERALD, 1);
   public static final RegistryObject<Item> CURSED_TRIDENT = sword("cursed_trident", VCTier.EMERALD, 1);
   public static final RegistryObject<Item> DEMONIC_TRIDENT = sword("demonic_trident", VCTier.DEMONIC, 1);
   public static final RegistryObject<Item> FROST_TRIDENT = sword("frost_trident", VCTier.FROST, 1);
   public static final RegistryObject<Item> MITHRIL_TRIDENT = sword("mithril_trident", VCTier.MITHRIL, 1);
   public static final RegistryObject<Item> KUNAI = kunai("kunai");
   public static final RegistryObject<Item> KUNAI_REVERSED = kunai("kunai_reversed");
   public static final RegistryObject<Item> SHURIKEN = throwing("shuriken", 2);
   public static final RegistryObject<Item> SHURIKEN_GIANT = throwing("shuriken_giant", 4);
   public static final RegistryObject<Item> KATANA = sword("katana", VCTier.IRON, 1);
   public static final RegistryObject<Item> KUKRI = sword("kukri", VCTier.IRON, 1);
   public static final RegistryObject<Item> NINJA_CLAW = sword("ninja_claw", VCTier.IRON, 1);
   public static final RegistryObject<Item> STEEL_CLAW = sword("steel_claw", VCTier.IRON, 1);
   public static final RegistryObject<Item> BEAR_CLAW = sword("bear_claw", VCTier.IRON, 1);
   public static final RegistryObject<Item> KATAR = sword("katar", VCTier.IRON, 1);
   public static final RegistryObject<Item> BRONZE_SWORD = sword("bronze_sword", VCTier.BRONZE, 1);
   public static final RegistryObject<Item> EMERALD_SWORD = sword("emerald_sword", VCTier.EMERALD, 1);
   public static final RegistryObject<Item> DEMONIC_SWORD = sword("demonic_sword", VCTier.DEMONIC, 1);
   public static final RegistryObject<Item> FROST_SWORD = sword("frost_sword", VCTier.FROST, 1);
   public static final RegistryObject<Item> MITHRIL_SWORD = sword("mithril_sword", VCTier.MITHRIL, 1);
   public static final RegistryObject<Item> LEAF_SWORD = sword("leaf_sword", VCTier.IRON, 1);
   public static final RegistryObject<Item> GOLF_CLUB = sword("golf_club", VCTier.IRON, 1);
   public static final RegistryObject<Item> HAMMER = sword("hammer", VCTier.STONE, 1);
   public static final RegistryObject<Item> BASEBALL_BAT = sword("baseball_bat", VCTier.IRON, 1);
   public static final RegistryObject<Item> LEAD_PIPE = sword("lead_pipe", VCTier.IRON, 1);
   public static final RegistryObject<Item> CLEAVER = sword("cleaver", VCTier.IRON, 1);
   public static final RegistryObject<Item> SABER = sword("saber", VCTier.IRON, 1);
   public static final RegistryObject<Item> HOCKEY_STICK = sword("hockey_stick", VCTier.IRON, 1);
   public static final RegistryObject<Item> SLEDGE_HAMMER = sword("sledge_hammer", VCTier.IRON, 1);
   public static final RegistryObject<Item> BROKEN_BOTTLE = sword("broken_bottle", VCTier.IRON, 1);
   public static final RegistryObject<Item> COMBAT_KNIVE = sword("combat_knive", VCTier.STONE, 1);
   public static final RegistryObject<Item> MACUAHUITL = sword("macuahuitl", VCTier.IRON, 1);
   public static final RegistryObject<Item> BO_STAFF = sword("bo_staff", VCTier.IRON, 1);
   public static final RegistryObject<Item> SAI = sword("sai", VCTier.IRON, 1);
   public static final RegistryObject<Item> SLINGSHOT =
           VCItems.register("slingshot", () -> new ItemSlingshot(durable(384)), ORDER);
   public static final RegistryObject<Item> MUSKET =
           VCItems.register("musket", () -> new ItemMusket(durable(129)), ORDER);
   public static final RegistryObject<Item> CROSSBOW =
           VCItems.register("crossbow", () -> new ItemCrossbowVC(durable(129)), ORDER);
   public static final RegistryObject<Item> BATTON = sword("batton", VCTier.STONE, 1);
   public static final RegistryObject<Item> CHAINSAW_GUN = sword("chainsaw_gun", VCTier.DIAMOND, 1);
   public static final RegistryObject<Item> RAPIER = sword("rapier", VCTier.IRON, 1);
   public static final RegistryObject<Item> CROWBAR = sword("crowbar", VCTier.IRON, 1);
   public static final RegistryObject<Item> PIPE_WRENCH = sword("pipe_wrench", VCTier.IRON, 1);
   public static final RegistryObject<Item> SWISS_ARMY_KNIFE = sword("swiss_army_knife", VCTier.IRON, 1);
   public static final RegistryObject<Item> WRENCH = sword("wrench", VCTier.IRON, 1);
   public static final RegistryObject<Item> EXCALIBUR = sword("excalibur", VCTier.DIAMOND, 1);
   public static final RegistryObject<Item> WOODEN_SHIELD_ROUND = shield("wooden_shield_round", VCTier.WOOD);
   public static final RegistryObject<Item> STONE_SHIELD_ROUND = shield("stone_shield_round", VCTier.STONE);
   public static final RegistryObject<Item> IRON_SHIELD_ROUND = shield("iron_shield_round", VCTier.IRON);
   public static final RegistryObject<Item> GOLDEN_SHIELD_ROUND = shield("golden_shield_round", VCTier.GOLD);
   public static final RegistryObject<Item> DIAMOND_SHIELD_ROUND = shield("diamond_shield_round", VCTier.DIAMOND);
   public static final RegistryObject<Item> BRONZE_SHIELD_ROUND = shield("bronze_shield_round", VCTier.BRONZE);
   public static final RegistryObject<Item> EMERALD_SHIELD_ROUND = shield("emerald_shield_round", VCTier.EMERALD);
   public static final RegistryObject<Item> DEMONIC_SHIELD_ROUND = shield("demonic_shield_round", VCTier.DEMONIC);
   public static final RegistryObject<Item> MITHRIL_SHIELD_ROUND = shield("mithril_shield_round", VCTier.MITHRIL);
   public static final RegistryObject<Item> WOODEN_SHIELD = shield("wooden_shield", VCTier.WOOD);
   public static final RegistryObject<Item> STONE_SHIELD = shield("stone_shield", VCTier.STONE);
   public static final RegistryObject<Item> IRON_SHIELD = shield("iron_shield", VCTier.IRON);
   public static final RegistryObject<Item> GOLDEN_SHIELD = shield("golden_shield", VCTier.GOLD);
   public static final RegistryObject<Item> DIAMOND_SHIELD = shield("diamond_shield", VCTier.DIAMOND);
   public static final RegistryObject<Item> BRONZE_SHIELD = shield("bronze_shield", VCTier.BRONZE);
   public static final RegistryObject<Item> EMERALD_SHIELD = shield("emerald_shield", VCTier.EMERALD);
   public static final RegistryObject<Item> FROST_SHIELD = shield("frost_shield", VCTier.FROST);
   public static final RegistryObject<Item> HEATER_SHIELD = shield("heater_shield", VCTier.IRON);
   public static final RegistryObject<Item> CRESCENT_SHIELD = shield("crescent_shield", VCTier.IRON);
   public static final RegistryObject<Item> SCUTUM_SHIELD = shield("scutum_shield", VCTier.IRON);
   public static final RegistryObject<Item> TOWER_SHIELD = shield("tower_shield", VCTier.IRON);
   public static final RegistryObject<Item> WOODEN_SPEAR = sword("wooden_spear", VCTier.WOOD, 2);
   public static final RegistryObject<Item> STONE_SPEAR = sword("stone_spear", VCTier.STONE, 2);
   public static final RegistryObject<Item> IRON_SPEAR = sword("iron_spear", VCTier.IRON, 2);
   public static final RegistryObject<Item> GOLDEN_SPEAR = sword("golden_spear", VCTier.GOLD, 2);
   public static final RegistryObject<Item> DIAMOND_SPEAR = sword("diamond_spear", VCTier.DIAMOND, 2);
   public static final RegistryObject<Item> BRONZE_SPEAR = sword("bronze_spear", VCTier.BRONZE, 2);
   public static final RegistryObject<Item> EMERALD_SPEAR = sword("emerald_spear", VCTier.EMERALD, 2);
   public static final RegistryObject<Item> DEMONIC_SPEAR = sword("demonic_spear", VCTier.DEMONIC, 2);
   public static final RegistryObject<Item> FROST_SPEAR = sword("frost_spear", VCTier.FROST, 2);
   public static final RegistryObject<Item> MITHRIL_SPEAR = sword("mithril_spear", VCTier.MITHRIL, 2);
   public static final RegistryObject<Item> WOODEN_HALBERD = sword("wooden_halberd", VCTier.WOOD, 1);
   public static final RegistryObject<Item> STONE_HALBERD = sword("stone_halberd", VCTier.STONE, 1);
   public static final RegistryObject<Item> IRON_HALBERD = sword("iron_halberd", VCTier.IRON, 1);
   public static final RegistryObject<Item> GOLDEN_HALBERD = sword("golden_halberd", VCTier.GOLD, 1);
   public static final RegistryObject<Item> DIAMOND_HALBERD = sword("diamond_halberd", VCTier.DIAMOND, 1);
   public static final RegistryObject<Item> BRONZE_HALBERD = sword("bronze_halberd", VCTier.BRONZE, 1);
   public static final RegistryObject<Item> EMERALD_HALBERD = sword("emerald_halberd", VCTier.EMERALD, 1);
   public static final RegistryObject<Item> DEMONIC_HALBERD = sword("demonic_halberd", VCTier.DEMONIC, 1);
   public static final RegistryObject<Item> FROST_HALBERD = sword("frost_halberd", VCTier.FROST, 1);
   public static final RegistryObject<Item> MITHRIL_HALBERD = sword("mithril_halberd", VCTier.MITHRIL, 1);
   public static final RegistryObject<Item> WOODEN_BATTLEAXE = sword("wooden_battleaxe", VCTier.WOOD, 1);
   public static final RegistryObject<Item> STONE_BATTLEAXE = sword("stone_battleaxe", VCTier.STONE, 1);
   public static final RegistryObject<Item> IRON_BATTLEAXE = sword("iron_battleaxe", VCTier.IRON, 1);
   public static final RegistryObject<Item> GOLDEN_BATTLEAXE = sword("golden_battleaxe", VCTier.GOLD, 1);
   public static final RegistryObject<Item> DIAMOND_BATTLEAXE = sword("diamond_battleaxe", VCTier.DIAMOND, 1);
   public static final RegistryObject<Item> BRONZE_BATTLEAXE = sword("bronze_battleaxe", VCTier.BRONZE, 1);
   public static final RegistryObject<Item> EMERALD_BATTLEAXE = sword("emerald_battleaxe", VCTier.EMERALD, 1);
   public static final RegistryObject<Item> DEMONIC_BATTLEAXE = sword("demonic_battleaxe", VCTier.DEMONIC, 1);
   public static final RegistryObject<Item> FROST_BATTLEAXE = sword("frost_battleaxe", VCTier.FROST, 1);
   public static final RegistryObject<Item> MITHRIL_BATTLEAXE = sword("mithril_battleaxe", VCTier.MITHRIL, 1);
   public static final RegistryObject<Item> WOODEN_WARHAMMER = sword("wooden_warhammer", VCTier.WOOD, 1);
   public static final RegistryObject<Item> STONE_WARHAMMER = sword("stone_warhammer", VCTier.STONE, 1);
   public static final RegistryObject<Item> IRON_WARHAMMER = sword("iron_warhammer", VCTier.IRON, 1);
   public static final RegistryObject<Item> GOLDEN_WARHAMMER = sword("golden_warhammer", VCTier.GOLD, 1);
   public static final RegistryObject<Item> DIAMOND_WARHAMMER = sword("diamond_warhammer", VCTier.DIAMOND, 1);
   public static final RegistryObject<Item> BRONZE_WARHAMMER = sword("bronze_warhammer", VCTier.BRONZE, 1);
   public static final RegistryObject<Item> EMERALD_WARHAMMER = sword("emerald_warhammer", VCTier.EMERALD, 1);
   public static final RegistryObject<Item> DEMONIC_WARHAMMER = sword("demonic_warhammer", VCTier.DEMONIC, 1);
   public static final RegistryObject<Item> FROST_WARHAMMER = sword("frost_warhammer", VCTier.FROST, 1);
   public static final RegistryObject<Item> MITHRIL_WARHAMMER = sword("mithril_warhammer", VCTier.MITHRIL, 1);
   public static final RegistryObject<Item> WOODEN_MACE = sword("wooden_mace", VCTier.WOOD, 1);
   public static final RegistryObject<Item> STONE_MACE = sword("stone_mace", VCTier.STONE, 1);
   public static final RegistryObject<Item> IRON_MACE = sword("iron_mace", VCTier.IRON, 1);
   public static final RegistryObject<Item> GOLDEN_MACE = sword("golden_mace", VCTier.GOLD, 1);
   public static final RegistryObject<Item> DIAMOND_MACE = sword("diamond_mace", VCTier.DIAMOND, 1);
   public static final RegistryObject<Item> BRONZE_MACE = sword("bronze_mace", VCTier.BRONZE, 1);
   public static final RegistryObject<Item> EMERALD_MACE = sword("emerald_mace", VCTier.EMERALD, 1);
   public static final RegistryObject<Item> DEMONIC_MACE = sword("demonic_mace", VCTier.DEMONIC, 1);
   public static final RegistryObject<Item> FROST_MACE = sword("frost_mace", VCTier.FROST, 1);
   public static final RegistryObject<Item> MITHRIL_MACE = sword("mithril_mace", VCTier.MITHRIL, 1);
   public static final RegistryObject<Item> WOODEN_DAGGER = sword("wooden_dagger", VCTier.WOOD, 2);
   public static final RegistryObject<Item> WOODEN_DAGGER_REVERSED = sword("wooden_dagger_reversed", VCTier.WOOD, 2);
   public static final RegistryObject<Item> STONE_DAGGER = sword("stone_dagger", VCTier.STONE, 2);
   public static final RegistryObject<Item> STONE_DAGGER_REVERSED = sword("stone_dagger_reversed", VCTier.STONE, 2);
   public static final RegistryObject<Item> IRON_DAGGER = sword("iron_dagger", VCTier.IRON, 2);
   public static final RegistryObject<Item> IRON_DAGGER_REVERSED = sword("iron_dagger_reversed", VCTier.IRON, 2);
   public static final RegistryObject<Item> GOLDEN_DAGGER = sword("golden_dagger", VCTier.GOLD, 2);
   public static final RegistryObject<Item> GOLDEN_DAGGER_REVERSED = sword("golden_dagger_reversed", VCTier.GOLD, 2);
   public static final RegistryObject<Item> DIAMOND_DAGGER = sword("diamond_dagger", VCTier.DIAMOND, 2);
   public static final RegistryObject<Item> DIAMOND_DAGGER_REVERSED = sword("diamond_dagger_reversed", VCTier.DIAMOND, 2);
   public static final RegistryObject<Item> BRONZE_DAGGER = sword("bronze_dagger", VCTier.BRONZE, 2);
   public static final RegistryObject<Item> BRONZE_DAGGER_REVERSED = sword("bronze_dagger_reversed", VCTier.BRONZE, 2);
   public static final RegistryObject<Item> EMERALD_DAGGER = sword("emerald_dagger", VCTier.EMERALD, 2);
   public static final RegistryObject<Item> EMERALD_DAGGER_REVERSED = sword("emerald_dagger_reversed", VCTier.EMERALD, 2);
   public static final RegistryObject<Item> DEMONIC_DAGGER = sword("demonic_dagger", VCTier.DEMONIC, 2);
   public static final RegistryObject<Item> DEMONIC_DAGGER_REVERSED = sword("demonic_dagger_reversed", VCTier.DEMONIC, 2);
   public static final RegistryObject<Item> FROST_DAGGER = sword("frost_dagger", VCTier.FROST, 2);
   public static final RegistryObject<Item> FROST_DAGGER_REVERSED = sword("frost_dagger_reversed", VCTier.FROST, 2);
   public static final RegistryObject<Item> MITHRIL_DAGGER = sword("mithril_dagger", VCTier.MITHRIL, 2);
   public static final RegistryObject<Item> MITHRIL_DAGGER_REVERSED = sword("mithril_dagger_reversed", VCTier.FROST, 2);
   public static final RegistryObject<Item> WOODEN_SCYTHE = sword("wooden_scythe", VCTier.WOOD, 1);
   public static final RegistryObject<Item> STONE_SCYTHE = sword("stone_scythe", VCTier.STONE, 1);
   public static final RegistryObject<Item> IRON_SCYTHE = sword("iron_scythe", VCTier.IRON, 1);
   public static final RegistryObject<Item> GOLDEN_SCYTHE = sword("golden_scythe", VCTier.GOLD, 1);
   public static final RegistryObject<Item> DIAMOND_SCYTHE = sword("diamond_scythe", VCTier.DIAMOND, 1);
   public static final RegistryObject<Item> BRONZE_SCYTHE = sword("bronze_scythe", VCTier.BRONZE, 1);
   public static final RegistryObject<Item> EMERALD_SCYTHE = sword("emerald_scythe", VCTier.EMERALD, 1);
   public static final RegistryObject<Item> DEMONIC_SCYTHE = sword("demonic_scythe", VCTier.DEMONIC, 1);
   public static final RegistryObject<Item> FROST_SCYTHE = sword("frost_scythe", VCTier.FROST, 1);
   public static final RegistryObject<Item> MITHRIL_SCYTHE = sword("mithril_scythe", VCTier.MITHRIL, 1);
   public static final RegistryObject<Item> WOODEN_GLAIVE = sword("wooden_glaive", VCTier.WOOD, 1);
   public static final RegistryObject<Item> STONE_GLAIVE = sword("stone_glaive", VCTier.STONE, 1);
   public static final RegistryObject<Item> IRON_GLAIVE = sword("iron_glaive", VCTier.IRON, 1);
   public static final RegistryObject<Item> GOLDEN_GLAIVE = sword("golden_glaive", VCTier.GOLD, 1);
   public static final RegistryObject<Item> DIAMOND_GLAIVE = sword("diamond_glaive", VCTier.DIAMOND, 1);
   public static final RegistryObject<Item> BRONZE_GLAIVE = sword("bronze_glaive", VCTier.BRONZE, 1);
   public static final RegistryObject<Item> EMERALD_GLAIVE = sword("emerald_glaive", VCTier.EMERALD, 1);
   public static final RegistryObject<Item> DEMONIC_GLAIVE = sword("demonic_glaive", VCTier.DEMONIC, 1);
   public static final RegistryObject<Item> FROST_GLAIVE = sword("frost_glaive", VCTier.FROST, 1);
   public static final RegistryObject<Item> MITHRIL_GLAIVE = sword("mithril_glaive", VCTier.MITHRIL, 1);
   public static final RegistryObject<Item> WOODEN_BROADSWORD = sword("wooden_broadsword", VCTier.WOOD, 1);
   public static final RegistryObject<Item> STONE_BROADSWORD = sword("stone_broadsword", VCTier.STONE, 1);
   public static final RegistryObject<Item> IRON_BROADSWORD = sword("iron_broadsword", VCTier.IRON, 1);
   public static final RegistryObject<Item> GOLDEN_BROADSWORD = sword("golden_broadsword", VCTier.GOLD, 1);
   public static final RegistryObject<Item> DIAMOND_BROADSWORD = sword("diamond_broadsword", VCTier.DIAMOND, 1);
   public static final RegistryObject<Item> BRONZE_BROADSWORD = sword("bronze_broadsword", VCTier.BRONZE, 1);
   public static final RegistryObject<Item> EMERALD_BROADSWORD = sword("emerald_broadsword", VCTier.EMERALD, 1);
   public static final RegistryObject<Item> DEMONIC_BROADSWORD = sword("demonic_broadsword", VCTier.DEMONIC, 1);
   public static final RegistryObject<Item> FROST_BROADSWORD = sword("frost_broadsword", VCTier.FROST, 1);
   public static final RegistryObject<Item> MITHRIL_BROADSWORD = sword("mithril_broadsword", VCTier.MITHRIL, 1);
   public static final RegistryObject<Item> HOLYHANDGRENADE =
           VCItems.register("holyhandgrenade", () -> new ItemHolyHandGrenade(basic()), ORDER);

   private VCWeapons() {
   }

   /** 由主类触碰以触发类初始化——所有注册都在 clinit 里发生。 */
   public static void init() {
   }

   /** 本页创造栏的展示顺序。 */
   public static List<RegistryObject<Item>> creativeOrder() {
      return Collections.unmodifiableList(ORDER);
   }


   // ── 注册辅助 ─────────────────────────────────────────────────────────

   private static RegistryObject<Item> item(String name, Item.Properties props) {
      return VCItems.register(name, () -> new Item(props), ORDER);
   }



   private static RegistryObject<Item> gun(String name, VCTier tier) {
      return VCItems.register(name, () -> new ItemGun(tier, tool(tier)), ORDER);
   }

   /** 法杖：法术物品与粒子色取自原作的 getProjectile / spawnParticle。 */
   private static RegistryObject<Item> staff(String name, VCTier tier,
                                             RegistryObject<Item> spell, int colorA, int colorB) {
      return VCItems.register(name,
              () -> new ItemStaff(tier, spell::get, colorA, colorB, single()), ORDER);
   }

   /** 元素法杖：16 色各射同色法球，材质固定铁（原作 VCToolMaterial.IRON）。 */
   private static Map<DyeColor, RegistryObject<Item>> elementalStaffs() {
      EnumMap<DyeColor, RegistryObject<Item>> map = new EnumMap<>(DyeColor.class);
      for (int meta = 0; meta < 16; meta++) {
         DyeColor color = DyeColor.byId(15 - meta);
         RegistryObject<Item> orb = VCItems.ORB.get(color);
         map.put(color, VCItems.register("elemental_staff_" + color.getSerializedName(),
                 () -> new ItemStaffElemental(VCTier.IRON, color, orb::get, single()), ORDER));
      }
      return Collections.unmodifiableMap(map);
   }

   /**
    * 苦无：近战剑 + 可投掷。
    *
    * <p>⚠️ 原作那句 {@code setMaxStackSize(1)} 不能照抄：1.20.1 的 {@code durability(n)}
    * 本身就把堆叠上限设成 1，再调 {@code stacksTo(1)} 会直接抛
    * {@code Unable to have damage AND stack}。
    */
   private static RegistryObject<Item> kunai(String name) {
      return VCItems.register(name,
              () -> new ItemKunai(VCTier.IRON, 1, tool(VCTier.IRON)), ORDER);
   }

   /** 投掷武器：原作三件都是 setRotating().setDropItem()。 */
   private static RegistryObject<Item> throwing(String name, int damage) {
      return VCItems.register(name,
              () -> new ItemThrowingWeapon(damage, true, true, basic()), ORDER);
   }

   private static RegistryObject<Item> sword(String name, VCTier tier, int hitCost) {
      return VCItems.register(name, () -> new ItemWeaponBasic(tier, hitCost, tool(tier)), ORDER);
   }

   private static RegistryObject<Item> shield(String name, VCTier tier) {
      return VCItems.register(name, () -> new ItemShieldBasic(VCItemProps.shield(tier)), ORDER);
   }

}
