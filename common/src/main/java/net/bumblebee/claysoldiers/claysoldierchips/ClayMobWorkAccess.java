package net.bumblebee.claysoldiers.claysoldierchips;

import net.bumblebee.claysoldiers.entity.ClayMobWorkStatusManger;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public interface ClayMobWorkAccess {
    void setDataWorkStatus(byte id);

    byte getDataWorkStatus();

    default @Nullable Component getWorkStatus() {
        return ClayMobWorkStatusManger.get(getDataWorkStatus());
    }

    default void setBreak() {
        setDataWorkStatus(ClayMobWorkStatusManger.BREAK_ID);
    }

    default void setSearching() {
        setDataWorkStatus(ClayMobWorkStatusManger.SEARCHING_ID);
    }

    default void setCarrying() {
        setDataWorkStatus(ClayMobWorkStatusManger.CARRYING_ID);
    }

    default void setRequiresPoi() {
        setDataWorkStatus(ClayMobWorkStatusManger.REQUIRES_POI_ID);
    }

    default void setReturning() {
        setDataWorkStatus(ClayMobWorkStatusManger.RETURNING_ID);
    }

    default void setCannotFindItem() {
        setDataWorkStatus(ClayMobWorkStatusManger.CANNOT_FIND_ITEM_ID);
    }

    default void setIsAnker() {
        setDataWorkStatus(ClayMobWorkStatusManger.IS_ANKER_ID);
    }

    default void setSearchingForWater() {
        setDataWorkStatus(ClayMobWorkStatusManger.SEARCHING_FOR_WATER_ID);
    }

    default void setSearchingAnker() {
        setDataWorkStatus(ClayMobWorkStatusManger.SEARCHING_ANKER_ID);
    }

    default void setFishing() {
        setDataWorkStatus(ClayMobWorkStatusManger.FISHING_ID);
    }

    default void setExtracting() {
        setDataWorkStatus(ClayMobWorkStatusManger.EXTRACTING_ID);
    }

    default void setInserting() {
        setDataWorkStatus(ClayMobWorkStatusManger.INSERTING_ID);
    }

    default void setNeedsBattery() {
        setDataWorkStatus(ClayMobWorkStatusManger.NEEDS_BATTERY_ID);
    }

    default void setNothingToCharge() {
        setDataWorkStatus(ClayMobWorkStatusManger.NOTHING_TO_CHARGE_ID);
    }

    default void setNoChargingPad() {
        setDataWorkStatus(ClayMobWorkStatusManger.NO_CHARGING_PAD_ID);
    }

    default void setBreaking() {
        setDataWorkStatus(ClayMobWorkStatusManger.BREAKING_ID);
    }

    default void setUnbreakableBlock() {
        setDataWorkStatus(ClayMobWorkStatusManger.UNBREAKABLE_ID);
    }

    default void setBreakingCrop() {
        setDataWorkStatus(ClayMobWorkStatusManger.BREAKING_CROP_ID);
    }

    default void setCannotBreakCrops() {
        setDataWorkStatus(ClayMobWorkStatusManger.CANT_BREAK_CROP_ID);
    }

    default void setCannotBreakBlocks() {
        setDataWorkStatus(ClayMobWorkStatusManger.CANNOT_BREAK_BLOCKS_ID);
    }

    default void setNoChest() {
        setDataWorkStatus(ClayMobWorkStatusManger.NO_CHEST_ID);
    }

    default void setRequiresSheersOrBottle() {
        setDataWorkStatus(ClayMobWorkStatusManger.BEEKEEPING_REQUIRES_TOOLS_ID);
    }

    default void setNoHive() {
        setDataWorkStatus(ClayMobWorkStatusManger.NO_HIVE_ID);
    }
}
