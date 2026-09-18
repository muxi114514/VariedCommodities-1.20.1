package noppes.vc.blocks;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * 方块属性工厂。
 *
 * 1.12 的硬度/抗爆散落在各构造函数里（`BlockBasicContainer` 统一 5.0F/10.0F），
 * 1.20.1 改成不可变的 Properties，集中一处避免 135 个注册项各写各的。
 */
public final class VCBlockProps {

   private VCBlockProps() {
   }

   /** 木质家具：沿用 1.12 BlockBasicContainer 的 5.0F/10.0F。 */
   public static BlockBehaviour.Properties wood() {
      return BlockBehaviour.Properties.of()
              .mapColor(MapColor.WOOD)
              .strength(5.0F, 10.0F)
              .sound(SoundType.WOOD)
              .noOcclusion();
   }

   /** 石质家具（基座等）。原作这类方块无论材质变体如何都用同一套属性，只有贴图不同。 */
   public static BlockBehaviour.Properties stone() {
      return BlockBehaviour.Properties.of()
              .mapColor(MapColor.STONE)
              .strength(5.0F, 10.0F)
              .sound(SoundType.STONE)
              .noOcclusion();
   }

   /**
    * 升级板条箱（原创扩展）：金属质感、镐挖，硬度沿用家具统一的 5.0F。
    * 抗爆值由调用方给——下界合金档要对齐原版下界合金块的 1200。
    */
   public static BlockBehaviour.Properties metalCrate(MapColor color, SoundType sound, float resistance) {
      return BlockBehaviour.Properties.of()
              .mapColor(color)
              .strength(5.0F, resistance)
              .sound(sound)
              .noOcclusion();
   }

   /** 木质光源（蜡烛、台灯）：点燃态亮度 15。 */
   public static BlockBehaviour.Properties woodLight(boolean lit) {
      return wood().lightLevel(state -> lit ? 15 : 0);
   }

   /** 石质光源（篝火）：原作 Material.COBBLESTONE + SoundType.STONE。 */
   public static BlockBehaviour.Properties stoneLight(boolean lit) {
      return stone().lightLevel(state -> lit ? 15 : 0);
   }

   /** 血迹：1.12 Material.GLASS，无碰撞（isPassable 为真）。 */
   public static BlockBehaviour.Properties blood() {
      return BlockBehaviour.Properties.of()
              .mapColor(MapColor.NONE)
              .strength(5.0F, 10.0F)
              .sound(SoundType.GLASS)
              .noCollission()
              .noOcclusion();
   }

   /** 高脚灯：恒亮（1.12 setLightLevel(1.0F) → 15）。材质是木头但音效用石头，原作如此。 */
   public static BlockBehaviour.Properties tallLamp() {
      return BlockBehaviour.Properties.of()
              .mapColor(MapColor.WOOD)
              .strength(5.0F, 10.0F)
              .sound(SoundType.STONE)
              .lightLevel(state -> 15)
              .noOcclusion();
   }

   /** 水晶：1.12 setLightLevel(0.8F) 在内部换算成 (int)(15*0.8)=12，此处直接写 12。 */
   public static BlockBehaviour.Properties crystal(DyeColor color) {
      return BlockBehaviour.Properties.of()
              .mapColor(color)
              .strength(5.0F, 10.0F)
              .sound(SoundType.GLASS)
              .lightLevel(state -> 12)
              .noOcclusion();
   }

   /** 占位方块：1.12 只设了音效和创造栏，硬度走 Block 默认值 0（瞬破）。 */
   public static BlockBehaviour.Properties placeholder() {
      return BlockBehaviour.Properties.of()
              .mapColor(MapColor.STONE)
              .strength(0.0F)
              .sound(SoundType.WOOD);
   }
}
