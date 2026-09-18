package noppes.vc.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import noppes.vc.VariedCommodities;

/**
 * 数据生成入口（gradlew runData）。
 *
 * 1.12 时代 blockstate/模型/配方/战利品表全是手写 json，519 个注册项手写必然漏项——
 * 漏 blockstate 是紫黑块、漏战利品表是挖了不掉，这正是各种半吊子移植版的通病。
 * 这里全部改为代码产出，产物落在 src/generated/resources（build.gradle 已并入主 sourceSet）。
 *
 * 注意：lang 与配方都不走 DataGen，理由相同——它们是<b>从原作机械转录</b>的既有数据，
 * 不是这边新写的内容。16 种语言是原作的译文；244 条配方是 1.12 {@code VCRecipes.loadDefaultRecipes()}
 * 的逐条搬运（抽取脚本的产物存档在 reference/RECIPES.json）。
 * 用 LanguageProvider / RecipeProvider 等于把同一份真相再抄一遍进 java，反而制造第二份真相，
 * 且那会是个上千行的类。故两者都作为静态资源直接放在
 * src/main/resources/{assets,data}/variedcommodities/ 下。
 *
 * <p>这里留下的四个 Provider 产出的都是<b>推导物</b>（由注册表推出 blockstate/模型/战利品表/标签），
 * 原作里没有对应文件，才适合代码生成。
 */
@Mod.EventBusSubscriber(modid = VariedCommodities.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class VCDataGen {

   private VCDataGen() {
   }

   @SubscribeEvent
   public static void gather(GatherDataEvent event) {
      DataGenerator gen = event.getGenerator();
      PackOutput out = gen.getPackOutput();
      ExistingFileHelper helper = event.getExistingFileHelper();

      // BlockStates 必须排在 ItemModels 之前：物品模型 parent 到方块模型，
      // ExistingFileHelper 要先"见过"后者才不会判定为引用了不存在的模型
      gen.addProvider(event.includeClient(), new VCBlockStates(out, helper));
      gen.addProvider(event.includeClient(), new VCItemModels(out, helper));
      // 物品标签要复用方块标签的 contentsGetter（原版 ItemTagsProvider 的契约：
      // 它会把"方块标签同名下的方块物品"自动带过去），故先建实例再传引用
      VCBlockTags blockTags = new VCBlockTags(out, event.getLookupProvider(), helper);
      gen.addProvider(event.includeServer(), blockTags);
      gen.addProvider(event.includeServer(),
              new VCItemTags(out, event.getLookupProvider(), blockTags.contentsGetter(), helper));
      gen.addProvider(event.includeServer(), new VCLootTables(out));
   }
}
