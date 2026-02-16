package net.bumblebee.claysoldiers.entity.client.renderstates;

import net.bumblebee.claysoldiers.entity.variant.ClayHorseVariants;
import net.bumblebee.claysoldiers.item.itemeffectholder.HorseWearableItemStack;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class ClayHorseRenderState extends EquineRenderState {
    public boolean isInSittingPose;
    public boolean isPassenger;
    public ClayHorseVariants variant;
    public boolean onGround;
    public Vec3 statusAttachmentPoint;

    public boolean shouldShowWorkStatus;
    public Component workStatus;

    public HorseWearableItemStack clayHorseArmor;
    public int clayHorseArmorColor;
    public boolean isSlimeRooted;
    public boolean hasHorn;

}
