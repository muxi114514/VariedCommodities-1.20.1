package noppes.vc.network;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import noppes.vc.VariedCommodities;

/**
 * 网络通道。对应 1.12 的 {@code FMLEventChannel} + 手写字节流（{@code Server}/{@code Client}）。
 *
 * <p>原作把所有包挤在一个枚举里靠 ordinal 分发，自己序列化 NBT、自己 GZIP 压缩；
 * 1.20.1 的 {@link SimpleChannel} 已经把编解码、方向校验、线程切换都做好了，
 * 每个包一个类、各自声明方向即可。
 *
 * <p>原作的 6 个包里，{@code TRADE_ACCEPT}/{@code GUI}/{@code GUI_DATA} 服务于交易台，
 * 而交易台按用户决定不移植（见"不移植的内容"一节），故不存在。
 */
public final class VCNetwork {

   private static final String VERSION = "1";

   private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
           .named(new ResourceLocation(VariedCommodities.MODID, "main"))
           .networkProtocolVersion(() -> VERSION)
           .clientAcceptedVersions(VERSION::equals)
           .serverAcceptedVersions(VERSION::equals)
           .simpleChannel();

   private static int nextId;

   private VCNetwork() {
   }

   public static void register() {
      CHANNEL.registerMessage(nextId++, SaveSignPacket.class,
              SaveSignPacket::encode, SaveSignPacket::decode, SaveSignPacket::handle,
              Optional.of(NetworkDirection.PLAY_TO_SERVER));
   }

   public static void toServer(Object packet) {
      CHANNEL.sendToServer(packet);
   }
}
