package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.config.ClientConfig;
import net.bumblebee.claysoldiers.config.CommonConfig;
import net.bumblebee.claysoldiers.config.ServerConfig;
import net.bumblebee.claysoldiers.networking.ConfigSyncPayload;
import net.bumblebee.claysoldiers.platform.services.IConfig;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class FabricConfig implements IConfig, CommonConfig, ServerConfig {
    private static final ClientConfig CLIENT_CONFIG = new ClientConfig() {};

    public static int hamsterWheelSpeed = 3;
    public static boolean canModifyMenu = false;
    public static boolean shearBladeRecipeEnabled;
    public static int soldierTransferRate = 15;
    public static int chargingPadPlayerRate = 20;

    // Server
    public static boolean soldierDropInventory = true;
    public static float soldierDropSelf = 0.5f;
    public static boolean chipRequiresLoyalty = true;

    public static void init(int hamsterWheelSpeed, boolean canModifyMenu, boolean shearBladeRecipeEnabled, float soldierDropSelf, boolean soldierDropInventory, boolean chipRequiresLoyalty, int soldierTransferRate, int chargingPadPlayerRate) {
        FabricConfig.hamsterWheelSpeed = hamsterWheelSpeed;
        FabricConfig.canModifyMenu = canModifyMenu;
        FabricConfig.shearBladeRecipeEnabled = shearBladeRecipeEnabled;
        FabricConfig.soldierDropSelf = soldierDropSelf;
        FabricConfig.soldierDropInventory = soldierDropInventory;
        FabricConfig.chipRequiresLoyalty = chipRequiresLoyalty;
        FabricConfig.soldierTransferRate = soldierTransferRate;
        FabricConfig.chargingPadPlayerRate = chargingPadPlayerRate;
    }

    public static void initSynced(int hamsterWheelSpeed, boolean shearBladeRecipeEnabled, int soldierTransferRate, int chargingPadPlayerRate) {
        FabricConfig.hamsterWheelSpeed = hamsterWheelSpeed;
        FabricConfig.setShearBladeRecipeEnabled(shearBladeRecipeEnabled);
        FabricConfig.soldierTransferRate = soldierTransferRate;
        FabricConfig.chargingPadPlayerRate = chargingPadPlayerRate;
    }

    public static CustomPacketPayload createSyncPayload() {
        return new ConfigSyncPayload(
                hamsterWheelSpeed,
                shearBladeRecipeEnabled,
                soldierTransferRate,
                chargingPadPlayerRate
        );
    }

    @Override
    public boolean soldierDropInventory() {
        return soldierDropInventory;
    }

    @Override
    public float soldierDropSelf() {
        return soldierDropSelf;
    }

    @Override
    public boolean chipRequiresLoyalty() {
        return chipRequiresLoyalty;
    }

    @Override
    public boolean canModifyClayMobMenu() {
        return canModifyMenu;
    }

    @Override
    public int baseSoldierEnergyTransferRate() {
        return soldierTransferRate;
    }

    @Override
    public int chargingPadRatePlayerInventory() {
        return chargingPadPlayerRate;
    }

    @Override
    public int getHamsterWheelSpeed() {
        return hamsterWheelSpeed;
    }

    @Override
    public boolean shearBladeRecipeEnabled() {
        return shearBladeRecipeEnabled;
    }

    public static void setShearBladeRecipeEnabled(boolean shearBladeRecipeEnabled) {
        FabricConfig.shearBladeRecipeEnabled = shearBladeRecipeEnabled;
    }

    public static void logConfig(String context, boolean client) {
        ClaySoldiersCommon.ERROR_HANDLER.debug("Config (%s):".formatted(context));
        ClaySoldiersCommon.CONFIG.getInfo(ClaySoldiersCommon.ERROR_HANDLER::debug, client);
    }



    @Override
    public ClientConfig getClientConfig() {
        return CLIENT_CONFIG;
    }

    @Override
    public ServerConfig getServerConfig() {
        return this;
    }

    @Override
    public CommonConfig getCommonConfig() {
        return this;
    }
}
