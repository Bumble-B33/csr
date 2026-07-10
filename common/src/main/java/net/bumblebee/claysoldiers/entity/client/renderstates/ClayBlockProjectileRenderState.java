package net.bumblebee.claysoldiers.entity.client.renderstates;

import net.bumblebee.claysoldiers.entity.client.FakeClaySoldierAccess;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.Nullable;

public class ClayBlockProjectileRenderState extends EntityRenderState {
    public float size;
    public int rot = 0;
    public float partialRot;
    @Nullable
    public FakeClaySoldierAccess clientClaySoldierEntity;
}
