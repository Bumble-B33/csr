package net.bumblebee.claysoldiers.entity.client.renderstates;

import net.bumblebee.claysoldiers.ClaySoldiersClient;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class ClayMobRenderState extends HumanoidRenderState {
    public boolean slimeRoot = false;
    public boolean isInSittingPose = false;
    public boolean isWaxed = false;
    public boolean isUsingPoi = false;
    public ClayMobTeam clayTeam;
    public int clayTeamColor;
    public Component workStatus;
    public boolean shouldShowStatus;
    public int lightLevel;
    public Vec3 statusAttachmentPoint;

    public static boolean shouldShowWorkStatus(ClayMobEntity clayMob) {
        return (Minecraft.getInstance().player.equals(clayMob.getClayTeamOwner()) && ClaySoldiersClient.hasPlayerClayGogglesEquipped()) && !clayMob.isVehicle();
    }

    public static void extractClayMobRenderState(ClayMobEntity clayMob, ClayMobRenderState clayMobRenderState, float partialTick) {
        clayMobRenderState.slimeRoot = clayMob.isSlimeRooted();
        clayMobRenderState.isInSittingPose = clayMob.isInSittingPose();
        clayMobRenderState.isWaxed = clayMob.isWaxed();
        clayMobRenderState.isUsingPoi = clayMob.usingPoi();
        clayMobRenderState.clayTeam = clayMob.getClayTeamHolder().value();
        clayMobRenderState.clayTeamColor = clayMobRenderState.clayTeam.getColor(clayMob, partialTick);
        clayMobRenderState.workStatus = clayMob.getWorkStatus();
        clayMobRenderState.shouldShowStatus = ClayMobRenderState.shouldShowWorkStatus(clayMob);
        clayMobRenderState.lightLevel = getLightLevel(clayMob);
        clayMobRenderState.statusAttachmentPoint = clayMob.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, clayMob.getYRot(partialTick));
    }

    private static int getLightLevel(Entity pEntity) {
        final BlockPos pos = pEntity.getOnPos();
        final int bLight = pEntity.level().getBrightness(LightLayer.BLOCK, pos);
        final int sLight = pEntity.level().getBrightness(LightLayer.SKY, pos);
        return LightCoordsUtil.pack(bLight, sLight);
    }
}
