package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import noppes.vc.VariedCommodities;
import noppes.vc.blocks.tiles.TileBanner;

/**
 * 壁挂横幅：单格、无碰撞（原作 {@code isPassable} 为真），朝向决定贴在哪一面。
 *
 * 无碰撞通过 Properties 的 {@code noCollission()} 实现，比覆写 getCollisionShape 更省事；
 * {@link #getShape} 仍返回实体形状，这样射线能打中它、可以右键和挖掘。
 */
public class BlockWallBanner extends BlockBasicRotated implements EntityBlock {

   /** 四个朝向各贴一面墙，数值取自原作 AABB0~AABB3。 */
   private static final VoxelShape[] SHAPES = {
           Block.box(0.0D, 0.0D, 11.2D, 16.0D, 16.0D, 16.0D),   // 朝向 0：南
           Block.box(0.0D, 0.0D, 0.0D, 4.8D, 16.0D, 16.0D),     // 朝向 1：西
           Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.8D),     // 朝向 2：北
           Block.box(11.2D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D),   // 朝向 3：东
   };

   private final DyeColor defaultColor;

   public BlockWallBanner(Properties props, DyeColor defaultColor) {
      super(props);
      this.defaultColor = defaultColor;
      registerDefaultState(defaultBlockState().setValue(VCDye.COLOR, defaultColor));
   }

   public DyeColor defaultColor() {
      return this.defaultColor;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(VCDye.COLOR);
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPES[state.getValue(rotation()) & 3];
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileBanner(pos, state);
   }

   @Override
   public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      if (level.isClientSide) {
         return;
      }
      if (level.getBlockEntity(pos) instanceof TileBanner tile) {
         tile.startEditWindow();
      }
      if (placer instanceof Player player) {
         player.displayClientMessage(
                 Component.translatable(VariedCommodities.MODID + ".message.edit_icon"), true);
      }
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
      if (hand != InteractionHand.MAIN_HAND) {
         return InteractionResult.PASS;
      }
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }
      if (!(level.getBlockEntity(pos) instanceof TileBanner tile)) {
         return InteractionResult.PASS;
      }

      if (tile.tryEditIcon(player.getItemInHand(hand))) {
         level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
         return InteractionResult.CONSUME;
      }

      DyeColor dye = VCDye.heldDye(player, hand);
      if (dye == null) {
         return InteractionResult.PASS;
      }
      if (state.getValue(VCDye.COLOR) != dye) {
         VCDye.recolor(level, pos, this, dye);
         VCDye.consume(player, hand);
      }
      return InteractionResult.CONSUME;
   }
}
