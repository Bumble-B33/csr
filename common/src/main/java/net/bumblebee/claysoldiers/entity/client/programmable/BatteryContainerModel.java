package net.bumblebee.claysoldiers.entity.client.programmable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.BatteryContentRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BatteryContainerModel extends Model.Simple {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/item/battery_container.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(TEXTURE);
    private static final Set<ModelInfo> ITEM_MODEL_INFO = Set.of(
            new ModelInfo(6, true),
            new ModelInfo(9, true)
    );
    private static final Set<ModelInfo> BLOCK_MODEL_INFO = Set.of(
            new ModelInfo(8, false)
    );

    public static final int MAX_HEIGHT_EXCLUSIVE = 10;
    public static final int MIN_HEIGHT_EXCLUSIVE = 2;
    private final ModelPart root;
    private final BatteryContentRenderer batteryContentRenderer;

    private BatteryContainerModel(ModelPart root, int height, boolean item) {
        super(root, RenderTypes::entityCutout);
        this.root = root;
        if (item) {
            this.batteryContentRenderer = BatteryContentRenderer.ofDefaultTexture(2.5f, -1, 2.5f, height);
        } else {
            this.batteryContentRenderer = BatteryContentRenderer.ofDefaultTexture(0, 0, 0, height);
        }
    }

    public static BatteryContainerModel createItem(EntityModelSet modelSet, int height) {
        return new BatteryContainerModel(modelSet.bakeLayer(get(height, true).modelLayerLocation), height, true);
    }

    public static BatteryContainerModel createBlock(EntityModelSet modelSet, int height) {
        return new BatteryContainerModel(modelSet.bakeLayer(get(height, false).modelLayerLocation), height, false);
    }

    public void getExtents(Consumer<Vector3fc> consumer) {
        root.getExtentsForGui(new PoseStack(), consumer);
    }

    public void submitAsItem(@NonNull BatteryContainerRenderState fillPercent, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, boolean foil, int outline) {
        poseStack.pushPose();
        poseStack.scale(2.0F, -2.0F, -2.0F);

        nodeCollector.submitModel(this, Unit.INSTANCE, poseStack, RENDER_TYPE, packedLight, packedOverlay, 0, null);

        if (foil) {
            nodeCollector.order(1)
                    .submitModel(
                            this,
                            Unit.INSTANCE,
                            poseStack,
                            ItemFeatureRenderer.getFoilRenderType(RENDER_TYPE, false),
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            outline,
                            null
                    );
        }

        poseStack.pushPose();


        poseStack.scale(1f, -1f, -1f);

        batteryContentRenderer.submitBatteryContent(fillPercent.fillPercent, nodeCollector, poseStack, packedLight);
        poseStack.popPose();

        poseStack.popPose();
    }

    public void submitAsBlock(float fillPercent, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int outlineColor, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        poseStack.pushPose();
        poseStack.translate(0, 0.002f, 0);
        nodeCollector.submitModel(this, Unit.INSTANCE, poseStack, RENDER_TYPE, packedLight, OverlayTexture.NO_OVERLAY, outlineColor, breakProgress);


        batteryContentRenderer.submitBatteryContent(fillPercent, nodeCollector, poseStack, packedLight);
        poseStack.popPose();
    }

    public static void validateHeight(int height, boolean item) {
        get(height, item);
    }

    public static void registerLayers(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> event) {
        ITEM_MODEL_INFO.forEach(s -> event.accept(s.modelLayerLocation, s.layerDefinitionSupplier));
        BLOCK_MODEL_INFO.forEach(s -> event.accept(s.modelLayerLocation, s.layerDefinitionSupplier));
    }

    private static LayerDefinition createBatteryItemLayer(int height, PartPose offset) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        if (height <= MIN_HEIGHT_EXCLUSIVE) {
            throw new IllegalArgumentException("Battery height to small: %s min %s".formatted(height, MIN_HEIGHT_EXCLUSIVE));
        } else if (height >= MAX_HEIGHT_EXCLUSIVE) {
            throw new IllegalArgumentException("Battery height to big: %s min %s".formatted(height, MAX_HEIGHT_EXCLUSIVE));
        }

        partdefinition.addOrReplaceChild("battery", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, -6.5F, 0.0F, 1.0F, 1.0F, 1.0F)
                        .texOffs(4, 12).addBox(-2.0F, -6.01F + height - 1, -1.0F, 3.0F, 1, 3.0F, new CubeDeformation(0.01f))
                        .texOffs(4, 0).addBox(-2.0F, -6.01F, -1.0F, 3.0F, height - 1, 3.0F, new CubeDeformation(0.01f)),
                offset
        );

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static LayerDefinition createBatteryBlockLayer(int height, PartPose offset) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        if (height <= MIN_HEIGHT_EXCLUSIVE) {
            throw new IllegalArgumentException("Battery height to small: %s min %s".formatted(height, MIN_HEIGHT_EXCLUSIVE));
        } else if (height >= MAX_HEIGHT_EXCLUSIVE) {
            throw new IllegalArgumentException("Battery height to big: %s min %s".formatted(height, MAX_HEIGHT_EXCLUSIVE));
        }

        partdefinition.addOrReplaceChild("battery", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, 2.5f - height, 0.0F, 1.0F, 1.0F, 1.0F)
                        .texOffs(4, 12).addBox(-2.0F, 2.01f, -1.0F, 3.0F, 1, 3.0F, new CubeDeformation(0.01f))
                        .texOffs(4, 0).addBox(-2.0F, 3.00F - height, -1.0F, 3.0F, height - 1, 3.0F, new CubeDeformation(0.01f)),
                offset
        );

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static ModelLayerLocation createModelLocation(int height, boolean item) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "battery"), (item ? "item" : "block") + "_height_" + height);
    }

    private static @NotNull ModelInfo get(int height, boolean item) {
        Set<ModelInfo> set = item ? ITEM_MODEL_INFO : BLOCK_MODEL_INFO;
        for (var info : set) {
            if (info.height == height) {
                return info;
            }
        }
        throw new IllegalArgumentException("Battery for height %s does not exist".formatted(height));
    }

    private static final class ModelInfo {
        private final int height;
        private final ModelLayerLocation modelLayerLocation;
        private final Supplier<LayerDefinition> layerDefinitionSupplier;

        private ModelInfo(int height, boolean item) {
            this.height = height;
            this.modelLayerLocation = createModelLocation(height, item);
            if (item) {
                this.layerDefinitionSupplier = () -> createBatteryItemLayer(height, PartPose.offset(4.5f, 7 - height, -4.5f));
            } else {
                this.layerDefinitionSupplier = () -> createBatteryBlockLayer(height, PartPose.offsetAndRotation(1, 3, 1, 0, 0, Mth.PI));
            }
        }
    }
}
