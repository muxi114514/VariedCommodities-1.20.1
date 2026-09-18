package noppes.vc.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import noppes.vc.init.VCBlockEntities;

/**
 * 可刻字方块的方块实体（大告示牌、墓碑）。
 *
 * <p>只存文本与一个"是否还能改"的锁——朝向已在 blockstate（P3），
 * 原作 NBT 里的 {@code SignRotation} 因此不再需要，{@code SignText} 保留原键名。
 *
 * <p><b>一次性写入锁。</b>原作的 {@code canEdit} 初始为 true，
 * 第一次保存文本后置 false，此后拒绝写入（{@code PacketHandlerServer:75-77}），
 * 唯一的解锁方式是 CustomNPCs 的 npcwand。这里做了两处调整：
 * <ul>
 *   <li><b>锁写进 NBT。</b>原作没存，重载世界就重置为 true——那个锁实际只在区块加载期间有效，
 *       形同虚设。存下来它才真是一次性的。</li>
 *   <li><b>解锁钥匙换成创造模式。</b>npcwand 属于 CustomNPCs，独立安装下根本不存在，
 *       照搬等于"写错了只能拆掉重放"。创造模式是管理员身份的自然替代。</li>
 * </ul>
 */
public class TileBigSign extends BlockEntity {

   /** 与 {@code SaveSignPacket} 的上限一致。 */
   public static final int MAX_LENGTH = 4096;

   private String text = "";
   private boolean canEdit = true;

   public TileBigSign(BlockPos pos, BlockState state) {
      super(VCBlockEntities.BIG_SIGN.get(), pos, state);
   }

   public String text() {
      return this.text;
   }

   /** 生存玩家只有一次机会；创造模式随时可改。 */
   public boolean mayEdit(Player player) {
      return this.canEdit || player.getAbilities().instabuild;
   }

   /** 由 {@code SaveSignPacket} 在服务端调用。是否放行仍由这里说了算，不信客户端。 */
   public void acceptText(String newText, Player player) {
      if (!mayEdit(player)) {
         return;
      }
      this.text = newText.length() > MAX_LENGTH ? newText.substring(0, MAX_LENGTH) : newText;
      this.canEdit = false;
      setChanged();
      if (this.level != null) {
         BlockState state = getBlockState();
         this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
      }
   }

   @Override
   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putString("SignText", this.text);
      tag.putBoolean("CanEdit", this.canEdit);
   }

   @Override
   public void load(CompoundTag tag) {
      super.load(tag);
      this.text = tag.getString("SignText");
      // 缺键时按 true 处理：老存档/新放置的牌子都该是可写的
      this.canEdit = !tag.contains("CanEdit") || tag.getBoolean("CanEdit");
   }

   /** 文本要在世界里画出来，也要让客户端知道还能不能改，两者都得同步。 */
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
