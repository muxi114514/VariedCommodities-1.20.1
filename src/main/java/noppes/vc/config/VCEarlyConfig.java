package noppes.vc.config;

import java.nio.file.Files;
import java.nio.file.Path;

import com.electronwill.nightconfig.core.file.FileConfig;

import net.minecraftforge.fml.loading.FMLPaths;
import noppes.vc.VariedCommodities;

/**
 * 构造期就要知道答案的配置项。
 *
 * <p><b>为什么不能用 {@link VCConfig}：</b>Forge 加载配置的时机在
 * {@code RegisterEvent} <b>之后</b>，而"附魔要不要注册"必须在注册那一刻就知道。
 * 这里绕过 ForgeConfigSpec，在 mod 构造期用 NightConfig（Forge 自己就是用它实现
 * ForgeConfigSpec 的，不是额外依赖）直接读同一个 toml 文件的同一个键。
 *
 * <p>首次启动时文件还不存在（Forge 随后才会用默认值写出来），此时按内置默认值走；
 * 也就是说<b>改完这一项要重启</b>——注册本来也只发生在启动那一次。
 */
public final class VCEarlyConfig {

   private static final String FILE = VariedCommodities.MODID + "-common.toml";
   private static final String KEY = "general.registerEnchantments";
   /** 与 {@link VCConfig} 里那一项的默认值必须一致。 */
   private static final boolean DEFAULT_REGISTER_ENCHANTMENTS = false;

   private static Boolean cached;

   private VCEarlyConfig() {
   }

   public static boolean registerEnchantments() {
      if (cached == null) {
         cached = read();
      }
      return cached;
   }

   private static boolean read() {
      Path path = FMLPaths.CONFIGDIR.get().resolve(FILE);
      if (!Files.isRegularFile(path)) {
         return DEFAULT_REGISTER_ENCHANTMENTS;
      }
      // 只读不写：FileConfig.of 不开 autosave，close() 不会回写文件，
      // 免得和随后 Forge 自己的读写撞车
      try (FileConfig config = FileConfig.of(path)) {
         config.load();
         return config.getOrElse(KEY, DEFAULT_REGISTER_ENCHANTMENTS);
      } catch (RuntimeException e) {
         // 配置文件坏了不该让整个 mod 起不来，按默认值继续
         VariedCommodities.LOG.warn("读 {} 失败，附魔按默认值处理：{}", FILE, e.toString());
         return DEFAULT_REGISTER_ENCHANTMENTS;
      }
   }
}
