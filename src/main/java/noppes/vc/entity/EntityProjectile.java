package noppes.vc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import noppes.vc.init.VCEntities;

/**
 * 通用投射物（原作 {@code noppes.vc.EntityProjectile}，709 行）。
 *
 * <p>苦无、手里剑、投掷武器、子弹、弹弓石块、法术弹全走这一个类，
 * 差别只在几个开关：是否受重力、是否 3D 显示、是否插墙、是否旋转、速度、击退、爆炸半径。
 *
 * <p>与原作的三处改动：
 * <ol>
 * <li><b>回调换成序列化的附魔等级。</b>原作用匿名 {@code IProjectileCallback} 捕获枪/法杖的
 *     ItemStack 来施加中毒与混乱，那个字段既不存盘（存档一读就没了），又让实体长期持有一份物品引用。
 *     这里改存 {@code poison}/{@code confusion} 两个整数，存盘、同步、无引用。</li>
 * <li><b>投掷者用原版的 {@code Projectile#setOwner}</b>，UUID 的存取与跨区块解析原版已经做好了，
 *     不必像原作那样自己维护 {@code throwerName}。</li>
 * <li><b>碰撞箱固定 0.25</b>。原作由实体投出时是 1.0×1.0（CustomNPCs 那套 size 旋钮的默认值，VC 从没调过），
 *     一个苦无一格见方不合理；命中判定本就是射线，箱子大小只影响 {@code checkInsideBlocks}。</li>
 * </ol>
 */
public class EntityProjectile extends ThrowableProjectile {

   private static final EntityDataAccessor<ItemStack> DATA_ITEM =
           SynchedEntityData.defineId(EntityProjectile.class, EntityDataSerializers.ITEM_STACK);
   /** 速度×10，与原作一致（原作存整数，除以 10 用）。 */
   private static final EntityDataAccessor<Integer> DATA_SPEED =
           SynchedEntityData.defineId(EntityProjectile.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_GRAVITY =
           SynchedEntityData.defineId(EntityProjectile.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_GLOWS =
           SynchedEntityData.defineId(EntityProjectile.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_3D =
           SynchedEntityData.defineId(EntityProjectile.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_ROTATING =
           SynchedEntityData.defineId(EntityProjectile.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_STICKS =
           SynchedEntityData.defineId(EntityProjectile.class, EntityDataSerializers.BOOLEAN);

   /** 原作 {@code EntityThrowable#getGravityVelocity()} 就是这个值。 */
   private static final float GRAVITY = 0.03F;

   public float damage = 5.0F;
   public int punch;
   public boolean accelerate;
   /** 爆炸是否破坏地形（法杖的爆炸不破坏）。 */
   public boolean explosiveDamage = true;
   public int explosiveRadius;
   /** 空中能飞多久，到点直接消失。 */
   public int timeToLive = 1200;
   /** 插在方块上能待多久，到点调 {@link #onTimeExpired()}（圣手雷就是靠它定时炸的）。 */
   public int groundTimeToLive = 1200;
   public boolean destroyedOnEntityHit = true;
   public boolean canBePickedUp;
   /** 取代原作的 IProjectileCallback：命中生物时按等级判定是否附加效果。 */
   public int poison;
   public int confusion;

   private Vec3 acceleration = Vec3.ZERO;
   private boolean inGround;
   private BlockPos stuckAt = BlockPos.ZERO;
   private int ticksInGround;
   public int ticksInAir;
   /** 插进方块后的抖动计时，抖完才能被捡起（原作 arrowShake）。 */
   private int shake;
   private boolean announced;

   public EntityProjectile(EntityType<? extends EntityProjectile> type, Level level) {
      super(type, level);
   }

   public EntityProjectile(Level level, LivingEntity thrower, ItemStack display) {
      this(VCEntities.PROJECTILE.get(), level, thrower, display);
   }

   /** 子类（法术弹）要能传自己的 EntityType，否则会被注册成普通投射物。 */
   protected EntityProjectile(EntityType<? extends EntityProjectile> type, Level level,
                              LivingEntity thrower, ItemStack display) {
      super(type, thrower, level);
      setItemDisplay(display);
      // 原作把出生点往身后挪一点，免得贴脸生成
      double yaw = getYRot() * Mth.DEG_TO_RAD;
      setPos(getX() - Mth.cos((float) yaw) * 0.1D, getY() - 0.1D,
              getZ() - Mth.sin((float) yaw) * 0.1D);
   }

   @Override
   protected void defineSynchedData() {
      this.entityData.define(DATA_ITEM, ItemStack.EMPTY);
      this.entityData.define(DATA_SPEED, 10);
      this.entityData.define(DATA_GRAVITY, false);
      this.entityData.define(DATA_GLOWS, false);
      this.entityData.define(DATA_3D, false);
      this.entityData.define(DATA_ROTATING, false);
      this.entityData.define(DATA_STICKS, false);
   }

   // ── 开关 ─────────────────────────────────────────────────────────────

   public void setItemDisplay(ItemStack stack) {
      this.entityData.set(DATA_ITEM, stack.copy());
   }

   public ItemStack getItemDisplay() {
      return this.entityData.get(DATA_ITEM);
   }

   /** 原作的 speed 是整数、用时除以 10。 */
   public void setSpeed(int speed) {
      this.entityData.set(DATA_SPEED, speed);
   }

   public float getSpeed() {
      return this.entityData.get(DATA_SPEED) / 10.0F;
   }

   public void setHasGravity(boolean value) {
      this.entityData.set(DATA_GRAVITY, value);
   }

   public boolean hasGravity() {
      return this.entityData.get(DATA_GRAVITY);
   }

   public void setGlows(boolean value) {
      this.entityData.set(DATA_GLOWS, value);
   }

   public boolean glows() {
      return this.entityData.get(DATA_GLOWS);
   }

   public void setIs3D(boolean value) {
      this.entityData.set(DATA_3D, value);
   }

   /** 方块永远按 3D 画——原作 {@code is3D()} 里就带这个或。 */
   public boolean is3D() {
      return this.entityData.get(DATA_3D) || isBlock();
   }

   public void setRotating(boolean value) {
      this.entityData.set(DATA_ROTATING, value);
   }

   public boolean isRotating() {
      return this.entityData.get(DATA_ROTATING);
   }

   public void setStickInWall(boolean value) {
      this.entityData.set(DATA_STICKS, value);
   }

   public boolean sticksToWalls() {
      return is3D() && this.entityData.get(DATA_STICKS);
   }

   public boolean isBlock() {
      return getItemDisplay().getItem() instanceof BlockItem;
   }

   /** 原作把「是不是箭」同步成一个字段；其实由物品直接推得出来，不必占一条同步项。 */
   public boolean isArrow() {
      return getItemDisplay().is(Items.ARROW);
   }

   public boolean isInGround() {
      return this.inGround;
   }

   // ── 发射 ─────────────────────────────────────────────────────────────

   /**
    * 按自身的朝向发射（原作 {@code shoot(float)}）。
    * {@code inaccuracy} 越大散布越广，原作枪械传的是材质伤害+1。
    */
   public void fire(float inaccuracy) {
      float yaw = getYRot() * Mth.DEG_TO_RAD;
      float pitch = getXRot() * Mth.DEG_TO_RAD;
      fire(-Mth.sin(yaw) * Mth.cos(pitch), -Mth.sin(pitch), Mth.cos(yaw) * Mth.cos(pitch),
              -getXRot(), inaccuracy);
   }

   public void fire(double dx, double dy, double dz, float gravityPitch, float inaccuracy) {
      double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (len < 1.0E-7D) {
         return;
      }
      double flat = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float) (Math.atan2(dx, dz) * Mth.RAD_TO_DEG);
      // 有重力时按给定的仰角抛射，没有重力就直指目标（原作两条分支）
      float pitch = hasGravity() ? gravityPitch : (float) (Math.atan2(dy, flat) * Mth.RAD_TO_DEG);
      setYRot(yaw);
      setXRot(pitch);
      this.yRotO = yaw;
      this.xRotO = pitch;

      float speed = getSpeed();
      double mx = Mth.sin(yaw * Mth.DEG_TO_RAD) * Mth.cos(pitch * Mth.DEG_TO_RAD);
      double mz = Mth.cos(yaw * Mth.DEG_TO_RAD) * Mth.cos(pitch * Mth.DEG_TO_RAD);
      double my = Mth.sin((pitch + 1.0F) * Mth.DEG_TO_RAD);
      mx += this.random.nextGaussian() * 0.0075D * inaccuracy;
      my += this.random.nextGaussian() * 0.0075D * inaccuracy;
      mz += this.random.nextGaussian() * 0.0075D * inaccuracy;
      setDeltaMovement(mx * speed, my * speed, mz * speed);
      this.acceleration = new Vec3(dx / len * 0.1D, dy / len * 0.1D, dz / len * 0.1D);
      this.ticksInGround = 0;
   }

   // ── tick ─────────────────────────────────────────────────────────────

   @Override
   public void tick() {
      // 不能走 super.tick()——ThrowableProjectile 会自己跑一遍移动与命中；
      // 但 Projectile.tick() 里那声 PROJECTILE_SHOOT（幽匿感测体要听）得自己补上
      if (!this.announced) {
         gameEvent(GameEvent.PROJECTILE_SHOOT, getOwner());
         this.announced = true;
      }
      baseTick();
      if (this.shake > 0) {
         this.shake--;
      }
      if (this.inGround) {
         tickInGround();
         return;
      }

      this.ticksInAir++;
      if (this.ticksInAir >= this.timeToLive) {
         discard();
         return;
      }

      HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
      if (hit.getType() != HitResult.Type.MISS
              && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hit)) {
         setRotating(false);
         onHit(hit);
         if (isRemoved()) {
            return;
         }
      }

      Vec3 motion = getDeltaMovement();
      setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
      updateFlightRotation(motion);

      float drag = this.accelerate ? 0.95F : 1.0F;
      if (isInWater()) {
         for (int i = 0; i < 4; i++) {
            this.level().addParticle(ParticleTypes.BUBBLE, getX() - motion.x * 0.25D,
                    getY() - motion.y * 0.25D, getZ() - motion.z * 0.25D, motion.x, motion.y, motion.z);
         }
         drag = 0.8F;
      }
      motion = getDeltaMovement().scale(drag);
      if (hasGravity()) {
         motion = motion.subtract(0.0D, GRAVITY, 0.0D);
      }
      if (this.accelerate) {
         motion = motion.add(this.acceleration);
      }
      setDeltaMovement(motion);
      checkInsideBlocks();
   }

   /** 插在方块上：原方块一变就脱落，否则数到 timeToLive 消失（原作同款）。 */
   private void tickInGround() {
      BlockState state = this.level().getBlockState(this.stuckAt);
      if (state.isAir()) {
         this.inGround = false;
         setDeltaMovement(getDeltaMovement().scale(this.random.nextFloat() * 0.2F));
         this.ticksInGround = 0;
         this.ticksInAir = 0;
         return;
      }
      this.ticksInGround++;
      if (this.ticksInGround >= this.groundTimeToLive) {
         onTimeExpired();
      }
   }

   private void updateFlightRotation(Vec3 motion) {
      this.yRotO = getYRot();
      this.xRotO = getXRot();
      float flat = Mth.sqrt((float) (motion.x * motion.x + motion.z * motion.z));
      setYRot((float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
      float pitch = (float) (Mth.atan2(motion.y, flat) * Mth.RAD_TO_DEG);
      if (isRotating()) {
         // 旋转的投掷物翻滚：方块慢一半，与原作的 10/20 两档一致
         pitch -= (this.ticksInAir % 15) * (isBlock() ? 10 : 20) * getSpeed();
      }
      setXRot(pitch);
   }

   /** 原作允许 25 刻后打到自己（抛物线落回头上）。 */
   @Override
   protected boolean canHitEntity(Entity target) {
      if (target == getOwner()) {
         return this.ticksInAir >= 25;
      }
      return super.canHitEntity(target);
   }

   @Override
   protected float getGravity() {
      return hasGravity() ? GRAVITY : 0.0F;
   }

   // ── 命中 ─────────────────────────────────────────────────────────────

   @Override
   protected void onHitEntity(EntityHitResult result) {
      Entity target = result.getEntity();
      if (target instanceof Player player && (player.getAbilities().invulnerable
              || getOwner() instanceof Player owner && !owner.canHarmPlayer(player))) {
         return;
      }
      float dealt = this.damage == 0.0F ? 0.001F : this.damage;
      if (!target.hurt(damageSources().thrown(this, getOwner()), dealt)) {
         // 打不动（无敌帧/免疫）时插墙类投射物会弹回来，原作如此
         if (hasGravity() && (isArrow() || sticksToWalls())) {
            setDeltaMovement(getDeltaMovement().scale(-0.1D));
            setYRot(getYRot() + 180.0F);
            this.yRotO += 180.0F;
            this.ticksInAir = 0;
         }
         return;
      }

      if (target instanceof LivingEntity living) {
         applyEnchantEffects(living);
         if (isArrow() || sticksToWalls()) {
            living.setArrowCount(living.getArrowCount() + 1);
         }
      }
      if (this.punch > 0) {
         Vec3 motion = getDeltaMovement();
         double flat = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
         if (flat > 0.0D) {
            target.push(motion.x * this.punch * 0.6D / flat, 0.1D, motion.z * this.punch * 0.6D / flat);
         }
      }
      breakParticles();
      explodeIfNeeded();
      if (!this.level().isClientSide && (this.destroyedOnEntityHit || !(isArrow() || sticksToWalls()))) {
         discard();
      }
   }

   /** 原作在枪/法杖的回调里做这件事；改成读自身字段，存盘也不丢。 */
   private void applyEnchantEffects(LivingEntity living) {
      if (this.confusion > 0 && living.getRandom().nextInt(4) < this.confusion) {
         living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100));
      }
      if (this.poison > 0 && living.getRandom().nextInt(4) < this.poison) {
         living.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
      }
   }

   @Override
   protected void onHitBlock(BlockHitResult result) {
      if (!isArrow() && !sticksToWalls()) {
         breakParticles();
         explodeIfNeeded();
         if (!this.level().isClientSide) {
            discard();
         }
         return;
      }
      // 插进方块：贴着命中点停住，之后靠 tickInGround 计时
      this.stuckAt = result.getBlockPos();
      Vec3 into = result.getLocation().subtract(position());
      setDeltaMovement(into);
      double len = into.length();
      if (len > 1.0E-7D) {
         setPos(position().subtract(into.scale(0.05D / len)));
      }
      this.inGround = true;
      this.shake = 7;
      this.ticksInGround = 0;
      setRotating(false);
      if (!hasGravity()) {
         setHasGravity(true);   // 插住之后就该受重力了，原作同样在这里补上
      }
      onStuck(this.stuckAt);
      explodeIfNeeded();
   }

   /** 插在方块上熬到头了。默认消失，圣手雷覆写成爆炸。 */
   protected void onTimeExpired() {
      discard();
   }

   /** 刚插进方块。圣手雷覆写成放一声哈利路亚。 */
   protected void onStuck(BlockPos pos) {
   }

   private void explodeIfNeeded() {
      if (this.explosiveRadius <= 0 || this.level().isClientSide) {
         return;
      }
      // 原作按 mobGriefing 与 explosiveDamage 共同决定是否破坏地形（法杖的爆炸不破坏）
      boolean griefing = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
              && this.explosiveDamage;
      this.level().explode(getOwner() == null ? this : getOwner(), getX(), getY(), getZ(),
              this.explosiveRadius,
              griefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
      discard();
   }

   private void breakParticles() {
      if (this.level().isClientSide || isArrow() || sticksToWalls() || getItemDisplay().isEmpty()) {
         return;
      }
      if (isBlock()) {
         this.level().levelEvent(2001, blockPosition(),
                 net.minecraft.world.level.block.Block.getId(
                         ((BlockItem) getItemDisplay().getItem()).getBlock().defaultBlockState()));
         return;
      }
      if (this.level() instanceof net.minecraft.server.level.ServerLevel server) {
         server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, getItemDisplay()),
                 getX(), getY(), getZ(), 8, 0.15D, 0.2D, 0.15D, 0.0D);
      }
   }

   // ── 捡起 ─────────────────────────────────────────────────────────────

   @Override
   public void playerTouch(Player player) {
      if (this.level().isClientSide || !this.canBePickedUp || !this.inGround || this.shake > 0) {
         return;
      }
      if (player.getInventory().add(getItemDisplay().copy())) {
         playSound(SoundEvents.ITEM_PICKUP, 0.2F,
                 ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
         player.take(this, 1);
         discard();
      }
   }

   @Override
   public boolean isNoGravity() {
      return !hasGravity();
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.put("Item", getItemDisplay().save(new CompoundTag()));
      tag.putFloat("damagev2", this.damage);
      tag.putInt("punch", this.punch);
      tag.putInt("velocity", this.entityData.get(DATA_SPEED));
      tag.putInt("explosiveRadius", this.explosiveRadius);
      tag.putBoolean("explosiveDamage", this.explosiveDamage);
      tag.putBoolean("gravity", hasGravity());
      tag.putBoolean("accelerate", this.accelerate);
      tag.putBoolean("glows", glows());
      tag.putBoolean("Render3D", this.entityData.get(DATA_3D));
      tag.putBoolean("Spins", isRotating());
      tag.putBoolean("Sticks", this.entityData.get(DATA_STICKS));
      tag.putBoolean("canBePickedUp", this.canBePickedUp);
      tag.putBoolean("inGround", this.inGround);
      tag.putInt("xTile", this.stuckAt.getX());
      tag.putInt("yTile", this.stuckAt.getY());
      tag.putInt("zTile", this.stuckAt.getZ());
      tag.putByte("shake", (byte) this.shake);
      tag.putInt("poison", this.poison);
      tag.putInt("confusion", this.confusion);
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      ItemStack stack = ItemStack.of(tag.getCompound("Item"));
      if (stack.isEmpty()) {
         discard();
         return;
      }
      setItemDisplay(stack);
      this.damage = tag.getFloat("damagev2");
      this.punch = tag.getInt("punch");
      this.entityData.set(DATA_SPEED, tag.getInt("velocity"));
      this.explosiveRadius = tag.getInt("explosiveRadius");
      this.explosiveDamage = tag.getBoolean("explosiveDamage");
      setHasGravity(tag.getBoolean("gravity"));
      this.accelerate = tag.getBoolean("accelerate");
      setGlows(tag.getBoolean("glows"));
      setIs3D(tag.getBoolean("Render3D"));
      setRotating(tag.getBoolean("Spins"));
      setStickInWall(tag.getBoolean("Sticks"));
      this.canBePickedUp = tag.getBoolean("canBePickedUp");
      this.inGround = tag.getBoolean("inGround");
      this.stuckAt = new BlockPos(tag.getInt("xTile"), tag.getInt("yTile"), tag.getInt("zTile"));
      this.shake = tag.getByte("shake");
      this.poison = tag.getInt("poison");
      this.confusion = tag.getInt("confusion");
   }

   /** 名字取所投物品的名字（原作 getDisplayName 同义）。 */
   @Override
   public net.minecraft.network.chat.Component getName() {
      return getItemDisplay().isEmpty() ? super.getName() : getItemDisplay().getHoverName();
   }
}
