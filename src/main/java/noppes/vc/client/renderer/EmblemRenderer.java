package noppes.vc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import noppes.vc.blocks.BlockBasicDouble;
import noppes.vc.blocks.BlockBanner;
import noppes.vc.blocks.BlockBasicRotated;
import noppes.vc.blocks.BlockWallBanner;
import noppes.vc.blocks.tiles.TileBanner;

/**
 * 横幅 / 壁挂横幅 / 徽记牌上的那件徽记物品。
 *
 * <p>三者共用 {@link TileBanner}（数据完全一样），只是贴的位置不同，故合成一个渲染器。
 * 位置与尺寸由各自的旗面/牌面几何推出，尺寸沿用原作的成品大小
 * （横幅 {@code 16×0.05=0.8} 格、徽记牌 {@code 16×0.024≈0.38} 格）；
 * 变换链没有照搬，理由见 {@link VCItemDisplay#flat}。
 */
public class EmblemRenderer implements BlockEntityRenderer<TileBanner> {

   private final ItemRenderer items;

   public EmblemRenderer(BlockEntityRendererProvider.Context context) {
      this.items = context.getItemRenderer();
   }

   @Override
   public void render(TileBanner tile, float partial, PoseStack pose, MultiBufferSource buffer,
                      int light, int overlay) {
      ItemStack icon = tile.icon();
      if (!VCItemDisplay.renderable(icon)) {
         return;
      }
      BlockState state = tile.getBlockState();
      if (state.hasProperty(BlockBasicDouble.HALF)
              && state.getValue(BlockBasicDouble.HALF) == DoubleBlockHalf.UPPER) {
         return;   // 双格横幅的徽记只画一次，坐标基准是下半格
      }

      float yaw = -state.getValue(BlockBasicRotated.ROTATION_4) * 90.0F;
      if (state.getBlock() instanceof BlockBanner) {
         emblem(tile, icon, pose, buffer, light, overlay, yaw, 1.3F, 6.0F / 16.0F, 0.8F, false);
      } else if (state.getBlock() instanceof BlockWallBanner) {
         emblem(tile, icon, pose, buffer, light, overlay, yaw, 0.2F, 12.5F / 16.0F, 0.8F, false);
      } else {
         // 徽记牌两面都画，与原作一致（原作画完正面再转 180° 画一次背面）
         emblem(tile, icon, pose, buffer, light, overlay, yaw, 0.6F, 7.5F / 16.0F, 0.384F, false);
         emblem(tile, icon, pose, buffer, light, overlay, yaw, 0.6F, 8.5F / 16.0F, 0.384F, true);
      }
   }

   /**
    * @param plane 徽记所在平面的方块内 z 坐标（朝向 0 时）
    * @param back  true 表示贴在平面的 +Z 侧、朝 +Z
    */
   private void emblem(TileBanner tile, ItemStack icon, PoseStack pose, MultiBufferSource buffer,
                       int light, int overlay, float yaw, float y, float plane, float size, boolean back) {
      pose.pushPose();
      pose.translate(0.5D, y, 0.5D);
      pose.mulPose(Axis.YP.rotationDegrees(yaw));
      pose.translate(0.0D, 0.0D, plane - 0.5F + (back ? 0.006F : -0.006F));
      if (!back) {
         pose.mulPose(Axis.YP.rotationDegrees(180.0F));
      }
      VCItemDisplay.flat(this.items, icon, size, pose, buffer, light, overlay, tile.getLevel(), 0);
      pose.popPose();
   }

   @Override
   public int getViewDistance() {
      return 20;
   }
}
