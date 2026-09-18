package noppes.vc.items;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import noppes.vc.entity.EntityProjectile;

/**
 * 弹弓（原作 {@code ItemSlingshot}）：打圆石，蓄力越久击退越强。
 *
 * <p>蓄力满 6 刻才打得出去；超过 24 刻击退从 1 级升到 2 级。石块边飞边翻滚、受重力。
 */
public class ItemSlingshot extends Item {

   private static final int MIN_CHARGE = 6;
   private static final int STRONG_CHARGE = 24;

   public ItemSlingshot(Properties props) {
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
      int charge = getUseDuration(stack) - timeLeft;
      if (charge < MIN_CHARGE) {
         return;
      }
      if (entity instanceof Player player && !player.getAbilities().instabuild
              && !VCItemUtil.consumeItem(player, Items.COBBLESTONE)) {
         return;
      }
      stack.hurtAndBreak(1, entity, user -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));

      EntityProjectile stone = new EntityProjectile(level, entity, new ItemStack(Items.COBBLESTONE));
      stone.damage = 4.0F;
      stone.punch = charge > STRONG_CHARGE ? 2 : 1;
      stone.setRotating(true);
      stone.setHasGravity(true);
      stone.setSpeed(14);
      stone.fire(1.0F);
      VCItemUtil.playSound(entity, SoundEvents.ARROW_SHOOT, 1.0F,
              level.getRandom().nextFloat() * 0.3F + 0.8F);
      level.addFreshEntity(stone);
   }
}
