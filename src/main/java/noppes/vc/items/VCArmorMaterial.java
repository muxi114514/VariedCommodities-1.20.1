package noppes.vc.items;

import java.util.EnumMap;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import noppes.vc.VariedCommodities;

/**
 * 护甲材质。对应 1.12 中用 {@code EnumHelper.addEnum} 追加到
 * {@code ItemArmor.ArmorMaterial} 上的三档（MITHRIL / BRONZE / EMERALD）。
 *
 * <p>原作参数形如 {@code {"", 耐久系数, {防御[4]}, 附魔性, 装备音效, 韧性}}。
 * 1.12 的实际耐久 = 耐久系数 × 每部位基数 {11, 16, 15, 13}，1.20.1 的
 * {@code getDurabilityForType} 同样这么算，故耐久系数可以逐字照搬。
 *
 * <p>1.12 的 {@code reductionAmounts} 数组按 {靴, 腿, 胸, 盔} 排列（与 armorType 索引一致），
 * 这里按 1.20.1 的 {@link ArmorItem.Type} 重新落位，不是简单照抄顺序。
 */
public enum VCArmorMaterial implements ArmorMaterial {
   MITHRIL("mithril", 40, new int[]{3, 6, 8, 3}, 20, SoundEvents.ARMOR_EQUIP_IRON, 2.0F),
   BRONZE("bronze", 7, new int[]{2, 5, 6, 2}, 20, SoundEvents.ARMOR_EQUIP_IRON, 0.0F),
   EMERALD("emerald", 35, new int[]{3, 6, 8, 3}, 5, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F);

   /** 1.20.1 每个部位的耐久基数，与 1.12 的 {11,16,15,13} 一致。 */
   private static final EnumMap<ArmorItem.Type, Integer> HEALTH_PER_SLOT = new EnumMap<>(ArmorItem.Type.class);

   static {
      HEALTH_PER_SLOT.put(ArmorItem.Type.BOOTS, 13);
      HEALTH_PER_SLOT.put(ArmorItem.Type.LEGGINGS, 15);
      HEALTH_PER_SLOT.put(ArmorItem.Type.CHESTPLATE, 16);
      HEALTH_PER_SLOT.put(ArmorItem.Type.HELMET, 11);
   }

   private final String name;
   private final int durabilityFactor;
   /** 索引沿用 1.12 的 {靴, 腿, 胸, 盔}。 */
   private final int[] protection;
   private final int enchantmentValue;
   private final SoundEvent equipSound;
   private final float toughness;

   VCArmorMaterial(String name, int durabilityFactor, int[] protection,
                   int enchantmentValue, SoundEvent equipSound, float toughness) {
      this.name = name;
      this.durabilityFactor = durabilityFactor;
      this.protection = protection;
      this.enchantmentValue = enchantmentValue;
      this.equipSound = equipSound;
      this.toughness = toughness;
   }

   private static int index(ArmorItem.Type type) {
      return switch (type) {
         case BOOTS -> 0;
         case LEGGINGS -> 1;
         case CHESTPLATE -> 2;
         case HELMET -> 3;
      };
   }

   @Override
   public int getDurabilityForType(ArmorItem.Type type) {
      return HEALTH_PER_SLOT.get(type) * this.durabilityFactor;
   }

   @Override
   public int getDefenseForType(ArmorItem.Type type) {
      return this.protection[index(type)];
   }

   @Override
   public int getEnchantmentValue() {
      return this.enchantmentValue;
   }

   @Override
   public SoundEvent getEquipSound() {
      return this.equipSound;
   }

   /** 原作未设修复材料，忠实保留。 */
   @Override
   public Ingredient getRepairIngredient() {
      return Ingredient.EMPTY;
   }

   @Override
   public String getName() {
      return VariedCommodities.MODID + ":" + this.name;
   }

   @Override
   public float getToughness() {
      return this.toughness;
   }

   @Override
   public float getKnockbackResistance() {
      return 0.0F;
   }
}
