package noppes.vc.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import noppes.vc.client.VCClientHooks;

/**
 * 配方书：右键打开配方一览（原作 {@code ItemVCRecipesBook}）。
 *
 * <p>原作在服务端发一个开界面的包；1.20.1 的配方表本就随登录同步到客户端，
 * 界面纯展示、不需要服务端参与，故直接在客户端打开，少一次网络往返。
 * {@code DistExecutor} 保证专用服务器上不会去类加载客户端类。
 */
public class ItemRecipesBook extends Item {

   public ItemRecipesBook(Properties props) {
      super(props);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (level.isClientSide) {
         DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> VCClientHooks::openRecipeBook);
      }
      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
   }
}
