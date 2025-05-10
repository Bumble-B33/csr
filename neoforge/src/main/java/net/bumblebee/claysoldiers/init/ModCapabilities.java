package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.cap.ClaySoldierItemHandler;
import net.bumblebee.claysoldiers.capability.AssignablePoiCapability;
import net.bumblebee.claysoldiers.capability.BlueprintRequestHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class ModCapabilities {
    public static final BlockCapability<BlueprintRequestHandler, Void> BLUEPRINT_REQUEST_CAP =
            BlockCapability.createVoid(
                    ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprint_request_handler"),
                    BlueprintRequestHandler.class
            );

    public static final BlockCapability<AssignablePoiCapability, Void> ASSIGNABLE_POI_CAP =
            BlockCapability.createVoid(
                    ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "assignable_poi_cap"),
                    AssignablePoiCapability.class
            );

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                Capabilities.EnergyStorage.BLOCK,
                new IBlockCapabilityProvider<>() {
                    @Override
                    public @Nullable IEnergyStorage getCapability(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Direction context) {
                        var en = HamsterWheelBlockEntity.getEnergyStorage(blockEntity, context);
                        return en == null ? null : (IEnergyStorage) en;
                    }
                },
                ModBlocks.HAMSTER_WHEEL_BLOCK.get()
        );
        event.registerBlockEntity(BLUEPRINT_REQUEST_CAP, ModBlockEntities.EASEL_BLOCK_ENTITY.get(),
                (easel, unused) -> easel.getBlueprintRequestHandler());
        event.registerBlockEntity(ASSIGNABLE_POI_CAP, ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(),
                (wheel, unused) -> wheel.getPoiCap());

        event.registerEntity(Capabilities.ItemHandler.ENTITY, ModEntityTypes.CLAY_SOLDIER_ENTITY.get(), (soldier, context) -> new ClaySoldierItemHandler(soldier));
    }
}
