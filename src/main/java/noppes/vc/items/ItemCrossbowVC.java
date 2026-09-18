package noppes.vc.items;

import net.minecraft.nbt.CompoundTag;
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
import noppes.vc.init.VCItems;

/**
 * VC 的弩（原作 {@code ItemCrossbow}）。
 *
 * <p>⚠️ <b>类名加了 VC 后缀</b>：1.20.1 原版自带 {@code net.minecraft.world.item.CrossbowItem}，
 * 而且 VC 这把弩跟它毫无关系——用的是自己的 {@code crossbow_bolt}、自己的装填计时。
 *
 * <p>流程：按住 20 刻装好一支弩矢（{@code IsLoaded}），松手射出；射出的是一支带重力的箭，
 * 固定 10 点伤害。状态键名沿用原作的 {@code IsLoaded} / {@code Reloading}。
 */
public class ItemCrossbowVC extends Item {

   private static final String LOADED = "IsLoaded";
   private static final String RELOADING = "Reloading";
   private static final int RELOAD_TICKS = 20;

   public ItemCrossbowVC(Properties props) {
      super(props);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      CompoundTag tag = stack.getOrCreateTag();
      if (!player.getAbilities().instabuild
              && VCItemUtil.hasItem(player, VCItems.CROSSBOW_BOLT.get())
              && tag.getInt(LOADED) == 0) {
         tag.putInt(RELOADING, 1);
      }
      return VCItemUtil.startCharging(level, player, hand);
   }

   @Override
   public int getUseDuration(ItemStack stack) {
      return VCItemUtil.HOLD_FOREVER;
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      return tag != null && tag.getInt(RELOADING) != 0 ? UseAnim.NONE : UseAnim.BOW;
   }

   @Override
   public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
      if (level.isClientSide || !(entity instanceof Player player) || player.getAbilities().instabuild) {
         return;
      }
      CompoundTag tag = stack.getTag();
      if (tag != null && tag.getInt(RELOADING) == 1
              && VCItemUtil.hasItem(player, VCItems.CROSSBOW_BOLT.get())
              && getUseDuration(stack) - remaining == RELOAD_TICKS) {
         VCItemUtil.playSound(player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
         tag.putInt(LOADED, 1);
      }
   }

   @Override
   public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      if (level.isClientSide) {
         return;
      }
      if (entity instanceof Player player) {
         boolean creative = player.getAbilities().instabuild;
         CompoundTag tag = stack.getOrCreateTag();
         if (tag.getInt(LOADED) != 1 && !creative) {
            return;
         }
         // 这次松手只是结束装填，不射击
         if (tag.getInt(RELOADING) == 1 && !creative) {
            tag.putInt(RELOADING, 0);
            return;
         }
         if (!creative) {
            VCItemUtil.consumeItem(player, VCItems.CROSSBOW_BOLT.get());
         }
         tag.putInt(LOADED, 0);
      }

      stack.hurtAndBreak(1, entity, user -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      EntityProjectile bolt = new EntityProjectile(level, entity, new ItemStack(Items.ARROW));
      bolt.damage = 10.0F;
      bolt.setSpeed(20);
      bolt.setHasGravity(true);
      bolt.fire(2.0F);
      VCItemUtil.playSound(entity, SoundEvents.ARROW_SHOOT, 1.0F,
              level.getRandom().nextFloat() * 0.3F + 0.8F);
      level.addFreshEntity(bolt);
   }
}
