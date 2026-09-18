package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import noppes.vc.blocks.tiles.TileBigSign;
import noppes.vc.client.VCClientHooks;

/**
 * 可刻字的方块（大告示牌、墓碑）。
 *
 * <p>两者的差别只有形状和"这一款能不能刻字"，所以合为一类、由构造参数区分——
 * 原作的 {@code BlockBigSign} 与 {@code BlockTombstone} 逻辑几乎逐行相同。
 * 墓碑的第三款（原作 {@code meta >= 2}）不可刻字：既不在放置时开界面，也没有方块实体。
 *
 * <p>界面在客户端直接打开，不像原作那样绕服务端的 {@code openGui} 发一个 S→C 包：
 * 文本与编辑锁都已随方块实体同步到客户端，客户端自己就有全部信息。
 * 只有"保存"需要回传服务端（{@code SaveSignPacket}），而那一侧是不可信输入、另有校验。
 */
public class BlockTextEditable extends BlockBasicRotated implements EntityBlock {

   private final VoxelShape[] shapes;
   private final boolean editable;

   public BlockTextEditable(Properties props, VoxelShape[] shapes, boolean editable) {
      super(props);
      this.shapes = shapes;
      this.editable = editable;
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return this.shapes[state.getValue(rotation()) & 3];
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return this.editable ? new TileBigSign(pos, state) : null;
   }

   /** 放下就直接进编辑界面，与原作一致。 */
   @Override
   public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      if (this.editable && level.isClientSide && placer instanceof Player) {
         openEditor(pos, "", state);
      }
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
      if (!this.editable || hand != InteractionHand.MAIN_HAND) {
         return InteractionResult.PASS;
      }
      if (!(level.getBlockEntity(pos) instanceof TileBigSign sign) || !sign.mayEdit(player)) {
         return InteractionResult.PASS;
      }
      if (level.isClientSide) {
         openEditor(pos, sign.text(), state);
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   private static void openEditor(BlockPos pos, String text, BlockState state) {
      Component title = state.getBlock().getName();
      DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VCClientHooks.openTextEditor(pos, text, title));
   }
}
