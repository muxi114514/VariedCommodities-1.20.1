package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import noppes.vc.VariedCommodities;
import noppes.vc.blocks.tiles.TileBanner;

/**
 * 横幅：两格高，可染色，放下后有 10 秒窗口可用任意物品设为徽记。
 *
 * <p>原作把设徽记的逻辑放在全局的 {@code PlayerInteractEvent.RightClickBlock} 监听里
 * （{@code ServerEventsHandler}），而不是方块自己的 {@code onBlockActivated}。
 * 1.20.1 不需要这么绕：{@link #use} 在物品的 {@code useOn} 之前调用，
 * 返回 CONSUME 就能拦下这次交互，所以徽记与染色都能收进方块自己的交互里。
 */
public class BlockBanner extends BlockBasicDouble implements EntityBlock {

   private final DyeColor defaultColor;

   public BlockBanner(Properties props, DyeColor defaultColor) {
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
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      // 只给下半格：徽记的读写一律走 baseOf(pos)，上半格那个方块实体从来没人用，
      // 留着既白占内存，又会让渲染器把徽记重复画一遍
      return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new TileBanner(pos, state) : null;
   }

   /** 放置后开启编辑窗口并提示玩家。原作只在客户端发这条消息，这里改为服务端发给该玩家。 */
   @Override
   public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
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

      BlockPos base = baseOf(pos, state);
      if (!(level.getBlockEntity(base) instanceof TileBanner tile)) {
         return InteractionResult.PASS;
      }

      if (tile.tryEditIcon(player.getItemInHand(hand))) {
         BlockState baseState = level.getBlockState(base);
         level.sendBlockUpdated(base, baseState, baseState, Block.UPDATE_ALL);
         return InteractionResult.CONSUME;
      }

      DyeColor dye = VCDye.heldDye(player, hand);
      if (dye == null) {
         return InteractionResult.PASS;
      }
      if (state.getValue(VCDye.COLOR) != dye) {
         VCDye.recolor(level, base, this, dye);
         VCDye.recolor(level, base.above(), this, dye);
         VCDye.consume(player, hand);
      }
      return InteractionResult.CONSUME;
   }
}
