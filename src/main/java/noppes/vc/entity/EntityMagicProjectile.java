package noppes.vc.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import noppes.vc.init.VCEntities;

/**
 * 法杖蓄力时悬在手前的那颗法术弹（原作 {@code EntityMagicProjectile}）。
 *
 * <p>与普通投射物的唯一差别：<b>施法者一换手里的东西它就消失</b>。
 * 原作比的是 {@code player.getHeldItemMainhand() != equiped} —— <b>引用比较</b>，
 * 所以切换物品栏、丢出、耐久变化都算"换了"，这里照搬（`!=` 而非 equals）。
 */
public class EntityMagicProjectile extends EntityProjectile {

   private ItemStack caster = ItemStack.EMPTY;

   public EntityMagicProjectile(EntityType<? extends EntityMagicProjectile> type, Level level) {
      super(type, level);
   }

   public EntityMagicProjectile(Level level, LivingEntity caster, ItemStack display) {
      super(VCEntities.MAGIC_PROJECTILE.get(), level, caster, display);
      this.caster = caster.getMainHandItem();
   }

   @Override
   public void tick() {
      if (!this.level().isClientSide
              && (!(getOwner() instanceof LivingEntity owner) || owner.getMainHandItem() != this.caster)) {
         discard();
         return;
      }
      super.tick();
   }
}
