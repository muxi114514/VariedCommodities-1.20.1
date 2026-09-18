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
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import noppes.vc.config.VCConfig;
import noppes.vc.entity.EntityProjectile;
import noppes.vc.init.VCSounds;
import noppes.vc.init.VCWeapons;

/**
 * 火枪（原作 {@code ItemMusket}）：单发重武器，先装 60 刻的弹，再松手打出去。
 *
 * <p>状态存在 NBT 的 {@code IsLoaded2} / {@code Reloading2} 里，键名沿用原作。
 * 一发打完就回到未装填状态，得重新按住 3 秒。伤害与击退走配置项。
 *
 * <p>开火声是「爆炸 + 雷鸣」两条原版音效叠在一起，原作就是这么做的。
 */
public class ItemMusket extends Item {

   private static final String LOADED = "IsLoaded2";
   private static final String RELOADING = "Reloading2";
   /** 装填完成的时刻。 */
   private static final int RELOAD_TICKS = 60;

   public ItemMusket(Properties props) {
      super(props);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      CompoundTag tag = stack.getOrCreateTag();
      if (!player.getAbilities().instabuild && VCItemUtil.hasItem(player, VCWeapons.BULLET.get())
              && !tag.getBoolean(LOADED)) {
         tag.putBoolean(RELOADING, true);
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
      return tag != null && tag.getBoolean(RELOADING) ? UseAnim.NONE : UseAnim.BOW;
   }

   @Override
   public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
      if (level.isClientSide || !(entity instanceof Player player) || player.getAbilities().instabuild) {
         return;
      }
      CompoundTag tag = stack.getTag();
      if (tag != null && tag.getBoolean(RELOADING)
              && VCItemUtil.hasItem(player, VCWeapons.BULLET.get())
              && getUseDuration(stack) - remaining == RELOAD_TICKS) {
         VCItemUtil.playSound(player, VCSounds.GUN_AK47_LOAD.get(), 1.0F, 1.0F);
         tag.putBoolean(LOADED, true);
      }
   }

   @Override
   public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      if (level.isClientSide) {
         return;
      }
      if (entity instanceof Player player) {
         CompoundTag tag = stack.getTag();
         if (tag == null) {
            return;
         }
         boolean creative = player.getAbilities().instabuild;
         if (!tag.getBoolean(LOADED) && !creative) {
            VCItemUtil.playSound(player, VCSounds.GUN_EMPTY.get(), 1.0F, 1.0F);
            return;
         }
         // 这次松手只是结束装填，不开火
         if (tag.getBoolean(RELOADING) && !creative) {
            tag.putBoolean(RELOADING, false);
            return;
         }
         if (!creative) {
            VCItemUtil.consumeItem(player, VCWeapons.BULLET.get());
         }
         tag.putBoolean(LOADED, false);
      }

      stack.hurtAndBreak(1, entity, user -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      EntityProjectile bullet = new EntityProjectile(level, entity, new ItemStack(VCWeapons.BULLET.get()));
      bullet.damage = VCConfig.musketDamage();
      bullet.punch = VCConfig.musketKnockback();
      bullet.setSpeed(50);
      bullet.fire(2.0F);
      level.addFreshEntity(bullet);

      float pitch = level.getRandom().nextFloat() * 0.3F + 1.8F;
      VCItemUtil.playSound(entity, SoundEvents.GENERIC_EXPLODE, 0.9F, pitch);
      VCItemUtil.playSound(entity, SoundEvents.LIGHTNING_BOLT_THUNDER, 2.0F, pitch);
   }
}
