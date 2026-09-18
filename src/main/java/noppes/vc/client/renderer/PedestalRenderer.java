package noppes.vc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import noppes.vc.blocks.BlockBasicRotated;
import noppes.vc.blocks.tiles.TilePedestal;

/**
 * 基座上的展示品（原作 {@code BlockPedestalRenderer#doRender}）。
 * 基座本体已是 JSON 方块模型，这里只画那一件物品。
 */
public class PedestalRenderer implements BlockEntityRenderer<TilePedestal> {

   private final ItemRenderer items;

   public PedestalRenderer(BlockEntityRendererProvider.Context context) {
      this.items = context.getItemRenderer();
   }

   @Override
   public void render(TilePedestal tile, float partial, PoseStack pose, MultiBufferSource buffer,
                      int light, int overlay) {
      ItemStack stack = tile.getItem(0);
      if (!VCItemDisplay.renderable(stack)) {
         return;
      }
      pose.pushPose();
      // 与原作同一个基准：translate(x+.5, y+1.5, z+.5) 之后绕 Z 翻转，再按朝向转
      pose.translate(0.5D, 1.5D, 0.5D);
      pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
      pose.mulPose(Axis.YP.rotationDegrees(
              -tile.getBlockState().getValue(BlockBasicRotated.ROTATION_4) * 90.0F));
      pose.translate(0.0D, 0.6D, 0.0D);
      pose.mulPose(Axis.XP.rotationDegrees(180.0F));
      if (stack.getItem() instanceof SwordItem) {
         pose.mulPose(Axis.XN.rotationDegrees(180.0F));   // 剑再翻回来，剑尖朝上
      }
      VCItemDisplay.held(this.items, stack, pose, buffer, light, overlay, tile.getLevel(), 0);
      pose.popPose();
   }

   /** 原作 {@code specialRenderDistance() = 40}。 */
   @Override
   public int getViewDistance() {
      return 40;
   }
}
