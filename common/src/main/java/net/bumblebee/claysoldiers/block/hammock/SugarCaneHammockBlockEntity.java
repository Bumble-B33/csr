package net.bumblebee.claysoldiers.block.hammock;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.soldiercontainer.BlockEntityWithSoldier;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.StatInfoDisplay;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.init.ModPoiTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SugarCaneHammockBlockEntity extends BlockEntityWithSoldier implements StatInfoDisplay {
    public static final Identifier WORKSITE_ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "sugar_cane_hammock");
    private final AssignableWorksiteCapability poiCap = new AssignableWorksiteCapability() {
        @Override
        public boolean canUse(ClayMobEntity clayMob) {
            return clayMob instanceof AbstractClaySoldierEntity soldier && soldier.getSoldierSize() <= 1.25f;
        }

        @Override
        public int onUse(ClayMobEntity clayMob) {
            if (clayMob instanceof AbstractClaySoldierEntity soldier) {
                addSoldier(soldier);
                return 1;
            } else {
                throw new IllegalArgumentException(clayMob + " cannot use this poi");
            }
        }

        @Override
        public Identifier descriptionId() {
            return WORKSITE_ID;
        }
    };

    public SugarCaneHammockBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.SUGAR_CANE_HAMMOCK_BLOCK_ENTITY.get(), worldPosition, blockState, ModPoiTypes.SINGLE_SOLDIER_CONTAINER_POI_KEY, ModPoiTypes.SINGLE_SOLDIER_CONTAINER.get());
    }

    public AssignableWorksiteCapability getPoiCap() {
        return poiCap;
    }

    @Override
    protected Vec3 getExitPosition() {
        return Vec3.atBottomCenterOf(worldPosition);
    }

    public Direction getFacing() {
        return getBlockState().getValue(SugarCaneHammockBlock.FACING);
    }

    @Override
    public void getStatDisplay(List<Component> list, LivingEntity viewer) {
        list.add(getBlockState().getBlock().getName());
        addSoldierData(list, viewer);
    }

    @Override
    public String toString() {
        return "HamsterWheelBlockEntity(%s, %s)".formatted(worldPosition.toShortString(), level);
    }
}
