package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface FakeClaySoldierAccess {
    void submit(PoseStack poseStack, SubmitNodeCollector buffer, int packedLight, CameraRenderState cameraRenderState, float partialTicks);

    void setUpCape();

    void increaseTickCount();

    boolean isWaxed();

    ItemStack getAsItem();

    Component displayName();

    ClayMobTeam getClayTeam();

    int tickCount();

    int colorOffset();
}
