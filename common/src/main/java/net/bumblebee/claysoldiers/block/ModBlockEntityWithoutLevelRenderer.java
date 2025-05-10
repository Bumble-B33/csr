package net.bumblebee.claysoldiers.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.ClaySoldiersClient;
import net.bumblebee.claysoldiers.block.blueprint.EaselBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.claystaff.ClayStaffModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ModBlockEntityWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer {
    private static final EaselBlockEntity EASEL_BLOCK_ENTITY = new EaselBlockEntity(BlockPos.ZERO, ModBlocks.EASEL_BLOCK.get().defaultBlockState());
    private static final HamsterWheelBlockEntity HAMSTER_WHEEL_BLOCK_ENTITY = new HamsterWheelBlockEntity(BlockPos.ZERO, ModBlocks.HAMSTER_WHEEL_BLOCK.get().defaultBlockState());
    public static ModBlockEntityWithoutLevelRenderer instance = null;

    @Nullable
    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    @SuppressWarnings("ConstantValue")
    private ModBlockEntityWithoutLevelRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.blockEntityRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        EntityModelSet entityModelSet = Minecraft.getInstance().getEntityModels();
        if (entityModelSet != null) {
            ClaySoldiersClient.clayStaffModel = ClayStaffModel.create(entityModelSet::bakeLayer);
        }
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (pStack.getItem() instanceof BlockItem blockItem) {
            BlockState state = blockItem.getBlock().defaultBlockState();
            if (blockEntityRenderDispatcher == null) {
                blockEntityRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
            }

            if (state.is(ModBlocks.HAMSTER_WHEEL_BLOCK.get())) {
                this.blockEntityRenderDispatcher.renderItem(HAMSTER_WHEEL_BLOCK_ENTITY, poseStack, buffer, packedLight, packedOverlay);
            }
            if (state.is(ModBlocks.EASEL_BLOCK.get())) {
                this.blockEntityRenderDispatcher.renderItem(EASEL_BLOCK_ENTITY, poseStack, buffer, packedLight, packedOverlay);
            }
        }
        if (pStack.is(ModItems.CLAY_STAFF.get())) {
            ClayStaffModel.renderAsItem(pStack, pDisplayContext, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    public static ModBlockEntityWithoutLevelRenderer getOrCreateInstance() {
        if (instance == null) {
            instance = new ModBlockEntityWithoutLevelRenderer();
        }
        return instance;
    }
}