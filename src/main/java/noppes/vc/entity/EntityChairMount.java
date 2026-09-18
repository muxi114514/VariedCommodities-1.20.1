package noppes.vc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import noppes.vc.init.VCEntities;

/**
 * 落座用的隐形坐骑（椅子、凳子、沙发）。
 *
 * 除了"承载一名乘客"之外不做任何事：无碰撞、不可推动、不可伤害、不渲染。
 * 乘客离开后（服务端）立即销毁。
 *
 * <p><b>{@link #shouldBeSaved()} 返回 false</b>——这是 CLAUDE.md 里 P4 提到的
 * "椅子挂载实体加下线/区块卸载清理"的正解：座位是纯运行期的占位物，
 * 根本不该写进存档。不存盘就不存在"玩家坐着下线/区块卸载后残留、重进存档越积越多"的问题，
 * 比事后去清理更彻底。原作没有这项，坐着退出游戏会在存档里留下实体。
 */
public class EntityChairMount extends Entity {

   public EntityChairMount(EntityType<? extends EntityChairMount> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   @Override
   protected void defineSynchedData() {
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag tag) {
   }

   /** 乘客相对坐骑的抬升量，沿用原作 getMountedYOffset。 */
   @Override
   public double getPassengersRidingOffset() {
      return 0.5D;
   }

   @Override
   public boolean isInvisible() {
      return true;
   }

   @Override
   public boolean isInvulnerableTo(DamageSource source) {
      return true;
   }

   @Override
   public boolean canBeCollidedWith() {
      return false;
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   public boolean shouldBeSaved() {
      return false;
   }

   @Override
   public void tick() {
      super.tick();
      if (!level().isClientSide && getPassengers().isEmpty()) {
         discard();
      }
   }

   /**
    * 让玩家坐到该方块上。已经有人坐着就什么也不做。
    *
    * 查询范围只有本格，不会跨区块，因此不涉及区块加载问题。
    */
   public static InteractionResult sit(Level level, BlockPos pos, Player player) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }
      if (!level.getEntitiesOfClass(EntityChairMount.class, new AABB(pos)).isEmpty()) {
         return InteractionResult.CONSUME;
      }
      EntityChairMount mount = VCEntities.CHAIR_MOUNT.get().create(level);
      if (mount == null) {
         return InteractionResult.PASS;
      }
      mount.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
      level.addFreshEntity(mount);
      player.startRiding(mount, true);
      return InteractionResult.CONSUME;
   }
}
