package noppes.vc.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import noppes.vc.VariedCommodities;
import noppes.vc.init.VCItems;

/**
 * 物品标签。
 *
 * <p>对应 1.12 的 {@code OreDictionary.registerOre}（{@code VCItems.java:202-208}，共 7 条）。
 * <b>这一步不能省：</b>矿物词典是跨模组互认的唯一渠道，1.20.1 换成了标签。
 * 不打标签的话别的模组看不见 VC 的锭与宝石，整合包里所有引用
 * {@code forge:ingots/bronze} 之类的配方全部失效——对 RLcraft 这种整合包影响是直接的。
 *
 * <p>原作把蓝宝石写成 {@code gemSaphire}（少一个 p）。标签名按 1.20.1 的通行写法
 * {@code forge:gems/sapphire} 拼正确，不沿用这个拼写错误——标签是给别的模组查的，
 * 照抄错拼等于谁也对不上。
 */
public class VCItemTags extends ItemTagsProvider {

   public VCItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                     CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper helper) {
      super(output, lookup, blockTags, VariedCommodities.MODID, helper);
   }

   @Override
   protected void addTags(HolderLookup.Provider provider) {
      gem("sapphire", VCItems.GEM_SAPPHIRE);
      gem("ruby", VCItems.GEM_RUBY);
      gem("amethyst", VCItems.GEM_AMETHYST);

      ingot("bronze", VCItems.INGOT_BRONZE);
      ingot("steel", VCItems.INGOT_STEEL);
      ingot("demonic", VCItems.INGOT_DEMONIC);
      ingot("mithril", VCItems.INGOT_MITHRIL);
   }

   /** 具体标签 + 伞标签都要打：别的模组两种查法都有。 */
   private void gem(String name, RegistryObject<Item> item) {
      TagKey<Item> specific = forge("gems/" + name);
      tag(specific).add(item.get());
      tag(Tags.Items.GEMS).addTag(specific);
   }

   private void ingot(String name, RegistryObject<Item> item) {
      TagKey<Item> specific = forge("ingots/" + name);
      tag(specific).add(item.get());
      tag(Tags.Items.INGOTS).addTag(specific);
   }

   private static TagKey<Item> forge(String path) {
      return ItemTags.create(new ResourceLocation("forge", path));
   }
}
