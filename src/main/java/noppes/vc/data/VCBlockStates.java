package noppes.vc.data;

import java.util.Map;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.VariedCommodities;
import noppes.vc.blocks.BlockBasicDouble;
import noppes.vc.blocks.BlockBasicLightable;
import noppes.vc.blocks.BlockBasicRotated;
import noppes.vc.blocks.BlockBlood;
import noppes.vc.blocks.BlockCouch;
import noppes.vc.blocks.BlockCouchWool;
import noppes.vc.blocks.VCDye;
import noppes.vc.blocks.VCMaterial;
import noppes.vc.blocks.VCWood;
import noppes.vc.blocks.storage.StorageTier;
import noppes.vc.init.VCBlocks;

/**
 * blockstate + 方块模型。
 *
 * <p><b>几何不在这里写。</b>1.12 的 44 个 ModelBase 共三百多个盒子，由脚本按下面的换算式
 * 批量转成 42 个「形状」模型，作为静态资源放在
 * {@code resources/assets/variedcommodities/models/block/shape/}；这里只出每个变体的薄壳
 * （{@code parent} 指向形状 + 填贴图）与 blockstate。手写几何等于把同一份真相再抄一遍。
 *
 * <p>换算式：TESR 统一做 {@code translate(T) · scale(S) · rotate(180,0,0,1)} 再渲染模型，
 * 绕 Z 翻转把 X/Y 取负，故 ModelBase 坐标 m 的方块坐标是 {@code T + diag(-Sx,-Sy,Sz)·m}；
 * 多数 TESR 的 T 是 {@code (8,24,8)}（即 {@code translate(x+.5, y+1.5, z+.5)}），
 * 于是简化成 {@code (8-mx, 24-my, 8+mz)}。横梁与基座可与原作碰撞箱互相验算，吻合。
 *
 * <p>1.12 的 {@code rotateAngle} 合成顺序是 Rz·Ry·Rx，脚本把它拆成「立方体群 P」×「小角度残余」：
 * P 直接烤进坐标，残余交给 JSON 的 element rotation（<b>只能单轴、只有 ±22.5/±45 五档</b>）。
 * 因此少数斜件有吸附误差（凳腿、篝火原木、木桶）——木桶原是 12 边形，JSON 表达不了 30° 一档，
 * 改用同半径的正八边形。
 */
public class VCBlockStates extends BlockStateProvider {

   private static final ResourceLocation PARENT_BLOCK = new ResourceLocation("minecraft", "block/block");
   private static final ResourceLocation WHITE_WOOL = new ResourceLocation("minecraft", "block/white_wool");

   public VCBlockStates(PackOutput output, ExistingFileHelper helper) {
      super(output, VariedCommodities.MODID, helper);
   }

   @Override
   protected void registerStatesAndModels() {
      tables();
      beams();
      pedestals();
      crystals();
      placeholders();
      seats();
      couches();
      storage();
      racks();
      shelves();
      signs();
      banners();
      tallLamps();
      lightables();
      textBlocks();
      carpentryBenches();
      bloodBlocks();
   }

   // ── 形状薄壳与旋转 ───────────────────────────────────────────────────

   /**
    * 变体薄壳：几何来自静态形状模型，这里只补贴图。
    *
    * <p>形状放在 {@code block/shape/} 子目录，不与方块注册名同级——两者会撞
    * （big_sign / candle / lamp / campfire / carpentry_bench / tombstone_N 六组同名），
    * 撞了就是同一个资源路径下两份文件，processResources 直接报 duplicate。
    */
   private BlockModelBuilder shell(String name, String shape, ResourceLocation particle) {
      return models().withExistingParent("block/" + name, modLoc("block/shape/" + shape))
              .texture("particle", particle);
   }

   private BlockModelBuilder woodShell(String name, String shape, ResourceLocation planks) {
      return shell(name, shape, planks).texture("planks", planks);
   }

   private BlockModelBuilder matShell(String name, String shape, ResourceLocation mat) {
      return shell(name, shape, mat).texture("material", mat).texture("wool", WHITE_WOOL);
   }

   /**
    * 单模型 + y 轴旋转。{@code step} 是原作 TESR 每档转过的度数。
    *
    * <p>⚠️ blockstate 的 y 旋转只能是 90 的倍数，所以 8 档（45°）只能两两合并。
    * 走 8 档的都是绕立轴近似对称的物件（木桶/蜡烛/台灯/篝火），且贴墙时原作本就只用偶数档
    * （{@code BlockBasicLightable#wallRotation} 返回 0/2/4/6），合并后贴墙朝向仍然精确。
    */
   private void rotated(RegistryObject<Block> block, ModelFile model, int step, Property<?>... ignored) {
      IntegerProperty prop = step == 45 ? BlockBasicRotated.ROTATION_8 : BlockBasicRotated.ROTATION_4;
      getVariantBuilder(block.get()).forAllStatesExcept(state -> ConfiguredModel.builder()
              .modelFile(model)
              .rotationY(state.getValue(prop) * step % 360 / 90 * 90)
              .build(), ignored);
   }

   // ── 椅子 / 凳子 ──────────────────────────────────────────────────────

   private void seats() {
      for (Map.Entry<VCWood, RegistryObject<Block>> e : VCBlocks.CHAIR.entrySet()) {
         String name = path(e.getValue());
         rotated(e.getValue(), woodShell(name, "chair", planks(e.getKey())), 90);
      }
      for (Map.Entry<VCWood, RegistryObject<Block>> e : VCBlocks.STOOL.entrySet()) {
         String name = path(e.getValue());
         rotated(e.getValue(), woodShell(name, "stool", planks(e.getKey())), 90);
      }
   }

   // ── 沙发：按左右拼接状态换形状 ────────────────────────────────────────

   private void couches() {
      for (Map.Entry<VCWood, RegistryObject<Block>> e : VCBlocks.COUCH_WOOD.entrySet()) {
         String name = path(e.getValue());
         ResourceLocation planks = planks(e.getKey());
         BlockModelBuilder single = woodShell(name, "couch_wood_single", planks);
         BlockModelBuilder left = woodShell(name + "_left", "couch_wood_left", planks);
         BlockModelBuilder middle = woodShell(name + "_middle", "couch_wood_middle", planks);
         BlockModelBuilder right = woodShell(name + "_right", "couch_wood_right", planks);
         getVariantBuilder(e.getValue().get()).forAllStatesExcept(state -> {
            boolean l = state.getValue(BlockCouch.LEFT);
            boolean r = state.getValue(BlockCouch.RIGHT);
            ModelFile model = l && r ? middle : l ? left : r ? right : single;
            return ConfiguredModel.builder().modelFile(model)
                    .rotationY(state.getValue(BlockBasicRotated.ROTATION_4) * 90).build();
         }, VCDye.COLOR);
      }

      for (Map.Entry<VCWood, RegistryObject<Block>> e : VCBlocks.COUCH_WOOL.entrySet()) {
         String name = path(e.getValue());
         ResourceLocation planks = planks(e.getKey());
         BlockModelBuilder corner = woolCouch(name + "_corner", "couch_wool_corner", planks);
         BlockModelBuilder left = woolCouch(name + "_left", "couch_wool_left", planks);
         BlockModelBuilder middle = woolCouch(name, "couch_wool_middle", planks);
         BlockModelBuilder right = woolCouch(name + "_right", "couch_wool_right", planks);
         getVariantBuilder(e.getValue().get()).forAllStatesExcept(state -> {
            boolean cl = state.getValue(BlockCouchWool.CORNER_LEFT);
            boolean cr = state.getValue(BlockCouchWool.CORNER_RIGHT);
            boolean l = state.getValue(BlockCouch.LEFT);
            boolean r = state.getValue(BlockCouch.RIGHT);
            ModelFile model = cl || cr ? corner : l && !r ? left : r && !l ? right : middle;
            // 原作右转角是把同一个转角模型再转 90°（BlockCouchWoolRenderer）
            int extra = cr ? 90 : 0;
            return ConfiguredModel.builder().modelFile(model)
                    .rotationY((state.getValue(BlockBasicRotated.ROTATION_4) * 90 + extra) % 360)
                    .build();
         }, VCDye.COLOR);
      }
   }

   /** 坐垫共用白羊毛贴图 + tintindex 0，颜色由 VCClient 的 BlockColor 从 blockstate 读。 */
   private BlockModelBuilder woolCouch(String name, String shape, ResourceLocation planks) {
      return woodShell(name, shape, planks).texture("wool", WHITE_WOOL);
   }

   // ── 板条箱 / 木桶 ────────────────────────────────────────────────────

   private void storage() {
      for (Map.Entry<VCWood, RegistryObject<Block>> e : VCBlocks.CRATE.entrySet()) {
         String name = path(e.getValue());
         rotated(e.getValue(), woodShell(name, "crate", planks(e.getKey())), 90);
      }
      for (Map.Entry<VCWood, RegistryObject<Block>> e : VCBlocks.BARREL.entrySet()) {
         String name = path(e.getValue());
         rotated(e.getValue(), woodShell(name, "barrel", planks(e.getKey())), 45);
      }
      // 升级板条箱与木箱同一个形状：形状里所有面共用 #planks 键，换成原版金属块贴图即可，几何一行不改
      for (Map.Entry<StorageTier, RegistryObject<Block>> e : VCBlocks.UPGRADED_CRATE.entrySet()) {
         String name = path(e.getValue());
         rotated(e.getValue(), woodShell(name, "crate", crateTexture(e.getKey())), 90);
      }
   }

   // ── 武器架（双格） ───────────────────────────────────────────────────

   private void racks() {
      for (Map.Entry<VCWood, RegistryObject<Block>> e : VCBlocks.WEAPON_RACK.entrySet()) {
         String name = path(e.getValue());
         ResourceLocation planks = planks(e.getKey());
         doubleBlock(e.getValue(),
                 woodShell(name, "weapon_rack", planks),
                 woodShell(name + "_top", "weapon_rack_top", planks));
      }
   }

   /** 双格方块：上下两半各一个模型，几何已按 y=16 切开。 */
   private void doubleBlock(RegistryObject<Block> block, ModelFile lower, ModelFile upper) {
      getVariantBuilder(block.get()).forAllStatesExcept(state -> ConfiguredModel.builder()
              .modelFile(state.getValue(BlockBasicDouble.HALF) == DoubleBlockHalf.UPPER ? upper : lower)
              .rotationY(state.getValue(BlockBasicRotated.ROTATION_4) * 90)
              .build(), VCDye.COLOR);
   }

   private void shelves() {
      for (Map.Entry<VCWood, RegistryObject<Block>> e : VCBlocks.SHELF.entrySet()) {
         String name = path(e.getValue());
         rotated(e.getValue(), woodShell(name, "shelf", planks(e.getKey())), 90);
      }
   }

   /** 徽记牌：木板 + 钢链，钢链用原作的 steel.png（形状文件里已写死）。 */
   private void signs() {
      for (Map.Entry<VCWood, RegistryObject<Block>> e : VCBlocks.SIGN.entrySet()) {
         String name = path(e.getValue());
         rotated(e.getValue(), woodShell(name, "sign", planks(e.getKey())), 90);
      }
   }

   // ── 横幅 / 墙上横幅 / 高脚灯：材质件 + 带 tint 的布件 ─────────────────

   private void banners() {
      for (Map.Entry<VCMaterial, RegistryObject<Block>> e : VCBlocks.BANNER.entrySet()) {
         String name = path(e.getValue());
         ResourceLocation mat = materialTexture(e.getKey());
         doubleBlock(e.getValue(), matShell(name, "banner", mat),
                 matShell(name + "_top", "banner_top", mat));
      }
      for (Map.Entry<VCMaterial, RegistryObject<Block>> e : VCBlocks.WALL_BANNER.entrySet()) {
         String name = path(e.getValue());
         rotated(e.getValue(), matShell(name, "wall_banner", materialTexture(e.getKey())),
                 90, VCDye.COLOR);
      }
   }

   private void tallLamps() {
      for (Map.Entry<VCMaterial, RegistryObject<Block>> e : VCBlocks.TALL_LAMP.entrySet()) {
         String name = path(e.getValue());
         ResourceLocation mat = materialTexture(e.getKey());
         doubleBlock(e.getValue(), matShell(name, "tall_lamp", mat),
                 matShell(name + "_top", "tall_lamp_top", mat));
      }
   }

   // ── 蜡烛 / 台灯 / 篝火 ───────────────────────────────────────────────

   private void lightables() {
      attachable(VCBlocks.CANDLE, "candle");
      attachable(VCBlocks.CANDLE_UNLIT, "candle");
      attachable(VCBlocks.LAMP, "lamp");
      attachable(VCBlocks.LAMP_UNLIT, "lamp");
      ResourceLocation cobble = new ResourceLocation("minecraft", "block/cobblestone");
      for (RegistryObject<Block> block : java.util.List.of(VCBlocks.CAMPFIRE, VCBlocks.CAMPFIRE_UNLIT)) {
         String name = path(block);
         BlockModelBuilder model = shell(name, "campfire", cobble)
                 .texture("planks", planks(VCWood.OAK))   // 原作篝火的柴火固定用橡木板贴图
                 .texture("stone", cobble);
         rotated(block, model, 45, BlockBasicLightable.FACE);
      }
   }

   /** 蜡烛与台灯：三种安装面各一个形状（地面/贴墙/吊顶），朝向走 8 档。 */
   private void attachable(RegistryObject<Block> block, String shape) {
      String name = path(block);
      ResourceLocation particle = modLoc("block/model/" + shape);
      BlockModelBuilder floor = shell(name, shape, particle);
      BlockModelBuilder wall = shell(name + "_wall", shape + "_wall", particle);
      BlockModelBuilder ceiling = shell(name + "_ceiling", shape + "_ceiling", particle);
      getVariantBuilder(block.get()).forAllStates(state -> {
         ModelFile model = switch (state.getValue(BlockBasicLightable.FACE)) {
            case WALL -> wall;
            case CEILING -> ceiling;
            default -> floor;
         };
         return ConfiguredModel.builder().modelFile(model)
                 .rotationY(state.getValue(BlockBasicRotated.ROTATION_8) * 45 % 360 / 90 * 90)
                 .build();
      });
   }

   // ── 大告示牌 / 墓碑 / 书台 ───────────────────────────────────────────

   private void textBlocks() {
      rotated(VCBlocks.BIG_SIGN,
              shell(path(VCBlocks.BIG_SIGN), "big_sign", modLoc("block/model/big_sign")), 90);
      // 书台的朝向原作差 90°（BlockBookRenderer 里是 rotate(90 * rotation - 90)）
      BlockModelBuilder ink = shell(path(VCBlocks.BOOK), "book_ink", modLoc("block/model/ink"));
      getVariantBuilder(VCBlocks.BOOK.get()).forAllStates(state -> ConfiguredModel.builder()
              .modelFile(ink)
              .rotationY((state.getValue(BlockBasicRotated.ROTATION_4) * 90 + 270) % 360)
              .build());
      ResourceLocation stone = new ResourceLocation("minecraft", "block/stone");
      for (int i = 0; i < VCBlocks.TOMBSTONE.size(); i++) {
         RegistryObject<Block> block = VCBlocks.TOMBSTONE.get(i);
         BlockModelBuilder model = shell(path(block), "tombstone_" + i, stone).texture("stone", stone);
         rotated(block, model, 90);
      }
   }

   private void carpentryBenches() {
      rotated(VCBlocks.CARPENTRY_BENCH,
              shell(path(VCBlocks.CARPENTRY_BENCH), "carpentry_bench",
                      modLoc("block/model/carpentry_bench")), 90);
      rotated(VCBlocks.CARPENTRY_BENCH_ANVIL,
              shell(path(VCBlocks.CARPENTRY_BENCH_ANVIL), "carpentry_bench_anvil",
                      modLoc("block/model/steel")), 90);
   }

   // ── 血迹：六个面各一张贴片，按 hide 标志用 multipart 逐面开关 ──────────

   private void bloodBlocks() {
      for (RegistryObject<Block> block : VCBlocks.BLOOD_BLOCK) {
         String name = path(block);
         ResourceLocation texture = modLoc("block/" + name);
         // 物品栏图标用整方块（multipart 的分面模型没法当图标），只此一处用途
         // 血迹贴图一半是全透明像素，不走 cutout 的话透明处会按 RGB 画成实心色块
         models().cubeAll("block/" + name, texture).renderType("cutout");
         MultiPartBlockStateBuilder builder = getMultipartBuilder(block.get());
         for (Direction dir : Direction.values()) {
            String face = dir.getSerializedName();
            BlockModelBuilder model = models()
                    .withExistingParent("block/" + name + "_" + face, modLoc("block/shape/blood_" + face))
                    .texture("blood", texture)
                    .texture("particle", texture);
            builder.part().modelFile(model).addModel()
                    .condition(BlockBlood.hide(dir), false).end();
         }
      }
   }

   // ── 桌子：multipart，四条腿各自按邻接情况显隐 ──────────────────────────

   private void tables() {
      for (Map.Entry<VCWood, RegistryObject<Block>> entry : VCBlocks.TABLE.entrySet()) {
         String name = path(entry.getValue());
         ResourceLocation planks = planks(entry.getKey());

         BlockModelBuilder top = model(name + "_top", planks)
                 .element().from(0, 14, 0).to(16, 16, 16)
                 .allFaces((dir, face) -> {
                    face.texture("#planks");
                    if (dir != Direction.DOWN) {
                       face.cullface(dir);   // 除底面外都贴着方块边界
                    }
                 })
                 .end();

         BlockModelBuilder leg = model(name + "_leg", planks)
                 .element().from(1, 0, 1).to(3, 14, 3)
                 .allFaces((dir, face) -> {
                    face.texture("#planks");
                    if (dir == Direction.DOWN) {
                       face.cullface(dir);
                    }
                 })
                 .end();

         // 物品用的整桌模型：桌面 + 四条腿
         BlockModelBuilder inventory = model(name + "_inventory", planks);
         inventory.element().from(0, 14, 0).to(16, 16, 16)
                 .allFaces((dir, face) -> face.texture("#planks")).end();
         for (int[] leg2d : new int[][]{{1, 1}, {13, 1}, {13, 13}, {1, 13}}) {
            inventory.element().from(leg2d[0], 0, leg2d[1]).to(leg2d[0] + 2, 14, leg2d[1] + 2)
                    .allFaces((dir, face) -> face.texture("#planks")).end();
         }

         MultiPartBlockStateBuilder builder = getMultipartBuilder(entry.getValue().get());
         builder.part().modelFile(top).addModel().end();
         // 桌腿在西北角(1,1)，顺时针 y 旋转即可到其余三角；某条腿在两侧任一方向有桌子时隐藏
         addLeg(builder, leg, 0, Direction.NORTH, Direction.WEST);
         addLeg(builder, leg, 90, Direction.NORTH, Direction.EAST);
         addLeg(builder, leg, 180, Direction.SOUTH, Direction.EAST);
         addLeg(builder, leg, 270, Direction.SOUTH, Direction.WEST);
      }
   }

   private static void addLeg(MultiPartBlockStateBuilder builder, BlockModelBuilder leg,
                              int rotationY, Direction a, Direction b) {
      builder.part().modelFile(leg).rotationY(rotationY).addModel()
              .condition(connection(a), false)
              .condition(connection(b), false)
              .end();
   }

   private static BooleanProperty connection(Direction dir) {
      return PipeBlock.PROPERTY_BY_DIRECTION.get(dir);
   }

   // ── 横梁：单一模型 + y 轴旋转 ─────────────────────────────────────────

   private void beams() {
      for (Map.Entry<VCWood, RegistryObject<Block>> entry : VCBlocks.BEAM.entrySet()) {
         String name = path(entry.getValue());
         BlockModelBuilder model = model(name, planks(entry.getKey()))
                 .element().from(5.5F, 5.5F, 4).to(10.5F, 10.5F, 16)
                 .allFaces((dir, face) -> {
                    face.texture("#planks");
                    if (dir == Direction.SOUTH) {
                       face.cullface(dir);
                    }
                 })
                 .end();
         rotated(entry.getValue(), model, 90);
      }
   }

   /**
    * 基座：底座 + 上柱两段，外加 {@code pedestal.png} 的贴花层。
    *
    * <p>几何由 {@code ModelPedestal} 换算（底座 z 3~13、上柱 z 4~12，与碰撞箱 AABB1 的
    * 3.2~12.8 吻合）。原作 {@code render()} 里给两个部件各套了一层 z 缩放（0.5 / 0.625），
    * 贴花则是把同一套几何按 {@code scale(1,0.99,1)} 再画一遍；JSON 里共面会 z-fighting，
    * 改成整体外扩 0.005 压在外面。
    */
   private void pedestals() {
      for (Map.Entry<VCMaterial, RegistryObject<Block>> entry : VCBlocks.PEDESTAL.entrySet()) {
         String name = path(entry.getValue());
         ResourceLocation texture = materialTexture(entry.getKey());
         rotated(entry.getValue(), matShell(name, "pedestal", texture), 90);
      }
   }

   // ── 水晶：16 个颜色共用一个带 tintindex 的模型，着色由 IBlockColor 提供 ──

   private void crystals() {
      ResourceLocation texture = modLoc("block/crystal_block");
      BlockModelBuilder model = models().withExistingParent("block/crystal_block", PARENT_BLOCK)
              .texture("all", texture)
              .texture("particle", texture)
              .element().from(0, 0, 0).to(16, 16, 16)
              .allFaces((dir, face) -> face.texture("#all").cullface(dir).tintindex(0))
              .end();
      VCBlocks.CRYSTAL_BLOCK.values().forEach(block -> simpleBlock(block.get(), model));
   }

   // ── 占位方块：各自一张独立贴图 ────────────────────────────────────────

   private void placeholders() {
      for (RegistryObject<Block> block : VCBlocks.PLACEHOLDER) {
         String name = path(block);
         simpleBlock(block.get(), models().cubeAll("block/" + name, modLoc("block/" + name)));
      }
   }

   // ── 辅助 ─────────────────────────────────────────────────────────────

   private BlockModelBuilder model(String name, ResourceLocation planks) {
      return models().withExistingParent("block/" + name, PARENT_BLOCK)
              .texture("planks", planks)
              .texture("particle", planks);
   }

   private static ResourceLocation planks(VCWood wood) {
      return new ResourceLocation("minecraft", "block/" + wood.getSerializedName() + "_planks");
   }

   /** 1.12 BlockRendererBasic#setMaterialTexture 绑定的原版贴图。 */
   private static ResourceLocation materialTexture(VCMaterial material) {
      String texture = switch (material) {
         case WOOD -> "oak_planks";
         case STONE -> "stone";
         case IRON -> "iron_block";
         case GOLD -> "gold_block";
         case DIAMOND -> "diamond_block";
      };
      return new ResourceLocation("minecraft", "block/" + texture);
   }

   /** 升级板条箱的贴图：直接用原版对应金属块，不画新图。 */
   private static ResourceLocation crateTexture(StorageTier tier) {
      String texture = switch (tier) {
         case WOOD -> throw new IllegalArgumentException("木质档按木材取贴图，不走这里");
         case IRON -> "iron_block";
         case DIAMOND -> "diamond_block";
         case NETHERITE -> "netherite_block";
      };
      return new ResourceLocation("minecraft", "block/" + texture);
   }

   private static String path(RegistryObject<Block> block) {
      return block.getId().getPath();
   }
}
