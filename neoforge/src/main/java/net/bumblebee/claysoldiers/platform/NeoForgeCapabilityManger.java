package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.IHamsterWheelEnergyStorage;
import net.bumblebee.claysoldiers.cap.NeoForgeBlockStorageCapability;
import net.bumblebee.claysoldiers.cap.NeoForgeEnergy;
import net.bumblebee.claysoldiers.capability.AssignablePoiCapability;
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

public class NeoForgeCapabilityManger extends AbstractCapabilityManger {
    @ApiStatus.Internal
    public NeoForgeCapabilityManger() {
    }

    @Override
    public IHamsterWheelEnergyStorage createEnergyStorage(HamsterWheelBlockEntity hamsterWheelBlockEntity) {
        return new NeoForgeEnergy(hamsterWheelBlockEntity);
    }

    @Override
    public IBlockCache<IBlockStorageAccess> create(ServerLevel level, BlockPos pos) {
        return new NeoForgeBlockStorageCapability(BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, level, pos, null));
    }

    @Override
    public IBlockCache<BlueprintRequestHandler> createBlueprint(ServerLevel level, BlockPos pos) {
        return new NeoforgeBlueprintCache(BlockCapabilityCache.create(ModCapabilities.BLUEPRINT_REQUEST_CAP, level, pos, null));
    }

    @Override
    public IBlockCache<AssignablePoiCapability> createPoiCache(ServerLevel level, BlockPos pos) {
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
    }

    private record NeoForgePoiCache(BlockCapabilityCache<AssignablePoiCapability, Void> cache) implements IBlockCache<AssignablePoiCapability> {
        @Override
        public BlockPos pos() {
            return cache.pos();
        }

        @Override
        public @Nullable AssignablePoiCapability getCapability() {
            return cache.getCapability();
        }
    }
}
