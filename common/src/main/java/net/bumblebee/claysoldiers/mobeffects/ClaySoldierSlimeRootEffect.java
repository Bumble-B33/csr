package net.bumblebee.claysoldiers.mobeffects;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ClaySoldierSlimeRootEffect extends MobEffect {
    public ClaySoldierSlimeRootEffect() {
        super(MobEffectCategory.HARMFUL, 0x80C71F);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "slime_root_slow"), -10D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
