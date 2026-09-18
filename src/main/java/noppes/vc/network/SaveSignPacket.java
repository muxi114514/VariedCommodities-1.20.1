package noppes.vc.network;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import noppes.vc.blocks.tiles.TileBigSign;

/**
 * 客户端编辑完文本后回传给服务端（对应 1.12 的 {@code PacketServer.SAVE_SIGN}）。
 *
 * <p><b>服务端必须把这个包当作不可信输入。</b>坐标来自客户端，恶意客户端可以填任意位置，
 * 因此落地前做三重校验：
 * <ol>
 *   <li>发送者存在；</li>
 *   <li>目标区块<b>已加载</b>——直接 {@code getBlockEntity} 会触发同步加载甚至生成，卡死主线程；</li>
 *   <li>玩家距离在 8 格内——否则等于给了远程改写任意牌子的能力。</li>
 * </ol>
 * 能否写入还要再过方块实体自己的一次性锁（见 {@link TileBigSign#acceptText}）。
 */
public record SaveSignPacket(BlockPos pos, String text) {

   /** 与 {@link TileBigSign} 的上限一致，防止超长字符串撑爆包体。 */
   private static final int MAX_LENGTH = 4096;
   private static final double MAX_DISTANCE_SQR = 64.0D;

   public static void encode(SaveSignPacket packet, FriendlyByteBuf buf) {
      buf.writeBlockPos(packet.pos());
      buf.writeUtf(packet.text(), MAX_LENGTH);
   }

   public static SaveSignPacket decode(FriendlyByteBuf buf) {
      return new SaveSignPacket(buf.readBlockPos(), buf.readUtf(MAX_LENGTH));
   }

   public static void handle(SaveSignPacket packet, Supplier<NetworkEvent.Context> ctx) {
      ctx.get().enqueueWork(() -> {
         ServerPlayer player = ctx.get().getSender();
         if (player == null) {
            return;
         }
         if (!player.level().isLoaded(packet.pos())) {
            return;
         }
         if (player.distanceToSqr(Vec3.atCenterOf(packet.pos())) > MAX_DISTANCE_SQR) {
            return;
         }
         if (player.level().getBlockEntity(packet.pos()) instanceof TileBigSign sign) {
            sign.acceptText(packet.text(), player);
         }
      });
      ctx.get().setPacketHandled(true);
   }
}
