package net.bumblebee.claysoldiers.entity.client.renderstates;

import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.armor.SoldierMultiWearable;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessoryData;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessoryKey;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.AccessoryRenderState;
import net.bumblebee.claysoldiers.datamap.armor.accessories.custom.GliderAccessoryData;
import net.bumblebee.claysoldiers.datamap.armor.accessories.custom.SkullAccessoryData;
import net.bumblebee.claysoldiers.entity.boss.BossClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMapReader;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class AbstractClaySoldierRenderState extends ClayMobRenderState {
    public boolean isZombie;
    public boolean isAggressive;
    public boolean hasShieldInOffhand;
    public boolean hasShieldInMainHand;
    public boolean isFallingWithGlider;
    public boolean veryAngry;
    public SoldierEquipmentSlot gliderSlot;
    public ItemStack carriedItemStack;
    public final ItemStackRenderState carriedItemRenderState = new ItemStackRenderState();
    public AbstractClaySoldierEntity.RidingPose ridingPose;
    public int id;
    public int skinVariantId;

    public boolean offhandOccupied;
    public boolean mainhandOccupied;
    public boolean isAlive;
    public boolean isFalling;
    public float fallFlyingTimeInTicks;


    public boolean isNightForVampire = false;
    public int previousTeamColor = -1;
    @Nullable
    public BossClaySoldierEntity.BossTypes bossType = null;

    public SoldierPropertyMapReader allProperties;

    public int offsetColor;
    private final Map<SoldierEquipmentSlot, ItemStackWithEffect> inventory = new EnumMap<>(SoldierEquipmentSlot.class);

    public float capeFlap;
    public float capeLean;
    public float capeLean2;

    public float swelling;


    public AccessoryRenderState accessoryRenderState;

    public boolean hasShieldInHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? hasShieldInMainHand : hasShieldInOffhand;
    }

    public ItemStackWithEffect getItemBySlot(SoldierEquipmentSlot pSlot) {
        return inventory.get(pSlot);
    }

    public void setUpInventory(AbstractClaySoldierEntity claySoldierEntity) {
        for (SoldierEquipmentSlot slot : SoldierEquipmentSlot.values()) {
            inventory.put(slot, claySoldierEntity.getItemBySlot(slot));
        }
    }

    public float fallFlyingScale() {
        return Mth.clamp(this.fallFlyingTimeInTicks * this.fallFlyingTimeInTicks / 100.0F, 0.0F, 1.0F);
    }

    public static void extractCloakState(AbstractClaySoldierEntity claySoldierEntity, AbstractClaySoldierRenderState renderState, float partialTick) {
        double d0 = Mth.lerp(partialTick, claySoldierEntity.xCloakO, claySoldierEntity.xCloak) - Mth.lerp(partialTick, claySoldierEntity.xo, claySoldierEntity.getX());
        double d1 = Mth.lerp(partialTick, claySoldierEntity.yCloakO, claySoldierEntity.yCloak) - Mth.lerp(partialTick, claySoldierEntity.yo, claySoldierEntity.getY());
        double d2 = Mth.lerp(partialTick, claySoldierEntity.zCloakO, claySoldierEntity.zCloak) - Mth.lerp(partialTick, claySoldierEntity.zo, claySoldierEntity.getZ());
        float f = Mth.rotLerp(partialTick, claySoldierEntity.yBodyRotO, claySoldierEntity.yBodyRot);
        double d3 = Mth.sin(f * (float) (Math.PI / 180.0));
        double d4 = (-Mth.cos(f * (float) (Math.PI / 180.0)));
        renderState.capeFlap = (float) d1 * 10.0F;
        renderState.capeFlap = Mth.clamp(renderState.capeFlap, -6.0F, 32.0F);
        renderState.capeLean = (float) (d0 * d3 + d2 * d4) * 100.0F;
        renderState.capeLean = renderState.capeLean * (1.0F - renderState.fallFlyingScale());
        renderState.capeLean = Mth.clamp(renderState.capeLean, 0.0F, 150.0F);
        renderState.capeLean2 = (float) (d0 * d4 - d2 * d3) * 100.0F;
        renderState.capeLean2 = Mth.clamp(renderState.capeLean2, -20.0F, 20.0F);
        float f1 = Mth.lerp(partialTick, claySoldierEntity.oBob, claySoldierEntity.bob);
        float f2 = partialTick;//Mth.lerp(partialTick, player.walkDistO, player.walkDist);
        renderState.capeFlap = renderState.capeFlap + Mth.sin(f2 * 6.0F) * 32.0F * f1;
    }

    public static void extractAccessoryRenderState(AbstractClaySoldierEntity claySoldierEntity, AbstractClaySoldierRenderState renderState, ItemModelResolver itemModelResolver) {
        var acc = new AccessoryRenderState();

        Map<SoldierAccessoryKey<?>, SoldierAccessoryData> map = new HashMap<>();
        for (SoldierEquipmentSlot slot : SoldierEquipmentSlot.values()) {
            var multi = getMulti(claySoldierEntity, slot);
            if (multi != null) {
                map.putAll(multi.getAccessories());
            }
        }

        acc.renderableAccessories = map;

        for (SoldierAccessoryData data : acc.renderableAccessories.values()) {
            if (data instanceof SkullAccessoryData skullRenderable) {
                itemModelResolver.updateForLiving(acc.skullAccessory, skullRenderable.getHeadStack(), ItemDisplayContext.HEAD, claySoldierEntity);
            } else if (data instanceof GliderAccessoryData gliderRenderable) {
                itemModelResolver.updateForLiving(acc.gliderAccessory, gliderRenderable.getGliderStack(), ItemDisplayContext.HEAD, claySoldierEntity);
            }
        }

        renderState.accessoryRenderState = acc;
        acc.renderStateFrom = renderState;
        acc.ageInTicks = renderState.ageInTicks;
        acc.chestEquipment = renderState.chestEquipment;
        acc.id = renderState.id;
        acc.offsetColor = renderState.offsetColor;
        acc.isFalling = renderState.isFalling;
        acc.isInSittingPose = renderState.isInSittingPose;
        acc.mainArm = renderState.mainArm;
        acc.hasShieldInMainHand = renderState.hasShieldInMainHand;
        acc.hasShieldInOffhand = renderState.hasShieldInOffhand;
        acc.wornHeadProfile = renderState.wornHeadProfile;
        acc.wornHeadAnimationPos = renderState.wornHeadAnimationPos;
        acc.overlayCords0 = LivingEntityRenderer.getOverlayCoords(renderState, 0.0F);
        acc.outlineColor = renderState.outlineColor;
        acc.isInWater = renderState.isInWater;
    }

    @Nullable
    private static SoldierMultiWearable getMulti(AbstractClaySoldierEntity claySoldier, SoldierEquipmentSlot slot) {
        ItemStackWithEffect stackWithEffect = claySoldier.getItemBySlot(slot);

        if (stackWithEffect == null || stackWithEffect.isEmpty()) {
            return null;
        }
        return stackWithEffect.wearableEffectMap();
    }
}
