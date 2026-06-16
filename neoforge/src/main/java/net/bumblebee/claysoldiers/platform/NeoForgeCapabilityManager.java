package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.block.chipassembler.ChipEnergyStorage;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelEnergyStorage;
import net.bumblebee.claysoldiers.cap.HamsterWheelNeoForgeEnergy;
import net.bumblebee.claysoldiers.cap.NeoForgeBlockStorageCapability;
import net.bumblebee.claysoldiers.cap.NeoForgeChipAssemblerEnergy;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
import net.bumblebee.claysoldiers.capability.BlueprintRequestHandler;
import net.bumblebee.claysoldiers.capability.IBlockCache;
import net.bumblebee.claysoldiers.capability.IBlockStorageAccess;
import net.bumblebee.claysoldiers.init.ModCapabilities;
import net.bumblebee.claysoldiers.platform.services.AbstractCapabilityManger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class NeoForgeCapabilityManager extends AbstractCapabilityManger {
    @ApiStatus.Internal
    public NeoForgeCapabilityManager() {
    }

    @Override
    public HamsterWheelEnergyStorage createEnergyStorage(HamsterWheelBlockEntity hamsterWheelBlockEntity) {
        return new HamsterWheelNeoForgeEnergy(hamsterWheelBlockEntity);
    }

    @Override
    public ChipEnergyStorage createEnergyChipStorage() {
        return new NeoForgeChipAssemblerEnergy();
    }

    @Override
    public IBlockCache<IBlockStorageAccess> create(ServerLevel level, BlockPos pos) {
        return new NeoForgeBlockStorageCapability(BlockCapabilityCache.create(Capabilities.Item.BLOCK, level, pos, null));
    }

    @Override
    public IBlockCache<BlueprintRequestHandler> createBlueprint(ServerLevel level, BlockPos pos) {
        return new NeoforgeBlueprintCache(BlockCapabilityCache.create(ModCapabilities.BLUEPRINT_REQUEST_CAP, level, pos, null));
    }

    @Override
    public IBlockCache<AssignableWorksiteCapability> createPoiCache(ServerLevel level, BlockPos pos) {
        return new NeoForgePoiCache(BlockCapabilityCache.create(ModCapabilities.ASSIGNABLE_POI_CAP, level, pos, null));
    }

    private record NeoforgeBlueprintCache(BlockCapabilityCache<BlueprintRequestHandler, Void> cache) implements IBlockCache<BlueprintRequestHandler> {
        @Override
        public BlockPos pos() {
            return cache.pos();
        }

        @Override
        public @Nullable BlueprintRequestHandler getCapability() {
            return cache.getCapability();
        }

        @Override
        public @NonNull String toString() {
            return "Cache: %s".formatted(getCapability());
        }
    }

    private record NeoForgePoiCache(BlockCapabilityCache<AssignableWorksiteCapability, Void> cache) implements IBlockCache<AssignableWorksiteCapability> {
        @Override
        public BlockPos pos() {
            return cache.pos();
        }

        @Override
        public @Nullable AssignableWorksiteCapability getCapability() {
            return cache.getCapability();
        }

        @Override
        public @NonNull String toString() {
            return "Cache: %s".formatted(getCapability());
        }
    }
}
