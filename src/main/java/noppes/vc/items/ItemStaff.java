package noppes.vc.items;

import java.util.function.Supplier;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import noppes.vc.entity.EntityMagicProjectile;
import noppes.vc.entity.EntityProjectile;
import noppes.vc.init.VCEnchants;
import noppes.vc.init.VCSounds;
import noppes.vc.init.VCWeapons;

/**
 * 法杖（原作 {@code ItemStaff}）。
 *
 * <p>流程照搬原作：按住右键蓄力 → 蓄满 {@code 20 + 材质等级×8} 刻时消耗一颗 {@code mana}
 * 并在手前生成一颗法术弹 → 之后每刻把它拖到手前 → 松手把它射出去，落点小范围爆炸（不破坏地形）。
 * 法术弹的实体 id 存在物品 NBT 的 {@code MagicProjectile} 里，与原作同名同用法。
 *
 * <p>⚠️ <b>蓄力粒子换成原版的 {@code dust}。</b>原作走自建的 {@code EntityElementalStaffFX}
 * （CustomNPCs 那套 "Spell" 粒子），1.20.1 没有对应物；颜色逐个照搬原作传的那些值——
 * 小于 16 的是染料序号（自然杖的 5=黄绿、12=棕），其余是 RGB。
 */
public class ItemStaff extends Item {

   private final VCTier tier;
   /** 飞出去的那颗长什么样（原作 getProjectile 里那串 if 判物品，改成注册时直接给）。 */
   private final Supplier<Item> spell;
   /** 蓄力粒子的两种颜色，含义见类注释。 */
   private final int colorA;
   private final int colorB;

   public ItemStaff(VCTier tier, Supplier<Item> spell, int colorA, int colorB, Properties props) {
      super(props);
      this.tier = tier;
      this.spell = spell;
      this.colorA = colorA;
      this.colorB = colorB;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      return VCItemUtil.startCharging(level, player, hand);
   }

   @Override
   public int getUseDuration(ItemStack stack) {
      return VCItemUtil.HOLD_FOREVER;
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BOW;
   }

   @Override
   public boolean isEnchantable(ItemStack stack) {
      return true;
   }

   @Override
   public int getEnchantmentValue() {
      return this.tier.getEnchantmentValue();
   }

   /** 蓄满所需刻数：原作 {@code 20 + material.getHarvestLevel() * 8}。 */
   private int chargeTime() {
      return 20 + this.tier.getLevel() * 8;
   }

   @Override
   public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
      int tick = getUseDuration(stack) - remaining;
      if (level.isClientSide) {
         spawnParticles(level, entity);
         return;
      }
      if (tick == chargeTime()) {
         if (entity instanceof Player player && !player.getAbilities().instabuild
                 && !VCEnchants.infinite(stack)) {
            if (!VCItemUtil.consumeItem(player, VCWeapons.MANA.get())) {
               return;
            }
         }
         VCItemUtil.playSound(entity, VCSounds.MAGIC_CHARGE.get(), 1.0F, 1.0F);
         int damage = 6 + (int) this.tier.getAttackDamageBonus() + level.getRandom().nextInt(4);
         damage = (int) (damage + damage * VCEnchants.damage(stack) * 0.5F);

         EntityProjectile spell = new EntityMagicProjectile(level, entity, getProjectile(stack));
         spell.damage = damage;
         spell.poison = VCEnchants.poison(stack);
         spell.confusion = VCEnchants.confusion(stack);
         spell.setSpeed(25);
         spell.setGlows(true);
         holdInFront(spell, entity);
         level.addFreshEntity(spell);
         stack.getOrCreateTag().putInt("MagicProjectile", spell.getId());
      } else if (tick > chargeTime()) {
         EntityProjectile spell = findSpell(level, stack);
         if (spell != null) {
            spell.ticksInAir = 0;   // 一直重置，免得被 timeToLive 掐掉
            holdInFront(spell, entity);
         }
      }
   }

   @Override
   public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      if (level.isClientSide) {
         return;
      }
      EntityProjectile spell = findSpell(level, stack);
      if (spell == null) {
         return;
      }
      stack.removeTagKey("MagicProjectile");
      spell.explosiveDamage = false;   // 只伤生物，不炸地形
      spell.explosiveRadius = 1;
      spell.setYRot(entity.getYRot());
      spell.setXRot(entity.getXRot());
      spell.fire(2.0F);
      VCItemUtil.playSound(entity, VCSounds.MAGIC_SHOT.get(), 1.0F, 1.0F);
   }

   /** 把法术弹拖到施法者手前，公式取自原作。 */
   private static void holdInFront(EntityProjectile spell, LivingEntity entity) {
      float yaw = entity.getYRot() * Mth.DEG_TO_RAD;
      float pitch = entity.getXRot() * Mth.DEG_TO_RAD;
      double dx = -Mth.sin(yaw) * Mth.cos(pitch);
      double dz = Mth.cos(yaw) * Mth.cos(pitch);
      spell.setPos(entity.getX() + dx * 0.8D,
              entity.getY() + 1.5D - entity.getXRot() / 80.0D,
              entity.getZ() + dz * 0.8D);
   }

   private static EntityProjectile findSpell(Level level, ItemStack stack) {
      if (stack.getTag() == null || !stack.getTag().contains("MagicProjectile")) {
         return null;
      }
      Entity entity = level.getEntity(stack.getTag().getInt("MagicProjectile"));
      return entity instanceof EntityProjectile projectile && projectile.isAlive() ? projectile : null;
   }

   /** 元素法杖覆写成对应颜色的法球。 */
   public ItemStack getProjectile(ItemStack stack) {
      return new ItemStack(this.spell.get());
   }

   /** 蓄力时手上冒的粒子。 */
   protected void spawnParticles(Level level, LivingEntity entity) {
      emit(level, entity, this.colorA);
      emit(level, entity, this.colorB);
   }

   protected static void emit(Level level, LivingEntity entity, int color) {
      if (color < 0) {
         return;
      }
      // 小于 16 的是染料序号（原作自然杖传 5/12 就是黄绿与棕），其余直接当 RGB
      float[] rgb = color < 16
              ? DyeColor.byId(color).getTextureDiffuseColors()
              : new float[]{(color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F};
      DustParticleOptions dust = new DustParticleOptions(new Vector3f(rgb[0], rgb[1], rgb[2]), 1.0F);
      float yaw = entity.getYRot() * Mth.DEG_TO_RAD;
      float pitch = entity.getXRot() * Mth.DEG_TO_RAD;
      double dx = -Mth.sin(yaw) * Mth.cos(pitch);
      double dz = Mth.cos(yaw) * Mth.cos(pitch);
      for (int i = 0; i < 2; i++) {
         level.addParticle(dust,
                 entity.getX() + dx * 0.8D + (level.getRandom().nextDouble() - 0.5D) * 0.4D,
                 entity.getY() + 1.5D - entity.getXRot() / 80.0D
                         + (level.getRandom().nextDouble() - 0.5D) * 0.4D,
                 entity.getZ() + dz * 0.8D + (level.getRandom().nextDouble() - 0.5D) * 0.4D,
                 0.0D, 0.0D, 0.0D);
      }
   }
}
