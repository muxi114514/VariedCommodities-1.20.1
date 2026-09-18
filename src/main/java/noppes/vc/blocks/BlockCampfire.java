package noppes.vc.blocks;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 篝火。不同于蜡烛/台灯的"右键即切换"，它要用燧石或打火石点燃、用沙子熄灭。
 *
 * 点燃只有 1/3 概率成功（原作 {@code world.rand.nextInt(3) == 0}），但无论成功与否
 * 都会冒一股烟并消耗一次工具——燧石消耗一个，打火石掉一点耐久。
 */
public class BlockCampfire extends BlockBasicLightable {

   private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   private static final VoxelShape[] WALL = {SHAPE, SHAPE, SHAPE, SHAPE, SHAPE, SHAPE, SHAPE, SHAPE};

   private static final int LIGHT_CHANCE = 3;

   public BlockCampfire(Properties props, boolean lit, Supplier<Block> counterpart) {
      super(props, lit, counterpart, SHAPE, SHAPE, WALL);
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
      if (hand != InteractionHand.MAIN_HAND) {
         return InteractionResult.PASS;
      }
      ItemStack held = player.getItemInHand(hand);

      if (!isLit() && (held.is(Items.FLINT) || held.is(Items.FLINT_AND_STEEL))) {
         if (level.isClientSide) {
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
         } else {
            if (level.random.nextInt(LIGHT_CHANCE) == 0) {
               toggle(state, level, pos);
            }
            if (held.is(Items.FLINT)) {
               if (!player.getAbilities().instabuild) {
                  held.shrink(1);
               }
            } else {
               held.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(hand));
            }
         }
         return InteractionResult.sidedSuccess(level.isClientSide);
      }

      if (isLit() && held.is(Items.SAND)) {
         if (!level.isClientSide) {
            toggle(state, level, pos);
         }
         return InteractionResult.sidedSuccess(level.isClientSide);
      }

      // 其它情况原作也把右键吃掉（返回 true），避免手上的方块被放到篝火上
      return InteractionResult.CONSUME;
   }

   @Override
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (!isLit()) {
         return;
      }
      double x = pos.getX() + 0.5D;
      double y = pos.getY() + 0.7D;
      double z = pos.getZ() + 0.5D;
      if (random.nextInt(36) == 0) {
         level.playLocalSound(x, y, z, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
                 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
      }
      level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
      level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
   }
}
