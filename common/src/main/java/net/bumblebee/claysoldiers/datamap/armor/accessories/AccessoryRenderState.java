package net.bumblebee.claysoldiers.datamap.armor.accessories;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Collection;

public class AccessoryRenderState {
    public boolean isInvisible;
    public ItemStack chestEquipment;
    public int offsetColor;
    public int id;
    public float ageInTicks;
    public Object renderStateFrom;
    public boolean isFalling;

    public boolean isInSittingPose;
    public HumanoidArm mainArm;

    public boolean hasShieldInOffhand;
    public boolean hasShieldInMainHand;

    public ResolvableProfile wornHeadProfile;
    public float wornHeadAnimationPos;
    public int overlayCords0;

    public Collection<RenderableAccessory> renderableAccessories;
    public final ItemStackRenderState skullAccessory = new ItemStackRenderState();
    public final ItemStackRenderState gliderAccessory = new ItemStackRenderState();

    public boolean hasShieldInHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? hasShieldInMainHand : hasShieldInOffhand;
    }
}
