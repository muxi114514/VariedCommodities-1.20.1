package noppes.vc.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import noppes.vc.entity.EntityProjectile;
import noppes.vc.init.VCSounds;

/**
 * 投掷武器（原作 {@code ItemThrowingWeapon}）：飞刀、手里剑、掷斧之类。
 *
 * <p>按住右键蓄力、松手掷出。飞出的是一个 {@link EntityProjectile}：3D 显示、插墙、受重力、速度 1.2。
 * 三个开关对应原作的三个链式设置：
 * {@code setRotating()} 让它边飞边翻滚、{@code setDamage(n)} 改伤害、{@code setDropItem()} 落地可捡回。
 */
public class ItemThrowingWeapon extends Item {

   private final boolean rotating;
   private final int damage;
   private final boolean dropItem;

   public ItemThrowingWeapon(int damage, boolean rotating, boolean dropItem, Properties props) {
      super(props);
      this.damage = damage;
      this.rotating = rotating;
      this.dropItem = dropItem;
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
      EntityProjectile projectile = new EntityProjectile(level, entity, new ItemStack(this));
      projectile.damage = this.damage;
      projectile.setRotating(this.rotating);
      projectile.setIs3D(true);
      projectile.setStickInWall(true);
      projectile.setHasGravity(true);
      projectile.setSpeed(12);
      if (entity instanceof Player player) {
         boolean creative = player.getAbilities().instabuild;
         projectile.canBePickedUp = !creative && this.dropItem;
         if (!creative) {
            VCItemUtil.consumeItem(player, this);
         }
      }
      projectile.fire(1.0F);
      VCItemUtil.playSound(entity, VCSounds.MISC_SWOSH.get(), 1.0F, 1.0F);
      level.addFreshEntity(projectile);
   }
}
