package noppes.vc.items;

import java.util.function.Consumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import noppes.vc.client.VCClient;

/**
 * 裙甲：占腿部槽位，但用一套独立的模型渲染（原作 {@code proxy.getSkirtModel()}）。
 */
public class ItemArmorSkirt extends ItemArmorBasic {

   /**
    * 不指定命名空间时用 <b>minecraft</b>——原作 {@code ItemArmorSkirt} 的 {@code id} 字段
    * 默认就是 "minecraft"，那几件裙甲借的是原版护甲贴图（chainmail_layer_2 等）。
    * 写成本模组命名空间会找不到贴图。
    */
   public ItemArmorSkirt(ArmorMaterial material, String texture, Properties props) {
      super(material, Type.LEGGINGS, "minecraft", texture, props);
   }

   public ItemArmorSkirt(ArmorMaterial material, String namespace, String texture, Properties props) {
      super(material, Type.LEGGINGS, namespace, texture, props);
   }

   /** 裙甲没有 overlay 层，且不论槽位一律用 _2 贴图。 */
   @Override
   public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String layer) {
      return "overlay".equals(layer) ? null : super.getArmorTexture(stack, entity, EquipmentSlot.LEGS, layer);
   }

   /**
    * 换掉原版护腿模型。
    *
    * <p>{@code initializeClient} 的实现体只在客户端被调用，专用服务器上不会去类加载
    * {@link IClientItemExtensions} 之后的任何东西——这正是 1.20.1 取代 1.12 {@code SidedProxy} 的写法。
    */
   @Override
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         @Override
         public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                       EquipmentSlot slot, HumanoidModel<?> original) {
            return VCClient.skirtModel(entity, original);
         }
      });
   }
}
