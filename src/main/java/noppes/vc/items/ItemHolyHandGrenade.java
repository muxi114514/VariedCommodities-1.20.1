package noppes.vc.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import noppes.vc.entity.EntityHolyHandGrenade;
import noppes.vc.init.VCSounds;

/**
 * 安提阿的圣手雷（原作 {@code ItemHolyHandGrenade}）。
 *
 * <p>蓄力越久扔得越远（速度取自原版弓的 {@code getArrowVelocity} 曲线），
 * 落地 34 刻后爆炸，威力由配置项 {@code holyHandGrenadeStrength} 决定。
 */
public class ItemHolyHandGrenade extends Item {

   /** 原作 {@code projectile.timeToLive = 34}——那是落地后的引信，不是空中寿命。 */
   private static final int FUSE = 34;

   public ItemHolyHandGrenade(Properties props) {
      super(props);
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
      float power = chargePower(getUseDuration(stack) - timeLeft);
      EntityHolyHandGrenade grenade =
              new EntityHolyHandGrenade(level, entity, new ItemStack(this));
      grenade.damage = 0.0F;
      grenade.setIs3D(true);
      grenade.setStickInWall(true);
      grenade.setRotating(true);
      grenade.setHasGravity(true);
      grenade.setSpeed((int) (8.0F * power) + 5);
      grenade.groundTimeToLive = FUSE;
      grenade.canBePickedUp = false;
      if (entity instanceof Player player && !player.getAbilities().instabuild) {
         VCItemUtil.consumeItem(player, this);
      }
      grenade.fire(1.0F);
      VCItemUtil.playSound(entity, VCSounds.MISC_SWOSH.get(), 1.0F, 1.0F);
      level.addFreshEntity(grenade);
   }

   /** 与原版弓同一条曲线（原作 {@code getArrowVelocity} 就是抄的它）。 */
   private static float chargePower(int charge) {
      float f = charge / 20.0F;
      f = (f * f + f * 2.0F) / 3.0F;
      return Math.min(f, 1.0F);
   }
}
