package noppes.vc.containers;

import noppes.vc.blocks.storage.StorageTier;

/**
 * 大容量箱子界面的几何。菜单摆槽位、界面画背景都从这里取，两边不会各算各的对不上。
 *
 * <p>竖向间距逐项沿用原版 {@code ChestMenu}/{@code ContainerScreen}：标题带 18、每行 18、
 * 箱子与背包之间 13（"物品栏"标签在其中）、背包三行 + 4 间隔 + 快捷栏、底边 7，
 * 故总高 {@code 114 + 行数 × 18}——代入 6 行正是原版六行箱子的 222。
 * 列数超过 9 时面板加宽，玩家背包水平居中。
 */
public record StorageLayout(int columns, int rows) {

   public static final int SLOT = 18;
   private static final int BORDER = 7;
   private static final int PLAYER_COLUMNS = 9;
   private static final int GRID_TOP = 18;
   private static final int GAP_TO_INVENTORY = 13;
   private static final int HOTBAR_OFFSET = 58;

   public static StorageLayout of(StorageTier tier) {
      return new StorageLayout(tier.columns(), tier.rows());
   }

   public int width() {
      return Math.max(PLAYER_COLUMNS, this.columns) * SLOT + 2 * BORDER;
   }

   public int height() {
      return 114 + this.rows * SLOT;
   }

   /** 箱子首格物品的左上角；槽位底图比它向外扩 1 像素。 */
   public int gridX() {
      return (width() - this.columns * SLOT) / 2 + 1;
   }

   public int gridY() {
      return GRID_TOP;
   }

   public int inventoryX() {
      return (width() - PLAYER_COLUMNS * SLOT) / 2 + 1;
   }

   public int inventoryY() {
      return GRID_TOP + this.rows * SLOT + GAP_TO_INVENTORY;
   }

   public int hotbarY() {
      return inventoryY() + HOTBAR_OFFSET;
   }
}
