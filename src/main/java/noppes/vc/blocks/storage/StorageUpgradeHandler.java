package noppes.vc.blocks.storage;

import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import noppes.vc.VariedCommodities;

/**
 * 把右键方块事件转交给 {@link StorageUpgrades}。
 *
 * <p><b>为什么不写在 {@code BlockCrate#use} 里：</b>原版在「潜行 + 手里有东西」时直接跳过方块交互、
 * 改走物品放置（{@code ServerPlayerGameMode#useItemOn} 的 {@code isSecondaryUseActive} 分支，
 * 客户端 {@code MultiPlayerGameMode} 同理），{@code use} 根本收不到这次点击，铁块会被放在箱子旁边。
 * Forge 的这个事件在该分支之前触发，取消即可截下。
 *
 * <p>两端都要取消：客户端不拦会先预测放置出一个幽灵方块。
 * 默认不接收已被取消的事件，领地保护类模组在更高优先级拒绝后这里不会越权执行。
 */
@Mod.EventBusSubscriber(modid = VariedCommodities.MODID)
public final class StorageUpgradeHandler {

   private StorageUpgradeHandler() {
   }

   @SubscribeEvent
   public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
      InteractionResult result = StorageUpgrades.tryUpgrade(
              event.getLevel(), event.getPos(), event.getEntity(), event.getHand());
      if (result != InteractionResult.PASS) {
         event.setCanceled(true);
         event.setCancellationResult(result);
      }
   }
}
