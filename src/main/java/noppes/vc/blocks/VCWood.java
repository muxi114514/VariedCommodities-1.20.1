package noppes.vc.blocks;

import net.minecraft.util.StringRepresentable;

/**
 * 家具木材档位。
 *
 * 对应 1.12 的 meta 0~5（`BlockRendererBasic#setWoodTexture` 的分支顺序），
 * 枚举声明顺序即 meta 顺序，创造栏排列据此保持与原版一致。
 * `BIG_OAK` 在 1.20.1 原版已改名 dark_oak，这里跟新版对齐。
 */
public enum VCWood implements StringRepresentable {
   OAK("oak"),
   SPRUCE("spruce"),
   BIRCH("birch"),
   JUNGLE("jungle"),
   ACACIA("acacia"),
   DARK_OAK("dark_oak");

   private final String name;

   VCWood(String name) {
      this.name = name;
   }

   @Override
   public String getSerializedName() {
      return this.name;
   }
}
