package noppes.vc.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.VariedCommodities;
import noppes.vc.entity.EntityChairMount;
import noppes.vc.entity.EntityHolyHandGrenade;
import noppes.vc.entity.EntityMagicProjectile;
import noppes.vc.entity.EntityProjectile;

/** 实体注册（P2/P4：椅子挂载、投射物、魔法弹、圣手雷）。 */
public final class VCEntities {
   public static final DeferredRegister<EntityType<?>> ENTITIES =
           DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, VariedCommodities.MODID);

   /** 落座用的隐形坐骑。noSummon：不该出现在 /summon 里，它只由方块生成。 */
   public static final RegistryObject<EntityType<EntityChairMount>> CHAIR_MOUNT =
           ENTITIES.register("chair_mount", () -> EntityType.Builder
                   .<EntityChairMount>of(EntityChairMount::new, MobCategory.MISC)
                   .sized(0.0F, 0.0F)
                   .noSummon()
                   .clientTrackingRange(10)
                   .build("chair_mount"));

   /**
    * 通用投射物：苦无/手里剑/投掷武器/子弹/弹弓石块都是它。
    *
    * <p>⚠️ 碰撞箱取 0.25（原作由实体投出时是 1.0，那是 CustomNPCs 那套 size 旋钮的默认值，
    * VC 从没调过）。命中判定走射线，箱子只影响穿方块检测，一个苦无一格见方不合理。
    * 追踪频率 {@code updateInterval=1}：飞行物每刻都要同步位置，否则客户端看着一跳一跳。
    */
   public static final RegistryObject<EntityType<EntityProjectile>> PROJECTILE =
           ENTITIES.register("projectile", () -> EntityType.Builder
                   .<EntityProjectile>of(EntityProjectile::new, MobCategory.MISC)
                   .sized(0.25F, 0.25F)
                   .clientTrackingRange(4)
                   .updateInterval(1)
                   .build("projectile"));

   /** 法杖蓄力时悬在手前的那颗，换手里的东西就消失。 */
   public static final RegistryObject<EntityType<EntityMagicProjectile>> MAGIC_PROJECTILE =
           ENTITIES.register("magic_projectile", () -> EntityType.Builder
                   .<EntityMagicProjectile>of(EntityMagicProjectile::new, MobCategory.MISC)
                   .sized(0.25F, 0.25F)
                   .clientTrackingRange(4)
                   .updateInterval(1)
                   .build("magic_projectile"));

   /** 圣手雷：落地 34 刻后炸，威力走配置项。 */
   public static final RegistryObject<EntityType<EntityHolyHandGrenade>> HOLY_HAND_GRENADE =
           ENTITIES.register("holy_hand_grenade", () -> EntityType.Builder
                   .<EntityHolyHandGrenade>of(EntityHolyHandGrenade::new, MobCategory.MISC)
                   .sized(0.25F, 0.25F)
                   .fireImmune()
                   .clientTrackingRange(4)
                   .updateInterval(1)
                   .build("holy_hand_grenade"));

   private VCEntities() {
   }

   public static void register(IEventBus bus) {
      ENTITIES.register(bus);
   }
}
