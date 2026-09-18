package noppes.vc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 在世界里画物品的两种姿势，对应原作 TESR 的两套写法。
 *
 * <p>原作一律跳过 {@code ItemBlock}（基座/武器架/徽记都只认非方块物品）——
 * 这是渲染层的事，逻辑层照收不误，与原作一致，不擅自改。
 */
public final class VCItemDisplay {

   private VCItemDisplay() {
   }

   public static boolean renderable(ItemStack stack) {
      return !stack.isEmpty() && !(stack.getItem() instanceof BlockItem);
   }

   /**
    * 「握持姿势」：基座与武器架上的武器。
    *
    * <p>原作只取模型 {@code thirdperson_right} 的<b>缩放</b>，变换用 {@code TransformType.NONE}
    * （即不套那个变换的旋转与位移），再自己转 45°。这里逐字照搬：
    * {@link ItemDisplayContext#NONE} 在 1.20.1 同样走 {@code ItemTransforms.NO_TRANSFORM}。
    */
   public static void held(ItemRenderer renderer, ItemStack stack, PoseStack pose,
                           MultiBufferSource buffer, int light, int overlay, Level level, int seed) {
      BakedModel model = renderer.getModel(stack, level, null, seed);
      ItemTransform transform = model.getTransforms().thirdPersonRightHand;
      pose.scale(transform.scale.x(), transform.scale.y(), transform.scale.z());
      pose.mulPose(Axis.ZP.rotationDegrees(45.0F));
      renderer.renderStatic(stack, ItemDisplayContext.NONE, light, overlay, pose, buffer, level, seed);
   }

   /**
    * 「贴面姿势」：横幅与告示牌上的徽记，扁平地贴在旗面/牌面上。
    *
    * <p>⚠️ <b>没有照搬原作的变换链</b>。原作走 {@code renderItemAndEffectIntoGUI}，
    * 那套变换里藏着 GUI 专用的 100 单位 Z 推进与 64× 缩放，1.20.1 的 ItemRenderer 没有对应物，
    * 照抄那串 {@code translate(0,0,-3.57)} 会把徽记扔到几格开外。
    * 改为按模型几何把徽记贴在旗面/牌面外侧，尺寸沿用原作的成品大小
    * （横幅 16×0.05=0.8 格、告示牌 16×0.024≈0.38 格）。
    * {@link ItemDisplayContext#FIXED} 就是原版展示框用的那个"平贴"变换。
    */
   public static void flat(ItemRenderer renderer, ItemStack stack, float size, PoseStack pose,
                           MultiBufferSource buffer, int light, int overlay, Level level, int seed) {
      float scale = size / 0.5F;            // FIXED 变换本身把物品缩到半格
      pose.scale(scale, scale, scale);
      renderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffer, level, seed);
   }
}
