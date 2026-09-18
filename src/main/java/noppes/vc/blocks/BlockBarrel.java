package noppes.vc.blocks;

import noppes.vc.blocks.storage.StorageTier;

/**
 * 木桶：与板条箱完全相同，只是旋转细分为 8 档（原作 {@code maxRotation()} 返回 8）。
 *
 * {@code maxRotation()} 只能返回字面常量，原因见 {@link BlockBasicRotated#maxRotation()}。
 */
public class BlockBarrel extends BlockCrate {

   public BlockBarrel(Properties props) {
      super(props, StorageTier.WOOD);
   }

   @Override
   protected int maxRotation() {
      return 8;
   }

   /** 升级只做板条箱（用户决定），木桶恒为 54 格。 */
   @Override
   public boolean upgradable() {
      return false;
   }
}
