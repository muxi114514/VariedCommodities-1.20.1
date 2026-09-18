package noppes.vc.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import noppes.vc.VariedCommodities;
import noppes.vc.containers.CarpentryMenu;

/**
 * 木工台界面。
 *
 * 原作的 {@code GuiCarpentryBench} 继承自制的 {@code GuiContainerBasic}，后者负责画格子背景；
 * 1.20.1 的 {@link AbstractContainerScreen} 本来就会画槽位，于是只剩"贴背景图 + 两行标题"。
 * 贴图与尺寸（176×180）沿用原作。
 */
public class ScreenCarpentry extends AbstractContainerScreen<CarpentryMenu> {

   private static final ResourceLocation TEXTURE =
           new ResourceLocation(VariedCommodities.MODID, "textures/gui/carpentry.png");

   public ScreenCarpentry(CarpentryMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageHeight = 180;
      this.inventoryLabelY = this.imageHeight - 93;
   }

   @Override
   protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      renderBackground(graphics);
      super.render(graphics, mouseX, mouseY, partialTick);
      renderTooltip(graphics, mouseX, mouseY);
   }
}
