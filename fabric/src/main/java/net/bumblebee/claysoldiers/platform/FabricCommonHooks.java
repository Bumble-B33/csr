package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.ClaySoldierFabric;
import net.bumblebee.claysoldiers.platform.services.ICommonHooks;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class FabricCommonHooks implements ICommonHooks {
    private static boolean blueprintEnabled = false;

    @Override
    public boolean isBlueprintEnabled(FeatureFlagSet set) {
        return blueprintEnabled;
    }

    @Override
    public OptionalInt openMenu(Player serverPlayer, MenuProvider menuProvider, int extraData) {
        return serverPlayer.openMenu(new ExtendedScreenHandlerFactory<>() {
            @Override
            public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
                return menuProvider.createMenu(i, inventory, player);
            }

            @Override
            public Component getDisplayName() {
                return menuProvider.getDisplayName();
            }

            @Override
            public Object getScreenOpeningData(ServerPlayer player) {
                return extraData;
            }
        });
    }

    public static void setBlueprintEnabled(boolean blueprintEnabled) {
        FabricCommonHooks.blueprintEnabled = blueprintEnabled;
    }
    public static boolean isBlueprintEnabled() {
        return FabricCommonHooks.blueprintEnabled;
    }

    @Override
    public long getHamsterWheelSpeed() {
        return ClaySoldierFabric.hamsterWheelSpeed;
    }
}
