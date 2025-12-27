package net.bumblebee.claysoldiers.entity.client.renderstates;

import net.minecraft.client.renderer.entity.state.VexRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class ClayWraithRenderState extends VexRenderState {
    public boolean isInSittingPose;
    public boolean shouldShowWorkStatus;
    public Vec3 statusAttachmentPoint;
    public Component workStatus;

    public int lifePoint;
    public boolean hasLimitedLife;
    public int clayTeamColor;
}
