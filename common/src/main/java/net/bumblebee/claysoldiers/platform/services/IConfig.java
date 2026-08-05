package net.bumblebee.claysoldiers.platform.services;

import net.bumblebee.claysoldiers.config.ClientConfig;
import net.bumblebee.claysoldiers.config.CommonConfig;
import net.bumblebee.claysoldiers.config.ServerConfig;

import java.util.function.Consumer;

public interface IConfig {

    String SHEAR_BLADE_RECIPE_KEY = "shearBladeRecipeEnabled";
    String HAMSTER_WHEEL_SPEED_KEY = "hamsterWheelSpeed";
    String BASE_ENERGY_CAPACITY_BATTERIES_KEY = "baseEnergyCapacityBatteries";
    String SOLDIER_MODIFY_MENU_KEY = "claySoldierMenuModify";

    String SOLDIER_DROP_INVENTORY_KEY = "soldierDropInventory";
    String SOLDIER_DROP_SELF_KEY = "soldierDropSelf";
    String CHIP_REQUIRES_LOYALTY_KEY = "chipRequiresLoyalty";
    String BASE_SOLDIER_ENERGY_TRANSFER_RATE_KEY = "baseSoldierEnergyTransferRate";
    String CHARGING_PAD_PLAYER_RATE_KEY = "chargingPadPlayerRate";


    String STATO_METER_SHOW_INFO_KEY = "statoMeterShowInfo";
    String STATO_METER_SHOW_COUNT_KEY = "statoMeterShowCounts";
    String ENERGY_COLOR_KEY = "energyColor";


    ClientConfig getClientConfig();

    ServerConfig getServerConfig();

    CommonConfig getCommonConfig();

    default void getInfo(Consumer<String> appendLine, boolean client) {
        if (client) {
            appendLine.accept(" StatItem: " + (getClientConfig().statItemShowStats() ? "Stats " : "") + (getClientConfig().statItemShowCount() ? "Count" : ""));
            appendLine.accept(" EnergyColor: #" + Integer.toHexString(getClientConfig().getEnergyColor()).toUpperCase());
        } else {
            appendLine.accept(" DropInventory: " + getServerConfig().soldierDropInventory());
            appendLine.accept(" DropSelf: " + ((int) (getServerConfig().soldierDropSelf() * 100) + "%"));
            appendLine.accept(" Chip Requires Loyalty: " + getServerConfig().chipRequiresLoyalty());
        }
        CommonConfig config = getCommonConfig();

        appendLine.accept(" ModifyMenu: " + config.canModifyClayMobMenu());
        appendLine.accept(" HamsterWheelSpeed: " + config.getHamsterWheelSpeed());
        appendLine.accept(" SoldierEnergyTransferRate: " + config.baseSoldierEnergyTransferRate());
        appendLine.accept(" ChargingPadPlayerRate: " + config.chargingPadRatePlayerInventory());
        appendLine.accept(" ShearBladeRecipe: " + (config.shearBladeRecipeEnabled() ? "Enabled" : "Disabled"));

    }
}
