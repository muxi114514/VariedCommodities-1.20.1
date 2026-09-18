package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import noppes.vc.blocks.tiles.TileBook;
import noppes.vc.client.VCClientHooks;

/**
 * 书台：摊开的一本书。
 *
 * <p><b>与原作的交互模型不同，这是一处需要知情的偏离。</b>
 * 原作右键会就地打开一个可编辑的书本界面，那个 {@code GuiBook} 是 1.12 原版
 * {@code GuiScreenBook} 的逐行抄写，只把保存改成发包。1.20.1 的对应物 {@code BookEditScreen}
 * 约 700 行，且保存路径是私有的、绑死在"玩家手持物 + 手"上，无法改写目标，
 * fork 整个原版类只为换一个保存目标不划算。
 *
 * <p>因此改成书架式交换：空手取书（取出后在手里用原版界面正常读写，再放回）、
 * 拿着书放入；若台上是已签名的成书，空手右键直接开原版阅读界面，不必取下。
 * 功能（放一本可读可写的书在台上）保住了，且全程只用原版界面。
 */
public class BlockBook extends BlockBasicRotated implements EntityBlock {

   /** 原作 AABB (0,0,0)-(1,0.2,1)：摊平在台面上。 */
   private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.2D, 16.0D);

   public BlockBook(Properties props) {
      super(props);
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPE;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileBook(pos, state);
   }

   /** 借用原版书本的译名，与原作 {@code getTranslationKey()} 返回 "item.book" 一致。 */
   @Override
   public String getDescriptionId() {
      return Items.BOOK.getDescriptionId();
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
      if (hand != InteractionHand.MAIN_HAND) {
         return InteractionResult.PASS;
      }
      if (!(level.getBlockEntity(pos) instanceof TileBook tile)) {
         return InteractionResult.PASS;
      }

      ItemStack held = player.getMainHandItem();
      ItemStack stored = tile.getItem(0);

      // 台上是成书且空手不潜行 —— 直接阅读，不取下
      if (held.isEmpty() && !player.isShiftKeyDown() && stored.is(Items.WRITTEN_BOOK)) {
         if (level.isClientSide) {
            ItemStack copy = stored.copy();
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VCClientHooks.openBook(copy));
         }
         return InteractionResult.sidedSuccess(level.isClientSide);
      }

      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }
      if (held.isEmpty() && !stored.isEmpty()) {
         tile.setItem(0, ItemStack.EMPTY);
         player.setItemInHand(InteractionHand.MAIN_HAND, stored);
      } else if (stored.isEmpty() && tile.canPlaceItem(0, held)) {
         tile.setItem(0, held.split(1));
      } else {
         return InteractionResult.CONSUME;
      }

      tile.setChanged();
      level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
      return InteractionResult.CONSUME;
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof TileBook tile) {
            Containers.dropContents(level, pos, tile);
         }
         super.onRemove(state, level, pos, newState, moving);
      }
   }

}
