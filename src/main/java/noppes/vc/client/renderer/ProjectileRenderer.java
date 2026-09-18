package noppes.vc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import noppes.vc.entity.EntityProjectile;

/**
 * 投射物渲染（原作 {@code client/renderer/RenderProjectile}）。
 *
 * <p>两种画法，由实体的 {@code is3D} 决定：
 * <ul>
 * <li><b>3D</b>：苦无、手里剑、投掷武器、方块——按飞行方向立起来，旋转的还要翻滚；</li>
 * <li><b>平面</b>：子弹、法术弹——像原版投掷物那样始终正对镜头。</li>
 * </ul>
 */
public class ProjectileRenderer extends EntityRenderer<EntityProjectile> {

   private final ItemRenderer items;

   public ProjectileRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.items = context.getItemRenderer();
   }

   @Override
   public void render(EntityProjectile entity, float yaw, float partial, PoseStack pose,
                      MultiBufferSource buffer, int light) {
      ItemStack stack = entity.getItemDisplay();
      if (stack.isEmpty()) {
         return;
      }
      pose.pushPose();
      if (entity.is3D()) {
         // 先转到飞行方向，再把物品放平（GROUND 变换是平躺的，转 90° 立起来朝前）
         pose.mulPose(Axis.YP.rotationDegrees(
                 Mth.lerp(partial, entity.yRotO, entity.getYRot()) - 90.0F));
         pose.mulPose(Axis.ZP.rotationDegrees(
                 Mth.lerp(partial, entity.xRotO, entity.getXRot()) + 90.0F));
         pose.scale(1.4F, 1.4F, 1.4F);
      } else {
         pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
         pose.mulPose(Axis.YP.rotationDegrees(180.0F));
      }
      // 插在方块上时不该还带着发光（原作靠 glows 字段控制，这里同样只信那个字段）
      int packed = entity.glows() ? 0x00F000F0 : light;
      this.items.renderStatic(stack, ItemDisplayContext.GROUND, packed, OverlayTexture.NO_OVERLAY,
              pose, buffer, entity.level(), entity.getId());
      pose.popPose();
   }

   @Override
   public ResourceLocation getTextureLocation(EntityProjectile entity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
