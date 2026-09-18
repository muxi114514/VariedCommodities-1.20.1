package noppes.vc.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

/**
 * VC 的近战武器，全部是剑类。
 *
 * <p>伤害对齐：1.12 的 {@code ItemSword} 取 {@code 3 + material.getDamageVsEntity()}，
 * 1.20.1 的 {@link SwordItem} 取 {@code attackDamageModifier + tier.getAttackDamageBonus()}，
 * 因此传 3 即等价；攻速 -2.4F 也与 1.12 的剑一致。
 *
 * <p>部分武器每次命中掉 2 点耐久而非 1（原作 {@code ItemWeaponBasic(material, damage)} 的第二个参数），
 * 对应 {@link #hitCost}。
 */
public class ItemWeaponBasic extends SwordItem {

   private static final int BASE_DAMAGE = 3;
   private static final float ATTACK_SPEED = -2.4F;

   private final int hitCost;

   public ItemWeaponBasic(Tier tier, int hitCost, Properties props) {
      super(tier, BASE_DAMAGE, ATTACK_SPEED, props);
      this.hitCost = hitCost;
   }

   @Override
   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      stack.hurtAndBreak(this.hitCost, attacker, user -> user.broadcastBreakEvent(net.minecraft.world.entity.EquipmentSlot.MAINHAND));
      return true;
   }
}
