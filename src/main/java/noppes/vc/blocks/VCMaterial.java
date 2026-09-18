package noppes.vc.blocks;

import net.minecraft.util.StringRepresentable;

/**
 * 家具材质档位。
 *
 * 对应 1.12 的 meta 0~4（{@code BlockRendererBasic#setMaterialTexture} 的分支顺序），
 * 用于横幅、壁挂横幅、高脚灯、基座。
 * 注意这一档只改贴图，不改方块属性——原作这几个方块无论哪种材质都用同一套硬度与音效。
 */
public enum VCMaterial implements StringRepresentable {
   WOOD("wood"),
   STONE("stone"),
   IRON("iron"),
   GOLD("gold"),
   DIAMOND("diamond");

   private final String name;

   VCMaterial(String name) {
      this.name = name;
   }

   @Override
   public String getSerializedName() {
      return this.name;
   }
}
