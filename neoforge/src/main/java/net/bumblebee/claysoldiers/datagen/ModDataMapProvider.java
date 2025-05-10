package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.init.ModDataMaps;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.soldierproperties.SoldierProperty;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.SoldierVehicleProperties;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.UnitProperty;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttacks;
import net.bumblebee.claysoldiers.soldierproperties.types.BreathHoldPropertyType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModDataMapProvider extends DataMapProvider {
    public ModDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        builder(ModDataMaps.SOLDIER_VEHICLE_PROPERTIES)
                .add(EntityType.ENDERMITE.builtInRegistryHolder(),
                        new SoldierVehicleProperties(
                                SoldierPropertyMap.of(
                                        new SoldierProperty<>(SoldierPropertyTypes.TELEPORTATION.get(), UnitProperty.INSTANCE),
                                        new SoldierProperty<>(SoldierPropertyTypes.TELEPORT_TO_OWNER.get(), UnitProperty.INSTANCE)
                                )), false)
                .add(EntityType.TURTLE.builtInRegistryHolder(),
                        new SoldierVehicleProperties(
                                SoldierPropertyMap.of(
                                        SoldierPropertyTypes.BREATH_HOLD.get().createProperty(BreathHoldPropertyType.MAX_BREATH_HOLD),
                                        SoldierPropertyTypes.ATTACK_RANGE.get().createProperty(0.25f)
                                )), false)
                .add(ModEntityTypes.CLAY_HORSE_ENTITY.get().builtInRegistryHolder(),
                        new SoldierVehicleProperties(
                                SoldierPropertyMap.of(
                                        new SoldierProperty<>(SoldierPropertyTypes.HEAVY.get(), 0.2f),
                                        new SoldierProperty<>(SoldierPropertyTypes.ATTACK_RANGE.get(), 0.2f)
                                )), false)
                .add(ModEntityTypes.CLAY_PEGASUS_ENTITY.get().builtInRegistryHolder(),
                        new SoldierVehicleProperties(
                                SoldierPropertyMap.of(
                                        new SoldierProperty<>(SoldierPropertyTypes.HEAVY.get(), 0.25f),
                                        new SoldierProperty<>(SoldierPropertyTypes.ATTACK_RANGE.get(), 0.25f)
                                )), false)
                .add(EntityType.RABBIT.builtInRegistryHolder(),
                        new SoldierVehicleProperties(
                                SoldierPropertyMap.of(
                                        new SoldierProperty<>(SoldierPropertyTypes.ATTACK_RANGE.get(), 0.3f)
                                )), false)
                .add(EntityType.SLIME.builtInRegistryHolder(),
                        new SoldierVehicleProperties(
                                SoldierPropertyMap.of(
                                        new SoldierProperty<>(SoldierPropertyTypes.DAMAGE.get(), 0.3f),
                                        new SoldierProperty<>(SoldierPropertyTypes.SPECIAL_ATTACK.get(), List.of(new SpecialAttacks.CritAttack(SpecialAttackType.RANGED, 1, 0.6f)))
                                )), false);
    }
}