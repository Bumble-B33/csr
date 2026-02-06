package net.bumblebee.claysoldiers.block.blueprint;

import net.bumblebee.claysoldiers.blueprint.BlueprintTemplateSettings;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EaselBlockEntityRenderState extends BlockEntityRenderState {
    public float yRot;
    public boolean hasBlueprintData;
    public boolean mirrored;
    public BlueprintTemplateSettings settings;
    public boolean isFinished;
    public VoxelShape shape;
    public boolean hasStarted;
}
