package noppes.vc.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.VariedCommodities;

/**
 * 音效注册。注册名与 {@code assets/variedcommodities/sounds.json} 的键逐一对应。
 *
 * <p>1.12 的 {@code VCSounds} 注册了 8 个（{@code client/VCSounds.java}），
 * 而 sounds.json 声明了 9 条——{@code misc.old_explode} 在原作源码里零引用，是个孤儿条目。
 * 这里把它一并注册：多一个注册项零成本，反而让那条已经打包进资源的声明变得可用。
 *
 * <p>枪械与法杖的机制都要用到这些音效，故本类是它们的前置。
 */
public final class VCSounds {

   public static final DeferredRegister<SoundEvent> SOUNDS =
           DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, VariedCommodities.MODID);

   public static final RegistryObject<SoundEvent> GUN_EMPTY = sound("gun.empty");
   public static final RegistryObject<SoundEvent> GUN_AK47_LOAD = sound("gun.ak47.load");
   public static final RegistryObject<SoundEvent> GUN_PISTOL_SHOT = sound("gun.pistol.shot");
   public static final RegistryObject<SoundEvent> GUN_PISTOL_TRIGGER = sound("gun.pistol.trigger");
   public static final RegistryObject<SoundEvent> MAGIC_CHARGE = sound("magic.charge");
   public static final RegistryObject<SoundEvent> MAGIC_SHOT = sound("magic.shot");
   public static final RegistryObject<SoundEvent> MISC_SWOSH = sound("misc.swosh");
   public static final RegistryObject<SoundEvent> MISC_HALLELUJAH = sound("misc.hallelujah");
   /** 原作声明了但从未注册、也从未使用。 */
   public static final RegistryObject<SoundEvent> MISC_OLD_EXPLODE = sound("misc.old_explode");

   private VCSounds() {
   }

   public static void register(IEventBus bus) {
      SOUNDS.register(bus);
   }

   private static RegistryObject<SoundEvent> sound(String name) {
      return SOUNDS.register(name,
              () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(VariedCommodities.MODID, name)));
   }
}
