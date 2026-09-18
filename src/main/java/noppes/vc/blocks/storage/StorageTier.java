package noppes.vc.blocks.storage;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

/**
 * 板条箱容量档位（本项目原创扩展，1.12 原作只有 54 格的木箱）。
 *
 * <p>一档 = 界面列 × 行 + 升级材料。材料按标签匹配而非具体物品，整合包里的等价块/锭同样认；
 * {@link #displayMaterial} 只用于提示文字与 JEI 展示。
 * 加一档只需在这里加一行，再到 {@code VCBlocks.UPGRADED_CRATE} 注册对应方块。
 *
 * <p><b>容量是存档结构的一部分</b>（方块实体按它分配槽位），所以写死在这里而不做成配置：
 * 调小之后，已存进高位槽的物品无处安放。
 */
public enum StorageTier implements StringRepresentable {
   WOOD(9, 6, null, null),
   IRON(9, 9, Tags.Items.STORAGE_BLOCKS_IRON, Items.IRON_BLOCK),
   DIAMOND(12, 9, Tags.Items.STORAGE_BLOCKS_DIAMOND, Items.DIAMOND_BLOCK),
   NETHERITE(15, 9, Tags.Items.INGOTS_NETHERITE, Items.NETHERITE_INGOT);

   private static final StorageTier[] VALUES = values();

   private final int columns;
   private final int rows;
   @Nullable
   private final TagKey<Item> material;
   @Nullable
   private final Item displayMaterial;

   StorageTier(int columns, int rows, @Nullable TagKey<Item> material, @Nullable Item displayMaterial) {
      this.columns = columns;
      this.rows = rows;
      this.material = material;
      this.displayMaterial = displayMaterial;
   }

   public int columns() {
      return this.columns;
   }

   public int rows() {
      return this.rows;
   }

   public int size() {
      return this.columns * this.rows;
   }

   /** 恰好是原版六行箱子的尺寸：直接用原版菜单与界面，行为与改动前完全一致。 */
   public boolean usesVanillaMenu() {
      return this.columns == 9 && this.rows == 6;
   }

   /** 升级到本档所需的材料；木质档不是升级目标，返回 null。 */
   @Nullable
   public TagKey<Item> material() {
      return this.material;
   }

   @Nullable
   public Item displayMaterial() {
      return this.displayMaterial;
   }

   public boolean isAbove(StorageTier other) {
      return ordinal() > other.ordinal();
   }

   /** 比本档更高、可作为升级目标的档位，按从低到高排列。 */
   public List<StorageTier> higher() {
      return Arrays.stream(VALUES).filter(tier -> tier.isAbove(this)).toList();
   }

   @Override
   public String getSerializedName() {
      return name().toLowerCase(Locale.ROOT);
   }

   /** 网络下发的档位序号；越界时夹到合法范围，不信任任何外来数字。 */
   public static StorageTier byId(int id) {
      return VALUES[Mth.clamp(id, 0, VALUES.length - 1)];
   }

   /** 手持物是哪一档的升级材料；不是任何一档则返回 null。每次右键方块都会调，故不分配对象。 */
   @Nullable
   public static StorageTier byMaterial(ItemStack stack) {
      for (StorageTier tier : VALUES) {
         if (tier.material != null && stack.is(tier.material)) {
            return tier;
         }
      }
      return null;
   }
}
