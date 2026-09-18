package noppes.vc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import noppes.vc.config.VCConfig;
import noppes.vc.init.VCEntities;
import noppes.vc.init.VCSounds;
import noppes.vc.items.VCItemUtil;

/**
 * 安提阿的圣手雷（原作 {@code EntityHolyHandGrenade}，525 行）。
 *
 * <p>⚠️ 原作那 525 行是<b>把 EntityProjectile 整个复制了一份再改三处</b>——
 * 同一套同步字段、同一套飞行与命中代码。这里改成继承，只留真正不同的三点：
 * <ol>
 * <li>撞到生物<b>不造成伤害，反弹回去</b>（原作 onImpact 里 entityHit 分支只把速度取反）；</li>
 * <li>插进方块时放一声哈利路亚；</li>
 * <li>落地 {@code timeToLive} 刻（投掷时设 34）之后<b>爆炸</b>，威力走配置项。</li>
 * </ol>
 * 这也是为什么基类要留 {@code onTimeExpired} / {@code onStuck} 两个钩子。
 *
 * <p>⚠️ 防火由 EntityType 的 {@code fireImmune()} 提供。原作那句
 * {@code projectile.isImmuneToFire();} 其实是<b>调了个 getter 却丢掉返回值</b>，
 * 一点作用没有；照作者的意图补上，不照抄那行空语句。
 */
public class EntityHolyHandGrenade extends EntityProjectile {

   public EntityHolyHandGrenade(EntityType<? extends EntityHolyHandGrenade> type, Level level) {
      super(type, level);
   }

   public EntityHolyHandGrenade(Level level, LivingEntity thrower, ItemStack display) {
      super(VCEntities.HOLY_HAND_GRENADE.get(), level, thrower, display);
   }

   /** 砸到人只会弹开，真正伤人的是随后那一炸。 */
   @Override
   protected void onHitEntity(EntityHitResult result) {
      setDeltaMovement(getDeltaMovement().scale(-0.1D));
      setYRot(getYRot() + 180.0F);
      this.yRotO += 180.0F;
      this.ticksInAir = 0;
   }

   @Override
   protected void onStuck(BlockPos pos) {
      VCItemUtil.playSound(this, VCSounds.MISC_HALLELUJAH.get(), 4.0F, 1.0F);
   }

   @Override
   protected void onTimeExpired() {
      if (!this.level().isClientSide) {
         boolean griefing = net.minecraftforge.event.ForgeEventFactory
                 .getMobGriefingEvent(this.level(), getOwner());
         this.level().explode(this, getX(), getY(), getZ(),
                 VCConfig.holyHandGrenadeStrength(),
                 griefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
      }
      discard();
   }
}
