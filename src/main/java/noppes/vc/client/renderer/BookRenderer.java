package noppes.vc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import noppes.vc.blocks.BlockBasicRotated;
import noppes.vc.blocks.tiles.TileBook;

/**
 * 书台上那本摊开的书（原作 {@code BlockBookRenderer}）。
 *
 * <p>墨水瓶与羽毛笔已是 JSON 方块模型（{@code shape/book_ink}），这里只剩书本身。
 * 书用原版的 {@link BookModel} 与附魔台书本贴图——原作用的就是这两个。
 * 翻页参数 {@code setupAnim(0, 0, 0, 1)} 对应原作 {@code book.render(null,0,0,1.0F,1.24F,1.0F,...)}
 * 里唯一起作用的那个 {@code ageInTicks=1.0}（1.12 与 1.20.1 的展开式一模一样）。
 *
 * <p>⚠️ <b>摆放位置没有照搬。</b>原作那串 {@code translate(-1.49F, -0.18F, 0)} 会把书扔到
 * 一格半开外（那几个数只有配合 1.12 ModelBase 的内部单位才说得通），这里改为平摊在台面中央。
 */
public class BookRenderer implements BlockEntityRenderer<TileBook> {

   private final BookModel book;

   public BookRenderer(BlockEntityRendererProvider.Context context) {
      this.book = new BookModel(context.bakeLayer(ModelLayers.BOOK));
   }

   @Override
   public void render(TileBook tile, float partial, PoseStack pose, MultiBufferSource buffer,
                      int light, int overlay) {
      pose.pushPose();
      pose.translate(0.5D, 0.14D, 0.5D);
      pose.mulPose(Axis.YP.rotationDegrees(
              -tile.getBlockState().getValue(BlockBasicRotated.ROTATION_4) * 90.0F));
      pose.mulPose(Axis.XP.rotationDegrees(90.0F));   // 立着的书放平
      pose.scale(0.65F, 0.65F, 0.65F);

      this.book.setupAnim(0.0F, 0.0F, 0.0F, 1.0F);
      VertexConsumer consumer = EnchantTableRenderer.BOOK_LOCATION.buffer(buffer, RenderType::entitySolid);
      this.book.render(pose, consumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
      pose.popPose();
   }
}
