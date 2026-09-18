package noppes.vc.items;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 工具/武器材质档。对应 1.12 的 {@code Item.ToolMaterial}（含 EnumHelper 动态追加的五档）
 * 与 {@code VCToolMaterial}。
 *
 * <p>原作有两张几乎相同的表：武器用 {@code EnumHelper.addToolMaterial} 造的，
 * 盾/枪/法杖用 {@code VCToolMaterial} 枚举。两表只有两处数值不一致——
 * {@code GOLD} 的伤害（1 vs 0）和 {@code DEMONIC} 的附魔性（10 vs 22）。
 * 前者只被 VCToolMaterial 侧使用而那一侧根本不读伤害，后者影响的是盾牌附魔。
 * 这里合并成一张表，取武器侧（EnumHelper）的数值。
 *
 * <p>五个原版档的数值与 1.20.1 的 {@link net.minecraft.world.item.Tiers} 完全一致，
 * 一并放进来是为了让盾/枪的耐久计算（{@code material.getMaxUses()}）有统一出处。
 *
 * <p><b>修复材料为空</b>：原作没给这些材质设 {@code setRepairItem}，因此铁砧修不了。
 * 此处忠实保留；若要改，把 {@link #getRepairIngredient()} 指向对应锭即可。
 */
public enum VCTier implements Tier {
   WOOD(0, 59, 2.0F, 0.0F, 15),
   STONE(1, 131, 4.0F, 1.0F, 5),
   IRON(2, 250, 6.0F, 2.0F, 14),
   DIAMOND(3, 1561, 8.0F, 3.0F, 10),
   GOLD(0, 32, 12.0F, 0.0F, 22),
   BRONZE(2, 170, 5.0F, 2.0F, 15),
   EMERALD(3, 1000, 8.0F, 4.0F, 10),
   DEMONIC(3, 100, 8.0F, 6.0F, 22),
   FROST(2, 59, 6.0F, 3.0F, 5),
   MITHRIL(3, 3000, 8.0F, 3.0F, 10);

   private final int level;
   private final int uses;
   private final float speed;
   private final float attackDamageBonus;
   private final int enchantmentValue;

   VCTier(int level, int uses, float speed, float attackDamageBonus, int enchantmentValue) {
      this.level = level;
      this.uses = uses;
      this.speed = speed;
      this.attackDamageBonus = attackDamageBonus;
      this.enchantmentValue = enchantmentValue;
   }

   @Override
   public int getUses() {
      return this.uses;
   }

   @Override
   public float getSpeed() {
      return this.speed;
   }

   @Override
   public float getAttackDamageBonus() {
      return this.attackDamageBonus;
   }

   @Override
   public int getLevel() {
      return this.level;
   }

   @Override
   public int getEnchantmentValue() {
      return this.enchantmentValue;
   }

   @Override
   public Ingredient getRepairIngredient() {
      return Ingredient.EMPTY;
   }
}
