package noppes.vc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import noppes.vc.blocks.BlockBasicRotated;
import noppes.vc.blocks.tiles.TileWeaponRack;

/**
 * 武器架上的三件武器（原作 {@code BlockWeaponRackRenderer#doRender}）。
 *
 * <p>方块实体只挂在下半格（{@code BlockWeaponRack#newBlockEntity}），所以这里的坐标基准
 * 就是下半格，与原作一致。
 */
public class WeaponRackRenderer implements BlockEntityRenderer<TileWeaponRack> {

   private final ItemRenderer items;

   public WeaponRackRenderer(BlockEntityRendererProvider.Context context) {
      this.items = context.getItemRenderer();
   }

   @Override
   public void render(TileWeaponRack tile, float partial, PoseStack pose, MultiBufferSource buffer,
                      int light, int overlay) {
      pose.pushPose();
      pose.translate(0.5D, 1.34D, 0.5D);
      pose.scale(0.9F, 0.9F, 0.9F);
      pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
      pose.mulPose(Axis.YP.rotationDegrees(
              -tile.getBlockState().getValue(BlockBasicRotated.ROTATION_4) * 90.0F));

      for (int slot = 0; slot < 3; slot++) {
         ItemStack stack = tile.getItem(slot);
         if (!VCItemDisplay.renderable(stack)) {
            continue;
         }
         pose.pushPose();
         pose.translate(-0.37D + slot * 0.37D, 0.6D, 0.33D);
         // 原作拿 thirdperson_right 的 rotation.x 当 Y 轴角度用（看着别扭，但照搬）
         BakedModel model = this.items.getModel(stack, tile.getLevel(), null, 0);
         pose.mulPose(Axis.YP.rotationDegrees(model.getTransforms().thirdPersonRightHand.rotation.x()));
         pose.mulPose(Axis.XP.rotationDegrees(180.0F));
         pose.mulPose(Axis.YP.rotationDegrees(90.0F));
         VCItemDisplay.held(this.items, stack, pose, buffer, light, overlay, tile.getLevel(), 0);
         pose.popPose();
      }
      pose.popPose();
   }

   @Override
   public int getViewDistance() {
      return 40;
   }
}
