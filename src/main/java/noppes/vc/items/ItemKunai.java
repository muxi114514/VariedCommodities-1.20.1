package noppes.vc.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import noppes.vc.entity.EntityProjectile;
import noppes.vc.init.VCSounds;

/**
 * 苦无（原作 {@code ItemKunai}）：既能近战当短剑，又能按住右键掷出去。
 *
 * <p>与普通投掷武器的三点不同（全部照搬原作）：
 * <ul>
 * <li>伤害取自身的近战攻击力，不是固定值；</li>
 * <li>{@code destroyedOnEntityHit = false}——扎中生物后不消失，还会掉在地上；</li>
 * <li>掷出去的那把<b>带着当前耐久飞走</b>，落地捡回来还是那把。原作是先 {@code damageItem}
 *     再把当前槽位整个置空——苦无不可堆叠，所以"清空槽位"就等于"扔出这一把"；
 *     这里写成 {@code shrink(1)}，语义相同但不依赖"必定只有一个"这个前提。</li>
 * </ul>
 */
public class ItemKunai extends ItemWeaponBasic {

   /** {@code ItemWeaponBasic} 传给 SwordItem 的基础伤害就是 3。 */
   private static final float BASE_ATTACK_DAMAGE = 3.0F;

   public ItemKunai(Tier tier, int hitCost, Properties props) {
      super(tier, hitCost, props);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      return VCItemUtil.startCharging(level, player, hand);
   }

   @Override
   public int getUseDuration(ItemStack stack) {
      return VCItemUtil.HOLD_FOREVER;
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BOW;
   }

   @Override
   public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      if (level.isClientSide) {
         return;
      }
      ItemStack thrown = stack.copy();
      thrown.setCount(1);
      EntityProjectile projectile = new EntityProjectile(level, entity, thrown);
      projectile.damage = BASE_ATTACK_DAMAGE + getTier().getAttackDamageBonus();
      projectile.destroyedOnEntityHit = false;
      projectile.setIs3D(true);
      projectile.setStickInWall(true);
      projectile.setHasGravity(true);
      projectile.setSpeed(12);
      projectile.fire(1.0F);

      if (entity instanceof Player player) {
         boolean creative = player.getAbilities().instabuild;
         projectile.canBePickedUp = !creative;
         if (!creative) {
            // 掷出去的那把带着当前耐久飞走，手里这叠少一个
            thrown.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            projectile.setItemDisplay(thrown);
            stack.shrink(1);
         }
      }
      VCItemUtil.playSound(entity, VCSounds.MISC_SWOSH.get(), 1.0F, 1.0F);
      level.addFreshEntity(projectile);
   }
}
