package noppes.vc.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;
import noppes.vc.VariedCommodities;

/**
 * 裙甲模型（原作 {@code client/ModelSkirtArmor}）。
 *
 * <p>一片 L 形的双面薄片，绕立轴每 36° 画一遍、共十遍，凑成一圈裙摆。
 * 原作用自制的 {@code ModelPlaneRenderer.addSidePlane} 画单面四边形；1.20.1 直接用
 * {@code addBox} 的<b>零厚度盒子</b>——零厚度时只剩垂直于该轴的两个面，正反都看得见，
 * 比原作的单面片还省事。
 *
 * <p>⚠️ <b>贴图偏移要往上挪 depth</b>：原作那个自制四边形的 UV 起点就是 {@code texOffs}，
 * 而原版盒子展开时 X 向面的 v 起点是 {@code v0 + depth}。这里把 texOffs 的 v 减去 2，
 * 展开后正好落回 (4,20)/(6,20)，与原作逐像素一致。
 */
public class SkirtModel extends HumanoidModel<LivingEntity> {

   public static final ModelLayerLocation LAYER =
           new ModelLayerLocation(new ResourceLocation(VariedCommodities.MODID, "skirt"), "main");

   /** MoBends 会改玩家潜行姿势，原作为它单独少挪一段。 */
   private static final boolean MO_BENDS = ModList.get().isLoaded("mobends");

   private final ModelPart skirt;

   public SkirtModel(ModelPart root) {
      super(root);
      this.skirt = root.getChild("skirt");
   }

   public static LayerDefinition createLayer() {
      MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
      PartDefinition root = mesh.getRoot();
      PartDefinition skirt = root.addOrReplaceChild("skirt",
              CubeListBuilder.create().texOffs(4, 18)
                      .addBox(-2.6F, 9.0F, -2.6F, 0.0F, 9.0F, 2.0F),
              PartPose.offsetAndRotation(2.4F, 0.0F, 0.0F, 0.3F, -0.2F, -0.2F));
      skirt.addOrReplaceChild("skirt_side",
              CubeListBuilder.create().texOffs(6, 18)
                      .addBox(-0.6F, 9.0F, 2.6F, 0.0F, 9.0F, 2.0F),
              PartPose.rotation(0.0F, -Mth.HALF_PI, 0.0F));
      return LayerDefinition.create(mesh, 64, 32);
   }

   /**
    * 裙摆随手臂轻微摆动，逐字沿用原作 {@code setRotationAngles} 末尾那三行加减。
    *
    * <p>⚠️ 不能写进 {@code setupAnim}：护甲层走的是
    * {@code copyPropertiesTo} 把玩家模型的肢体角度抄过来，<b>根本不调 setupAnim</b>。
    * 原作同样是在 render 里重算一遍，这里保持一致，由 {@code VCClient} 抄完属性后调用。
    */
   public void sway(float ageInTicks) {
      this.skirt.setRotation(0.3F, -0.2F, -0.2F);
      this.skirt.xRot += this.leftArm.xRot * 0.01F;
      this.skirt.zRot += this.leftArm.xRot * 0.03F;
      this.skirt.zRot -= Mth.cos(ageInTicks * 0.06F) * 0.01F + 0.02F;
   }

   /**
    * <b>只画裙摆，不画人形部件。</b>原作同样整个覆写了 render 只画那一片——
    * 否则 {@code HumanoidArmorLayer#setPartVisibility} 会把身体与双腿设为可见，
    * 那两块会用裙甲贴图画出一套普通护腿来。
    */
   @Override
   public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay,
                              float red, float green, float blue, float alpha) {
      pose.pushPose();
      if (this.crouching) {
         // 原作这几个位移是方块单位，模型空间里 1 单位 = 1/16 格，故乘 16
         pose.translate(0.0F, 3.2F, -3.2F);
         if (!MO_BENDS) {
            pose.translate(0.0F, -1.6F, 5.6F);
         }
         pose.mulPose(Axis.XP.rotationDegrees(12.0F));
      }
      pose.scale(2.1F, 1.04F, 2.1F);
      for (int i = 0; i < 10; i++) {
         pose.mulPose(Axis.YP.rotationDegrees(36.0F));
         this.skirt.render(pose, consumer, light, overlay, red, green, blue, alpha);
      }
      pose.popPose();
   }
}
