package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.block.blueprint.EaselBlockEntity;
import net.bumblebee.claysoldiers.block.cacti.ClayCactusBlockEntity;
import net.bumblebee.claysoldiers.block.chipassembler.ChipAssemblerBlockEntity;
import net.bumblebee.claysoldiers.block.hammock.SugarCaneHammockBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.soldiercontainer.BlockEntityWithSoldier;
import net.bumblebee.claysoldiers.block.soldiercontainer.BlockEntityWithSoldiers;
import net.bumblebee.claysoldiers.block.soldiercontainer.ClayMobContainer;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
import net.bumblebee.claysoldiers.capability.BlueprintRequestHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ModCapabilities {
    public static void registerBlueprint(BiConsumer<BlockEntityType<?>, Function<BlockEntity, BlueprintRequestHandler>> event) {
        event.accept(ModBlockEntities.EASEL_BLOCK_ENTITY.get(), s -> s instanceof EaselBlockEntity easel ? easel.getBlueprintRequestHandler() : null);
    }

    public static void registerAssignablePoi(BiConsumer<BlockEntityType<?>, Function<BlockEntity, AssignableWorksiteCapability>> event) {
        event.accept(ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(), s -> s instanceof HamsterWheelBlockEntity entity ? entity.getPoiCap() : null);
        event.accept(ModBlockEntities.SUGAR_CANE_HAMMOCK_BLOCK_ENTITY.get(), s -> s instanceof SugarCaneHammockBlockEntity entity ? entity.getPoiCap() : null);
        event.accept(ModBlockEntities.CLAY_CACTUS_BLOCK_ENTITY.get(), s -> s instanceof ClayCactusBlockEntity entity ? entity.getPoiCap() : null);
    }

    public static void registerClayMobContainer(BiConsumer<BlockEntityType<?>, Function<BlockEntity, ClayMobContainer>> event) {
        event.accept(ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(), s -> s instanceof BlockEntityWithSoldier entity ? entity.getClayMobContainer() : null);
        event.accept(ModBlockEntities.SUGAR_CANE_HAMMOCK_BLOCK_ENTITY.get(), s -> s instanceof BlockEntityWithSoldier entity ? entity.getClayMobContainer() : null);
        event.accept(ModBlockEntities.CLAY_CACTUS_BLOCK_ENTITY.get(), s -> s instanceof BlockEntityWithSoldiers entity ? entity.getClayMobContainer() : null);
    }

    public static void registerEnergy(BiConsumer<BlockEntityType<?>, BiFunction<BlockEntity, Direction, ?>> event) {
        event.accept(ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(), HamsterWheelBlockEntity::getEnergyStorage);
        event.accept(ModBlockEntities.CHIP_ASSEMBLER_BLOCK_ENTITY.get(), (s, c) -> s instanceof ChipAssemblerBlockEntity entity ? entity.getEnergyStorage(c) : null);
    }
}
