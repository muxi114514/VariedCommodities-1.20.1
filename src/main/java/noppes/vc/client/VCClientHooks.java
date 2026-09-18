package noppes.vc.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import noppes.vc.client.gui.ScreenRecipes;
import noppes.vc.client.gui.ScreenTextEdit;

/**
 * 只在客户端执行的入口。
 *
 * 公共代码经 {@code DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ...)} 进来，
 * 这样专用服务器上本类永远不会被类加载，也就不会碰到任何客户端专属的类。
 */
public final class VCClientHooks {

   private VCClientHooks() {
   }

   public static void openTextEditor(BlockPos pos, String text, Component title) {
      Minecraft.getInstance().setScreen(new ScreenTextEdit(pos, text, title));
   }

   /** 阅读书台上的成书，用原版界面。 */
   public static void openBook(ItemStack book) {
      Minecraft.getInstance().setScreen(new BookViewScreen(new BookViewScreen.WrittenBookAccess(book)));
   }

   /**
    * 配方书。纯客户端界面——配方表本就随登录同步到客户端，
    * 不像原作那样绕服务端发一个开界面的包。
    */
   public static void openRecipeBook() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         mc.setScreen(new ScreenRecipes(mc.level));
      }
   }
}
