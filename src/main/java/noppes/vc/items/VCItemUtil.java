package noppes.vc.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 蓄力类物品（枪/法杖/弹弓/投掷物）共用的几件小事。
 *
 * <p>枪与法杖分属两条继承链（{@code ItemGun} / {@code ItemStaff}），苦无还得挂在剑上，
 * 三者没法共一个基类，故把重复的部分放成静态方法——与原作把它们塞进 {@code ItemBasic} 是同一个意图。
 */
public final class VCItemUtil {

   /** 原作 {@code ItemBasic#getMaxItemUseDuration} 一律返回这个值：按住不放就一直蓄力。 */
   public static final int HOLD_FOREVER = 72000;

   private VCItemUtil() {
   }

   /**
    * 广播一声音效。
    *
    * <p>原作在服务端手搓 {@code SPacketCustomSound} 发给附近玩家，是因为 1.12 的
    * {@code World#playSound} 在服务端不带自定义音效；1.20.1 的重载会自动广播给附近客户端，
    * 一行即可，行为等价。
    */
   public static void playSound(Entity entity, SoundEvent sound, float volume, float pitch) {
      entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
              sound, SoundSource.NEUTRAL, volume, pitch);
   }

   /** 右键即开始蓄力，与原作的 {@code setActiveHand} + SUCCESS 一致。 */
   public static InteractionResultHolder<ItemStack> startCharging(Level level, Player player,
                                                                 InteractionHand hand) {
      player.startUsingItem(hand);
      return InteractionResultHolder.consume(player.getItemInHand(hand));
   }

   /** 背包里有没有这个物品（原作先看双手再看背包，顺序照搬）。 */
   public static boolean hasItem(Player player, Item item) {
      return findSlot(player, item) >= 0;
   }

   /** 消耗一个，成功返回 true。 */
   public static boolean consumeItem(Player player, Item item) {
      int slot = findSlot(player, item);
      if (slot < 0) {
         return false;
      }
      player.getInventory().removeItem(slot, 1);
      return true;
   }

   private static int findSlot(Player player, Item item) {
      Inventory inventory = player.getInventory();
      for (int i = 0; i < inventory.getContainerSize(); i++) {
         ItemStack stack = inventory.getItem(i);
         if (!stack.isEmpty() && stack.is(item)) {
            return i;
         }
      }
      return -1;
   }
}
