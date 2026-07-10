package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.soldiercontainer.ClayMobContainer;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
import net.bumblebee.claysoldiers.capability.BlueprintRequestHandler;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

public class NeoForgeCapabilities {
    public static final BlockCapability<BlueprintRequestHandler, Void> BLUEPRINT_REQUEST_CAP =
            BlockCapability.createVoid(
                    Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprint_request_handler"),
                    BlueprintRequestHandler.class
            );

    public static final BlockCapability<AssignableWorksiteCapability, Void> ASSIGNABLE_POI_CAP =
            BlockCapability.createVoid(
                    Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "assignable_poi_cap"),
                    AssignableWorksiteCapability.class
            );

    public static final BlockCapability<ClayMobContainer, Void> CLAY_MOB_CONTAINER =
            BlockCapability.createVoid(
                    Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_mob_container"),
                    ClayMobContainer.class
            );

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        ModCapabilities.registerBlueprint((blockEntityType, lookup) ->
                event.registerBlockEntity(BLUEPRINT_REQUEST_CAP, blockEntityType, (e, c) -> lookup.apply(e)));
        ModCapabilities.registerAssignablePoi((blockEntityType, lookup) ->
                event.registerBlockEntity(ASSIGNABLE_POI_CAP, blockEntityType, (o, _) -> lookup.apply(o)));
        ModCapabilities.registerClayMobContainer((blockEntityType, lookup) ->
                event.registerBlockEntity(CLAY_MOB_CONTAINER, blockEntityType, (o, _) -> lookup.apply(o)));

        ModCapabilities.registerEnergy((type, lookup) ->
                event.registerBlockEntity(Capabilities.Energy.BLOCK, type, (b, c) -> (EnergyHandler) lookup.apply(b, c)));
    }
}
