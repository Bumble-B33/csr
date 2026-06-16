package net.bumblebee.claysoldiers.platform;

import net.bumblebee.claysoldiers.platform.services.ICommonHooks;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class FabricCommonHooks implements ICommonHooks {
    @Override
    public OptionalInt openMenu(Player serverPlayer, MenuProvider menuProvider, int extraData) {
        return serverPlayer.openMenu(new ExtendedMenuProvider<Integer>() {
            @Override
            public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
                return menuProvider.createMenu(i, inventory, player);
            }

            @Override
            public Component getDisplayName() {
                return menuProvider.getDisplayName();
            }

            @Override
            public Integer getScreenOpeningData(ServerPlayer player) {
                return extraData;
            }
        });
    }
}
