package noppes.vc.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import noppes.vc.entity.EntityProjectile;
import noppes.vc.init.VCEnchants;
import noppes.vc.init.VCSounds;
import noppes.vc.init.VCWeapons;

/**
 * 枪械（原作 {@code ItemGun}）：按住右键上膛，第 8 刻响一声扳机，满 10 刻后松手才打得出去。
 *
 * <p>每发消耗一颗 {@code bullet}（附了「无限」就不耗）。伤害 {@code (材质伤害+1)/2 + 5}，
 * 「伤害」附魔每级 +50%；「中毒」「混乱」附魔由子弹带到目标身上。
 *
 * <p>⚠️ 中毒/混乱的施加方式变了：原作在投射物上挂一个捕获了枪 ItemStack 的匿名回调，
 * 这里改成把等级写进投射物自己的字段（见 {@link EntityProjectile}），存盘也不丢。
 */
public class ItemGun extends Item {

   /** 蓄力不足这么多刻就松手＝没打出去，原作的判定值。 */
   private static final int MIN_CHARGE = 10;
   private static final int TRIGGER_TICK = 8;

   private final VCTier tier;

   public ItemGun(VCTier tier, Properties props) {
      super(props);
      this.tier = tier;
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
   public boolean isEnchantable(ItemStack stack) {
      return true;
   }

   @Override
   public int getEnchantmentValue() {
      return this.tier.getEnchantmentValue();
   }

   @Override
   public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
      if (!level.isClientSide && getUseDuration(stack) - remaining == TRIGGER_TICK) {
         VCItemUtil.playSound(entity, VCSounds.GUN_PISTOL_TRIGGER.get(), 1.0F,
                 1.0F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
      }
   }

   @Override
   public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      if (level.isClientSide) {
         return;
      }
      if (entity instanceof Player player) {
         if (!hasBullet(player, stack)) {
            VCItemUtil.playSound(player, VCSounds.GUN_EMPTY.get(), 1.0F, 1.0F);
            return;
         }
         if (getUseDuration(stack) - timeLeft < MIN_CHARGE) {
            return;
         }
         stack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
         if (!player.getAbilities().instabuild && !VCEnchants.infinite(stack)) {
            VCItemUtil.consumeItem(player, VCWeapons.BULLET.get());
         }
      }

      int damage = (int) ((this.tier.getAttackDamageBonus() + 1.0F) / 2.0F) + 5;
      damage = (int) (damage + damage * VCEnchants.damage(stack) * 0.5F);

      EntityProjectile bullet = new EntityProjectile(level, entity, new ItemStack(VCWeapons.BULLET.get()));
      bullet.damage = damage;
      bullet.poison = VCEnchants.poison(stack);
      bullet.confusion = VCEnchants.confusion(stack);
      bullet.setSpeed(40);
      bullet.fire(this.tier.getAttackDamageBonus() + 1.0F);
      VCItemUtil.playSound(entity, VCSounds.GUN_PISTOL_SHOT.get(), 1.0F,
              level.getRandom().nextFloat() * 0.3F + 0.8F);
      level.addFreshEntity(bullet);
   }

   private boolean hasBullet(Player player, ItemStack stack) {
      return player.getAbilities().instabuild || VCEnchants.infinite(stack)
              || VCItemUtil.hasItem(player, VCWeapons.BULLET.get());
   }
}
