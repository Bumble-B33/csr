package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> CLAY_HURT = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_hurt"));
    public static final ResourceKey<DamageType> CLAY_ON_FIRE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_on_fire"));

    public static void boostrap(BootstrapContext<DamageType> pContext) {
        pContext.register(CLAY_HURT, new DamageType("clayHurt", 0.0F, DamageEffects.POKING));
        pContext.register(CLAY_ON_FIRE, new DamageType("clayOnFire", 0.0F, DamageEffects.BURNING));
    }

}
