package noppes.vc.client.renderer;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import noppes.vc.blocks.BlockBasicRotated;
import noppes.vc.blocks.tiles.TileBigSign;
import noppes.vc.init.VCBlocks;

/**
 * 大告示牌与墓碑上的刻字（原作 {@code BlockBigSignRenderer} / {@code BlockTombstoneRenderer}）。
 *
 * <p><b>用的是原版字体，不是 opensans。</b>原作这两处都传 {@code mcFont = true}
 * （{@code TextBlockClient} 的第三个参数），opensans 只服务于 1.12 自制的多行文本框 {@code GuiTextArea}；
 * 那个控件已在 P4 换成原版 {@code MultiLineEditBox}，所以 <b>opensans.ttf 现在整个用不上了</b>。
 *
 * <p>分行原作是自己按空格逐词累加宽度，1.20.1 的 {@link Font#split} 做的是同一件事
 * （同样按词断行、同样认 {@code \n}），直接用，省掉一整个 TextBlock 体系。
 * 结果按文本缓存，文本没变就不重拆——等价于原作的 {@code hasChanged} 标志。
 */
public class TextBlockRenderer implements BlockEntityRenderer<TileBigSign> {

   /** 大告示牌：黑字、行宽 112、最多 13 行、按 14 行的高度居中（原作 BlockBigSignRenderer）。 */
   private static final Layout BIG_SIGN = new Layout(1.0F, 1.005F, 0.0133F * 2.0F / 3.0F,
           112, 13, 14, 0x000000);
   /** 墓碑：白字、行宽 94、最多 14 行、按 11 行的高度居中（原作 BlockTombstoneRenderer）。 */
   private static final Layout TOMBSTONE = new Layout(0.86F, 10.0F / 16.0F + 0.005F, 0.00665F,
           94, 14, 11, 0xFFFFFF);

   private final Font font;
   /** 渲染线程独占；方块实体被回收时自动清掉，不会攒内存。 */
   private final Map<TileBigSign, Cache> cache = new WeakHashMap<>();

   public TextBlockRenderer(BlockEntityRendererProvider.Context context) {
      this.font = context.getFont();
   }

   @Override
   public void render(TileBigSign tile, float partial, PoseStack pose, MultiBufferSource buffer,
                      int light, int overlay) {
      String text = tile.text();
      if (text == null || text.isEmpty()) {
         return;
      }
      BlockState state = tile.getBlockState();
      Layout layout = state.is(VCBlocks.BIG_SIGN.get()) ? BIG_SIGN : TOMBSTONE;
      List<FormattedCharSequence> lines = lines(tile, text, layout);
      if (lines.isEmpty()) {
         return;
      }

      pose.pushPose();
      pose.translate(0.5D, layout.y(), 0.5D);
      pose.mulPose(Axis.YP.rotationDegrees(
              -state.getValue(BlockBasicRotated.ROTATION_4) * 90.0F));
      pose.translate(0.0D, 0.0D, layout.z() - 0.5D);
      pose.scale(layout.scale(), -layout.scale(), layout.scale());

      // 行数不足时整体上下居中，与原作同一个式子
      float offset = lines.size() < layout.centerOver()
              ? (layout.centerOver() - lines.size()) / 2.0F : 0.0F;
      float step = this.font.lineHeight - 0.3F;
      int count = Math.min(lines.size(), layout.maxLines());
      for (int i = 0; i < count; i++) {
         FormattedCharSequence line = lines.get(i);
         this.font.drawInBatch(line, -this.font.width(line) / 2.0F, (offset + i) * step,
                 layout.color(), false, pose.last().pose(), buffer,
                 Font.DisplayMode.POLYGON_OFFSET, 0, light);
      }
      pose.popPose();
   }

   private List<FormattedCharSequence> lines(TileBigSign tile, String text, Layout layout) {
      Cache cached = this.cache.get(tile);
      if (cached == null || !cached.text.equals(text)) {
         cached = new Cache(text, this.font.split(Component.literal(text), layout.width()));
         this.cache.put(tile, cached);
      }
      return cached.lines;
   }

   @Override
   public int getViewDistance() {
      return 20;
   }

   private record Layout(float y, float z, float scale, int width, int maxLines,
                         int centerOver, int color) {
   }

   private record Cache(String text, List<FormattedCharSequence> lines) {
   }
}
