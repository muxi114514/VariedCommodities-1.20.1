package noppes.vc.items;

import net.minecraft.nbt.CompoundTag;
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
 * 机枪（原作 {@code ItemMachineGun}）：按住不放<b>每 6 刻一发</b>，打空 8 发后要花时间换弹。
 *
 * <p>弹匣状态存在物品 NBT 里，键名沿用原作的 {@code ShotsLeft} / {@code Reloading2}。
 * 换弹期间用的动作是 {@code NONE} 而非 {@code BOW}——原作靠这个让手不举起来，表示"在装弹"。
 *
 * <p>伤害走配置项 {@code machineGunDamage}（原作 {@code VariedCommodities.MachineGunDamage}）。
 */
public class ItemMachineGun extends Item {

   private static final String SHOTS_LEFT = "ShotsLeft";
   private static final String RELOADING = "Reloading2";
   /** 射速：每 6 刻一发。 */
   private static final int FIRE_INTERVAL = 6;
   private static final int MAGAZINE = 8;

   public ItemMachineGun(Properties props) {
      super(props);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!player.getAbilities().instabuild && !VCItemUtil.hasItem(player, VCWeapons.BULLET.get())) {
         stack.getOrCreateTag().putBoolean(RELOADING, true);
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
      if (level.isClientSide) {
         return;
      }
      int ticks = getUseDuration(stack) - remaining;
      if (ticks % FIRE_INTERVAL != 0) {
         return;
      }
      if (entity instanceof Player player && !player.getAbilities().instabuild) {
         CompoundTag tag = stack.getOrCreateTag();
         boolean hasBullet = VCItemUtil.hasItem(player, VCWeapons.BULLET.get());
         if (tag.getBoolean(RELOADING) && hasBullet) {
            // 换弹的这 24 刻里反复放上膛声，与原作一致
            if (ticks > 0 && ticks <= 24) {
               VCItemUtil.playSound(player, VCSounds.GUN_AK47_LOAD.get(), 1.0F, 1.0F);
            }
            return;
         }
         if (tag.getInt(SHOTS_LEFT) - ticks / FIRE_INTERVAL <= 0 || !hasBullet) {
            VCItemUtil.playSound(player, VCSounds.GUN_EMPTY.get(), 1.0F, 1.0F);
            return;
         }
         VCItemUtil.consumeItem(player, VCWeapons.BULLET.get());
         tag.remove(RELOADING);
      }

      EntityProjectile bullet = new EntityProjectile(level, entity, new ItemStack(VCWeapons.BULLET.get()));
      bullet.damage = VCConfig.machineGunDamage();
      bullet.setSpeed(40);
      bullet.fire(2.0F);
      VCItemUtil.playSound(entity, VCSounds.GUN_PISTOL_SHOT.get(), 0.9F,
              level.getRandom().nextFloat() * 0.3F + 0.8F);
      level.addFreshEntity(bullet);
   }

   /**
    * 松手时结算弹匣。
    *
    * <p>原作这段逻辑绕：{@code ShotsLeft} 并不是"剩几发"的实时计数，而是"开火起始时的余量"，
    * 真正的余量要减去 {@code ticks/6}。松手时才把差额写回去；若已见底就翻成换弹状态并扣一点耐久。
    * 换弹结束（下次松手）时按按住的时长补回子弹数，最多补满一匣。逐字照搬。
    */
   @Override
   public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      if (!(entity instanceof Player player) || player.getAbilities().instabuild) {
         return;
      }
      CompoundTag tag = stack.getOrCreateTag();
      int ticks = getUseDuration(stack) - timeLeft;
      int shotsLeft = tag.getInt(SHOTS_LEFT) - ticks / FIRE_INTERVAL;
      if (tag.getBoolean(RELOADING)) {
         int reloaded = ticks > 40 ? MAGAZINE : ticks / 5;
         if (reloaded > 1) {
            tag.putInt(SHOTS_LEFT, reloaded);
            tag.putBoolean(RELOADING, false);
         }
      } else if (shotsLeft <= 0) {
         tag.putBoolean(RELOADING, true);
         stack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      } else {
         tag.putInt(SHOTS_LEFT, shotsLeft);
      }
   }
}
