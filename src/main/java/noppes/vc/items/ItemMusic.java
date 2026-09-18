package noppes.vc.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * 乐器（原作 {@code ItemMusic}）：右键随机奏一个音，并冒一个音符粒子。
 *
 * <p>音高公式逐字照搬原作：{@code 2^((note-12)/12)}，音符取 0~23——
 * 与原版音符盒同一套算法。粒子的"颜色"由速度 x 分量携带（原版音符粒子就是这么用的），
 * 原作传 {@code note/24}，这里一样。
 *
 * <p>小提琴/单簧管/陶笛在原作里多了个拉弓姿势（{@code getItemUseAction = BOW}），
 * 用 {@code bowLike} 开关表达，不再为三件乐器各建一个类。
 */
public class ItemMusic extends Item {

   private static final int NOTES = 24;

   private final boolean bowLike;

   public ItemMusic(boolean bowLike, Properties props) {
      super(props);
      this.bowLike = bowLike;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (this.bowLike) {
         player.startUsingItem(hand);
      }
      if (level.isClientSide) {
         return InteractionResultHolder.success(stack);
      }
      int note = level.getRandom().nextInt(NOTES);
      float pitch = (float) Math.pow(2.0D, (note - 12) / 12.0D);
      VCItemUtil.playSound(player, SoundEvents.NOTE_BLOCK_HARP.value(), 3.0F, pitch);
      if (level instanceof ServerLevel server) {
         server.sendParticles(ParticleTypes.NOTE, player.getX(), player.getY() + 1.2D, player.getZ(),
                 0, note / (double) NOTES, 0.0D, 0.0D, 1.0D);
      }
      return InteractionResultHolder.consume(stack);
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return this.bowLike ? UseAnim.BOW : UseAnim.NONE;
   }

   @Override
   public int getUseDuration(ItemStack stack) {
      return this.bowLike ? VCItemUtil.HOLD_FOREVER : 0;
   }
}
