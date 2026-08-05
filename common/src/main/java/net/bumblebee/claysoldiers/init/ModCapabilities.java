package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.block.BlockEntityWithEnergy;
import net.bumblebee.claysoldiers.block.blueprint.EaselBlockEntity;
import net.bumblebee.claysoldiers.block.cacti.ClayCactusBlockEntity;
import net.bumblebee.claysoldiers.block.chipassembler.ChipAssemblerBlockEntity;
import net.bumblebee.claysoldiers.block.hammock.SugarCaneHammockBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.soldiercontainer.BaseBlockEntityWithSoldier;
import net.bumblebee.claysoldiers.block.soldiercontainer.ClayMobContainer;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
import net.bumblebee.claysoldiers.capability.BlueprintRequestHandler;
import net.bumblebee.claysoldiers.energy.BatteryProperties;
import net.bumblebee.claysoldiers.item.BatteryItem;
import net.bumblebee.claysoldiers.platform.services.IEnergyHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

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
        event.accept(ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(), ModCapabilities::getContainer);
        event.accept(ModBlockEntities.SUGAR_CANE_HAMMOCK_BLOCK_ENTITY.get(), ModCapabilities::getContainer);
        event.accept(ModBlockEntities.CLAY_CACTUS_BLOCK_ENTITY.get(), ModCapabilities::getContainer);
    }

    public static void registerEnergy(BiConsumer<BlockEntityType<?>, BiFunction<BlockEntity, Direction, ?>> event) {
        event.accept(ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(), HamsterWheelBlockEntity::getEnergyStorage);
        event.accept(ModBlockEntities.CHIP_ASSEMBLER_BLOCK_ENTITY.get(), (s, c) -> s instanceof BlockEntityWithEnergy entity ? entity.getEnergyStorage(c) : null);
        event.accept(ModBlockEntities.SOLDIER_CHARGING_PAD.get(), (s, c) -> s instanceof BlockEntityWithEnergy entity ? entity.getEnergyStorage(c) : null);

    }

    public static void registerItemEnergy(ItemEnergyEvent event) {
        event.register(BatteryItem::getMaxBatteryEnergyStorage, ModItems.SMALL_BATTERY, ModItems.LARGE_BATTERY);
    }

    private static ClayMobContainer getContainer(BlockEntity entity) {
        return entity instanceof BaseBlockEntityWithSoldier b ? b.getClayMobContainer() : null;
    }

    public interface ItemEnergyEvent {
        void register(Function<ItemStack, @Nullable BatteryProperties> maxCapacity, ItemLike... items);
    }
}
