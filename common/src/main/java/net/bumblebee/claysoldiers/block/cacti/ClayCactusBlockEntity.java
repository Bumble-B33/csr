package net.bumblebee.claysoldiers.block.cacti;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.soldiercontainer.BlockEntityWithSoldiers;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.StatInfoDisplay;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.init.ModPoiTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ClayCactusBlockEntity extends BlockEntityWithSoldiers implements StatInfoDisplay {
    private static final Identifier POI_ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "cactus_house");
    private final AssignableWorksiteCapability poiCap = new AssignableWorksiteCapability() {
        @Override
        public boolean canUse(ClayMobEntity clayMob) {
            if (clayMob instanceof AbstractClaySoldierEntity) {
                return hasSpace();
            }
            return false;
        }

        @Override
        public int onUse(ClayMobEntity clayMob) {
            if (clayMob instanceof AbstractClaySoldierEntity soldier) {
                if (addSoldier(soldier)) {
                    return 1;
                }
            }

            throw new IllegalArgumentException(clayMob + " cannot use this poi");
        }

        @Override
        public Identifier descriptionId() {
            return POI_ID;
        }
    };

    public ClayCactusBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.CLAY_CACTUS_BLOCK_ENTITY.get(), worldPosition, blockState, ModPoiTypes.CLAY_CACTUS_POI_KEY, ModPoiTypes.CLAY_CACTUS.get());
    }

    @Override
    protected void onUpdate(BlockState oldState, BlockState newState, UpdateOperation operation, int flags, int currentSoldiers) {
        super.onUpdate(oldState, newState, operation, flags & 0b101, currentSoldiers);
        level.setBlock(worldPosition, newState.setValue(ClayCactusBlock.COUNT, currentSoldiers), (flags & 0b010) | 0b001);
    }

    public AssignableWorksiteCapability getPoiCap() {
        return poiCap;
    }

    @Override
    protected Vec3 getExitPosition() {
        return Vec3.atBottomCenterOf(worldPosition).add(0, 1, 0);
    }

    @Override
    public void getStatDisplay(List<Component> list, LivingEntity viewer) {
        list.add(getBlockState().getBlock().getName());
        addSoldierData(list, viewer);
    }


}
