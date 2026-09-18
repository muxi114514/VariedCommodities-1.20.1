package noppes.vc.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import noppes.vc.VariedCommodities;
import noppes.vc.blocks.tiles.TileBigSign;
import noppes.vc.network.SaveSignPacket;
import noppes.vc.network.VCNetwork;

/**
 * 刻字界面（大告示牌、墓碑）。
 *
 * <p><b>没有移植 1.12 的自制控件框架。</b>原作这个界面继承 {@code GuiTextAreaScreen}，
 * 后者依赖 {@code shared/gui/} 下 1282 行的自制控件（GuiBasic / GuiTextArea / GuiScroll /
 * GuiTextFieldNop …）——那套东西存在的理由是 1.12 原版没有多行文本框。
 * 1.20.1 原生的 {@link MultiLineEditBox} 自带换行、滚动、光标与选区，
 * 于是同样的界面（背景图 + 文本框 + 清空/粘贴/复制/关闭四个按钮）只要这么点代码。
 * 背景贴图与四个按钮的译名都沿用原作（lang 键在 P1 已转换好）。
 */
public class ScreenTextEdit extends Screen {

   private static final ResourceLocation BACKGROUND =
           new ResourceLocation(VariedCommodities.MODID, "textures/gui/bgfilled.png");

   private static final int BUTTON_W = 56;
   private static final int BUTTON_H = 20;
   private static final int MARGIN = 8;

   private final BlockPos pos;
   private final String initialText;

   private MultiLineEditBox editor;
   private int panelLeft;
   private int panelTop;
   private int panelWidth;
   private int panelHeight;

   /** 标题直接用方块自己的译名，不为这个界面新造 lang 键。 */
   public ScreenTextEdit(BlockPos pos, String initialText, Component title) {
      super(title);
      this.pos = pos;
      this.initialText = initialText;
   }

   @Override
   protected void init() {
      // 面板尺寸沿用原作比例：宽度取屏幕的 88%，高宽比 0.56
      this.panelWidth = (int) (this.width * 0.88D);
      this.panelHeight = (int) (this.panelWidth * 0.56D);
      this.panelLeft = (this.width - this.panelWidth) / 2;
      this.panelTop = (this.height - this.panelHeight) / 2;

      int buttonX = this.panelLeft + this.panelWidth - BUTTON_W - MARGIN;
      int editorWidth = this.panelWidth - BUTTON_W - MARGIN * 3;

      String current = this.editor != null ? this.editor.getValue() : this.initialText;
      this.editor = new MultiLineEditBox(this.font,
              this.panelLeft + MARGIN, this.panelTop + MARGIN, editorWidth, this.panelHeight - MARGIN * 2,
              Component.empty(), getTitle());
      this.editor.setCharacterLimit(TileBigSign.MAX_LENGTH);
      this.editor.setValue(current);
      addRenderableWidget(this.editor);

      int y = this.panelTop + MARGIN;
      addRenderableWidget(button("clear", buttonX, y, b -> this.editor.setValue("")));
      y += BUTTON_H + 3;
      addRenderableWidget(button("paste", buttonX, y,
              b -> this.editor.setValue(this.minecraft.keyboardHandler.getClipboard())));
      y += BUTTON_H + 3;
      addRenderableWidget(button("copy", buttonX, y,
              b -> this.minecraft.keyboardHandler.setClipboard(this.editor.getValue())));
      addRenderableWidget(button("close", buttonX,
              this.panelTop + this.panelHeight - BUTTON_H - MARGIN, b -> onClose()));
   }

   private Button button(String key, int x, int y, Button.OnPress action) {
      return Button.builder(Component.translatable(VariedCommodities.MODID + ".gui." + key), action)
              .bounds(x, y, BUTTON_W, BUTTON_H)
              .build();
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      renderBackground(graphics);
      // 256×256 的背景图整张拉伸到面板大小，与原作的 bgScale 做法一致
      graphics.blit(BACKGROUND, this.panelLeft, this.panelTop, this.panelWidth, this.panelHeight,
              0.0F, 0.0F, 256, 256, 256, 256);
      super.render(graphics, mouseX, mouseY, partialTick);
   }

   /** 关闭即保存，与原作一致（没有单独的确认按钮）。 */
   @Override
   public void onClose() {
      VCNetwork.toServer(new SaveSignPacket(this.pos, this.editor.getValue()));
      super.onClose();
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}
