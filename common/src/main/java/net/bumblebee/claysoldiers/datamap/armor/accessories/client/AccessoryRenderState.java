package net.bumblebee.claysoldiers.datamap.armor.accessories.client;

import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessoryData;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessoryKey;
import net.bumblebee.claysoldiers.entity.client.renderstates.AbstractClaySoldierRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Map;

public class AccessoryRenderState {
    public boolean isInvisible;
    public ItemStack chestEquipment;
    public int offsetColor;
    public int id;
    public float ageInTicks;
    public AbstractClaySoldierRenderState renderStateFrom;
    public boolean isFalling;

    public boolean isInSittingPose;
    public HumanoidArm mainArm;

    public boolean hasShieldInOffhand;
    public boolean hasShieldInMainHand;

    public ResolvableProfile wornHeadProfile;
    public float wornHeadAnimationPos;
    public int overlayCords0;

    public int outlineColor;
    public boolean isInWater;

    public Map<SoldierAccessoryKey<?>, SoldierAccessoryData> renderableAccessories;
    public final ItemStackRenderState skullAccessory = new ItemStackRenderState();
    public final ItemStackRenderState gliderAccessory = new ItemStackRenderState();

    public boolean hasShieldInHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? hasShieldInMainHand : hasShieldInOffhand;
    }
}
