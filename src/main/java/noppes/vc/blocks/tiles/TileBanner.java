package noppes.vc.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import noppes.vc.init.VCBlockEntities;

/**
 * 横幅 / 壁挂横幅的方块实体：只存徽记与编辑窗口起始时刻。
 *
 * <p>颜色不在这里——它已经进了 blockstate（见 {@link noppes.vc.blocks.VCDye}）。
 * 朝向同理（P3）。所以这个方块实体只剩下"一个物品 + 一个时间戳"。
 *
 * <p>徽记的玩法：放下横幅后有 10 秒窗口，期间右键任意物品即把它设为徽记；
 * 超时后右键才走染色逻辑。NBT 键沿用原作的 {@code BannerIcon} / {@code EditTime}。
 *
 * <p>时间戳用的是真实时钟（{@code System.currentTimeMillis}）而非游戏刻，逐字沿用原作。
 * 副作用是重进存档后窗口必然已过期——这恰好是想要的行为。
 */
public class TileBanner extends BlockEntity {

   private static final long EDIT_WINDOW_MS = 10_000L;

   private ItemStack icon = ItemStack.EMPTY;
   private long editStarted;

   public TileBanner(BlockPos pos, BlockState state) {
      super(VCBlockEntities.BANNER.get(), pos, state);
   }

   /** 供 P5 的渲染器取用。 */
   public ItemStack icon() {
      return this.icon;
   }

   /** 放置时开启编辑窗口。 */
   public void startEditWindow() {
      this.editStarted = System.currentTimeMillis();
      setChanged();
   }

   public boolean canEdit() {
      return System.currentTimeMillis() - this.editStarted < EDIT_WINDOW_MS;
   }

   /**
    * 编辑窗口内把手持物设为徽记。返回是否消费掉了这次右键。
    *
    * 不限制物品类型：原作同样来者不拒，"方块不管用"是渲染层的事
    * （渲染器跳过 ItemBlock），不是这里的判断。
    */
   public boolean tryEditIcon(ItemStack held) {
      if (!canEdit() || held.isEmpty()) {
         return false;
      }
      this.icon = held.copy();
      setChanged();
      return true;
   }

   @Override
   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      if (!this.icon.isEmpty()) {
         tag.put("BannerIcon", this.icon.save(new CompoundTag()));
      }
      tag.putLong("EditTime", this.editStarted);
   }

   @Override
   public void load(CompoundTag tag) {
      super.load(tag);
      this.icon = tag.contains("BannerIcon", Tag.TAG_COMPOUND)
              ? ItemStack.of(tag.getCompound("BannerIcon"))
              : ItemStack.EMPTY;
      this.editStarted = tag.getLong("EditTime");
   }

   /** 徽记要在世界里画出来，必须同步。 */
   @Override
   public CompoundTag getUpdateTag() {
      CompoundTag tag = new CompoundTag();
      saveAdditional(tag);
      return tag;
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }
}
