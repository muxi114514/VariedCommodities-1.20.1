package noppes.vc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import noppes.vc.containers.CarpentryMenu;

/**
 * 木工台（含铁砧款）：4×4 合成台。
 *
 * <p><b>不需要方块实体。</b>原作的 {@code TileCarpentryBench} 与 {@code TileAnvil} 都是
 * 零字段零 NBT 的空类，存在的唯一理由是 1.12 的 {@code BlockContainer} 必须配一个 TileEntity。
 * 1.20.1 的合成格活在菜单里而不是方块里，这两个类整个消失。
 *
 * <p>译名可被覆盖：契约里 {@code carpentry_bench_anvil} 借用原版铁砧的译名
 * （原作靠 {@code ItemBlockNamed.names = {"tile.carpentry_bench", "tile.anvil"}} 实现）。
 */
public class BlockCarpentryBench extends BlockBasicRotated {

   private final String descriptionOverride;

   public BlockCarpentryBench(Properties props, String descriptionOverride) {
      super(props);
      this.descriptionOverride = descriptionOverride;
   }

   @Override
   public String getDescriptionId() {
      return this.descriptionOverride != null ? this.descriptionOverride : super.getDescriptionId();
   }

   @Override
   public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
      return new SimpleMenuProvider(
              (containerId, inventory, player) ->
                      new CarpentryMenu(containerId, inventory, ContainerLevelAccess.create(level, pos)),
              Component.translatable(getDescriptionId()));
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                InteractionHand hand, BlockHitResult hit) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }
      player.openMenu(state.getMenuProvider(level, pos));
      return InteractionResult.CONSUME;
   }
}
