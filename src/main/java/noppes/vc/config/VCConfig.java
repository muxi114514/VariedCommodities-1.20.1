package noppes.vc.config;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraftforge.common.ForgeConfigSpec;
import noppes.vc.blocks.storage.StorageTier;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 1.12 的自定义 @ConfigProp + ConfigLoader 在 1.20.1 由 ForgeConfigSpec 取代：
 * 配置值是惰性的 ConfigValue，必须在配置加载后再取，不能在静态初始化期读。
 * 数值类配置项与取值范围与 1.12 原版保持一致。
 *
 * <p><b>两条与时机有关的注意事项：</b>
 * <ol>
 * <li>附魔要不要注册这件事<b>读不到这里</b>——注册事件早于 Forge 加载配置。
 *     那一项由 {@link VCEarlyConfig} 在 mod 构造期直接读同一个 toml 文件，
 *     两者指向同一个键 {@code general.registerEnchantments}，不是两份真相。</li>
 * <li>配方开关<b>可以</b>读这里：数据包加载远晚于配置加载，
 *     Forge 的配方条件（{@code RecipeEnabledCondition}）在那时才求值。</li>
 * </ol>
 */
public final class VCConfig {

   public static final ForgeConfigSpec SPEC;
   private static final int DEFAULT_UPGRADE_COST = 4;
   private static final Common COMMON;

   public static final class Common {
      public final ForgeConfigSpec.BooleanValue registerEnchantments;
      public final ForgeConfigSpec.BooleanValue disableRecipes;
      public final ForgeConfigSpec.IntValue machineGunDamage;
      public final ForgeConfigSpec.IntValue musketDamage;
      public final ForgeConfigSpec.IntValue musketKnockback;
      public final ForgeConfigSpec.DoubleValue holyHandGrenadeStrength;
      public final ForgeConfigSpec.BooleanValue crateUpgrades;
      /** 升级到各档所需的材料数；木质档不是升级目标，不在表中。 */
      public final Map<StorageTier, ForgeConfigSpec.IntValue> crateUpgradeCosts;
      /** 配方名 → 开关，逐条对应 {@link VCRecipeNames#ALL}。 */
      public final Map<String, ForgeConfigSpec.BooleanValue> recipes;

      Common(ForgeConfigSpec.Builder b) {
         b.push("general");
         this.registerEnchantments = b
                 .comment("Register Varied Commodities' four enchantments (damage/poison/confusion/infinite).",
                         "Default off: they only apply to VC guns and staffs and would otherwise",
                         "dilute the enchanting table for everyone else.",
                         "Takes effect on restart -- registration happens once at startup.")
                 .define("registerEnchantments", false);
         this.disableRecipes = b
                 .comment("Master switch: disables every Varied Commodities recipe,",
                         "regardless of the per-recipe toggles below.")
                 .define("disableRecipes", false);
         this.machineGunDamage = b.comment("Damage from Machine Gun")
                 .defineInRange("machineGunDamage", 4, 0, 1000);
         this.musketDamage = b.comment("Damage from Musket")
                 .defineInRange("musketDamage", 16, 0, 1000);
         this.musketKnockback = b.comment("Bonus knockback from Musket")
                 .defineInRange("musketKnockback", 0, 0, 100);
         this.holyHandGrenadeStrength = b.comment("Holy Hand Grenade Explosion Strength")
                 .defineInRange("holyHandGrenadeStrength", 10.0D, 0.0D, 100.0D);
         b.pop();

         b.comment("Crate upgrades (an addition of this port, not in the original mod):",
                         "sneak + right-click a crate while holding the material of a higher tier",
                         "to upgrade it in place. Contents are kept and tiers may be skipped.",
                         "Capacities are fixed on purpose -- they are part of the save data.")
                 .push("crateUpgrades");
         this.crateUpgrades = b.comment("Allow upgrading crates.")
                 .define("enabled", true);
         Map<StorageTier, ForgeConfigSpec.IntValue> costs = new EnumMap<>(StorageTier.class);
         for (StorageTier tier : StorageTier.values()) {
            if (tier.material() != null) {
               costs.put(tier, b.comment("Materials consumed when upgrading a crate to the "
                               + tier.getSerializedName() + " tier (" + tier.size() + " slots).")
                       .defineInRange(tier.getSerializedName() + "Cost", DEFAULT_UPGRADE_COST, 1, 64));
            }
         }
         this.crateUpgradeCosts = Collections.unmodifiableMap(costs);
         b.pop();

         b.comment("One switch per recipe. Turning one off means that recipe is never loaded",
                         "at all -- it does not show up in JEI or the recipe book either.",
                         "Takes effect on the next datapack reload (/reload or rejoining the world).",
                         "",
                         "NOTE: everything defaults to OFF except the carpentry bench and the",
                         "crates. Varied Commodities is mostly a decoration mod, so recipes are",
                         "opt-in -- turn on whatever you want.",
                         "Most VC recipes are larger than 3x3 and can only be made at the carpentry",
                         "bench; the bench itself is 3x3 and works on a normal crafting table.")
                 .push("recipes");
         Map<String, ForgeConfigSpec.BooleanValue> map = new LinkedHashMap<>();
         for (String name : VCRecipeNames.ALL) {
            map.put(name, b.define(name, VCRecipeNames.DEFAULT_ON.contains(name)));
         }
         this.recipes = Collections.unmodifiableMap(map);
         b.pop();
      }
   }

   static {
      Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Common::new);
      COMMON = pair.getLeft();
      SPEC = pair.getRight();
   }

   private VCConfig() {
   }

   /**
    * 这条配方要不要装进配方表。
    *
    * <p>配置没加载时一律放行：那说明求值时机比预期早了，宁可多给配方也不要凭空少东西。
    */
   public static boolean recipeEnabled(String name) {
      if (!SPEC.isLoaded()) {
         return true;
      }
      if (COMMON.disableRecipes.get()) {
         return false;
      }
      ForgeConfigSpec.BooleanValue value = COMMON.recipes.get(name);
      return value == null || value.get();
   }

   public static int machineGunDamage() {
      return COMMON.machineGunDamage.get();
   }

   public static int musketDamage() {
      return COMMON.musketDamage.get();
   }

   public static int musketKnockback() {
      return COMMON.musketKnockback.get();
   }

   public static float holyHandGrenadeStrength() {
      return COMMON.holyHandGrenadeStrength.get().floatValue();
   }

   /** 升级只发生在进世界之后，那时配置早已加载；未加载时按默认值（开启）处理。 */
   public static boolean crateUpgradesEnabled() {
      return !SPEC.isLoaded() || COMMON.crateUpgrades.get();
   }

   public static int crateUpgradeCost(StorageTier tier) {
      ForgeConfigSpec.IntValue cost = COMMON.crateUpgradeCosts.get(tier);
      return cost == null || !SPEC.isLoaded() ? DEFAULT_UPGRADE_COST : cost.get();
   }
}
