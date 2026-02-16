package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.ClaySoldierFabric;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.IHamsterWheelEnergyStorage;
import net.bumblebee.claysoldiers.capability.*;
import net.bumblebee.claysoldiers.platform.services.AbstractCapabilityManger;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.Nullable;

public class FabricCapabilityManger extends AbstractCapabilityManger implements PreparableReloadListener {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "csr_capabilities");

    @Override
    public IBlockCache<IBlockStorageAccess> create(ServerLevel level, BlockPos pos) {
        return new FabricBlockStorageCache(level, pos);
    }

    @Override
    public IBlockCache<BlueprintRequestHandler> createBlueprint(ServerLevel level, BlockPos pos) {
        return new FabricBlueprintRequestCache(BlockApiCache.create(ClaySoldierFabric.BLUEPRINT_REQUEST_HANDLER_LOOKUP, level, pos));
    }

    @Override
    public IBlockCache<AssignableWorksiteCapability> createPoiCache(ServerLevel level, BlockPos pos) {
        return new FabricPoiCache(BlockApiCache.create(ClaySoldierFabric.ASSIGNABLE_POI_LOOKUP, level, pos));
    }

    @Override
    public IHamsterWheelEnergyStorage createEnergyStorage(HamsterWheelBlockEntity hamsterWheelBlockEntity) {
        return new FabricEnergyStorage(hamsterWheelBlockEntity);
    }

    private record FabricBlueprintRequestCache(BlockApiCache<BlueprintRequestHandler, Void> cache) implements IBlockCache<BlueprintRequestHandler> {
        @Override
        public BlockPos pos() {
            return cache.getPos();
        }

        @Override
        public @Nullable BlueprintRequestHandler getCapability() {
            return cache.find(null);
        }
    }
    private record FabricPoiCache(BlockApiCache<AssignableWorksiteCapability, Void> cache) implements IBlockCache<AssignableWorksiteCapability> {
        @Override
        public BlockPos pos() {
            return cache.getPos();
        }

        @Override
        public @Nullable AssignableWorksiteCapability getCapability() {
            return cache.find(null);
        }
    }

}
