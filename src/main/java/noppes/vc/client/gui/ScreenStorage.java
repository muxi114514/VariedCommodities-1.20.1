package noppes.vc.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import noppes.vc.containers.StorageLayout;
import noppes.vc.containers.StorageMenu;

/**
 * 升级板条箱的界面：任意 列×行，<b>不带任何新贴图</b>。
 *
 * <p>背景从原版 {@code generic_54.png} 切片拼出：面板四角各 4×4、四条边平铺、中间填原版底色，
 * 每个槽位再贴一格原版槽位底图。原版面板的边框在任何位置都一样，切下来重复就能得到任意尺寸，
 * 资源包改了原版箱子界面，这里也跟着变。
 *
 * <p><b>放不下时整体缩小。</b>9 行的界面高 276，而 1080p 默认 GUI 缩放下可视高度只有 254~270。
 * 做法是把整个界面放进一个缩放后的"虚拟屏幕"里：绘制时压缩坐标系，鼠标事件按同一比例换算回去，
 * 缩放为 1 时这层换算全部是恒等，行为与普通容器界面完全一样。
 */
public class ScreenStorage extends AbstractContainerScreen<StorageMenu> {

   private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
   /** 原版面板四角（含圆角与 3 像素描边）的边长。 */
   private static final int EDGE = 4;
   private static final int SOURCE_WIDTH = 176;
   private static final int SOURCE_HEIGHT = 222;
   /** 左右边框取样的高度：标题带那段不含任何槽位，纯边框。 */
   private static final int SIDE_SAMPLE = 13;
   /** 原版槽位底图在 generic_54 中的位置（首格物品左上角外扩 1 像素）。 */
   private static final int SLOT_U = 7;
   private static final int SLOT_V = 17;
   private static final int PANEL_COLOR = 0xFFC6C6C6;

   private float scale = 1.0F;

   public ScreenStorage(StorageMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      StorageLayout layout = StorageLayout.of(menu.tier());
      this.imageWidth = layout.width();
      this.imageHeight = layout.height();
      this.inventoryLabelX = layout.inventoryX();
      this.inventoryLabelY = layout.inventoryY() - 11;
   }

   @Override
   protected void init() {
      super.init();
      this.scale = Math.min(1.0F, Math.min((float) this.height / this.imageHeight,
              (float) this.width / this.imageWidth));
      if (this.scale < 1.0F) {
         // leftPos/topPos 改为虚拟屏幕里的坐标，使缩放后界面仍居中
         this.leftPos = Math.round((this.width / this.scale - this.imageWidth) / 2.0F);
         this.topPos = Math.round((this.height / this.scale - this.imageHeight) / 2.0F);
      }
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      renderBackground(graphics);
      graphics.pose().pushPose();
      graphics.pose().scale(this.scale, this.scale, 1.0F);
      super.render(graphics, (int) (mouseX / this.scale), (int) (mouseY / this.scale), partialTick);
      graphics.pose().popPose();
      // 物品提示框在真实坐标下画，保持原大小、跟随真实鼠标
      renderTooltip(graphics, mouseX, mouseY);
   }

   @Override
   protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      int x = this.leftPos;
      int y = this.topPos;
      int w = this.imageWidth;
      int h = this.imageHeight;
      int right = SOURCE_WIDTH - EDGE;
      int bottom = SOURCE_HEIGHT - EDGE;

      graphics.fill(x + EDGE, y + EDGE, x + w - EDGE, y + h - EDGE, PANEL_COLOR);
      graphics.blit(TEXTURE, x, y, 0, 0, EDGE, EDGE);
      graphics.blit(TEXTURE, x + w - EDGE, y, right, 0, EDGE, EDGE);
      graphics.blit(TEXTURE, x, y + h - EDGE, 0, bottom, EDGE, EDGE);
      graphics.blit(TEXTURE, x + w - EDGE, y + h - EDGE, right, bottom, EDGE, EDGE);
      graphics.blitRepeating(TEXTURE, x + EDGE, y, w - 2 * EDGE, EDGE, EDGE, 0, right - EDGE, EDGE);
      graphics.blitRepeating(TEXTURE, x + EDGE, y + h - EDGE, w - 2 * EDGE, EDGE, EDGE, bottom, right - EDGE, EDGE);
      graphics.blitRepeating(TEXTURE, x, y + EDGE, EDGE, h - 2 * EDGE, 0, EDGE, EDGE, SIDE_SAMPLE);
      graphics.blitRepeating(TEXTURE, x + w - EDGE, y + EDGE, EDGE, h - 2 * EDGE, right, EDGE, EDGE, SIDE_SAMPLE);

      for (Slot slot : this.menu.slots) {
         graphics.blit(TEXTURE, x + slot.x - 1, y + slot.y - 1, SLOT_U, SLOT_V,
                 StorageLayout.SLOT, StorageLayout.SLOT);
      }
   }

   // ── 鼠标事件换算到虚拟屏幕 ───────────────────────────────────────────

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return super.mouseClicked(mouseX / this.scale, mouseY / this.scale, button);
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return super.mouseReleased(mouseX / this.scale, mouseY / this.scale, button);
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      return super.mouseDragged(mouseX / this.scale, mouseY / this.scale, button,
              dragX / this.scale, dragY / this.scale);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      return super.mouseScrolled(mouseX / this.scale, mouseY / this.scale, delta);
   }

   @Override
   public void mouseMoved(double mouseX, double mouseY) {
      super.mouseMoved(mouseX / this.scale, mouseY / this.scale);
   }

   // ── 对外（JEI 等）报告界面在真实屏幕上的位置，避免它们的覆盖层压到界面上 ──

   @Override
   public int getGuiLeft() {
      return Math.round(this.leftPos * this.scale);
   }

   @Override
   public int getGuiTop() {
      return Math.round(this.topPos * this.scale);
   }

   @Override
   public int getXSize() {
      return Math.round(this.imageWidth * this.scale);
   }

   @Override
   public int getYSize() {
      return Math.round(this.imageHeight * this.scale);
   }
}
