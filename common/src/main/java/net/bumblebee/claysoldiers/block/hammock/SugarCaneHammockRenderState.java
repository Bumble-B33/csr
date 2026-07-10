package net.bumblebee.claysoldiers.block.hammock;

import net.bumblebee.claysoldiers.entity.client.FakeClaySoldierAccess;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jetbrains.annotations.Nullable;

public class SugarCaneHammockRenderState extends BlockEntityRenderState {
    @Nullable
    public FakeClaySoldierAccess claySoldier = null;
    float yRot = 0;
}
