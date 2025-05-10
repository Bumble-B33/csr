package net.bumblebee.claysoldiers.menu;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractClayMobMenu<T extends ClayMobEntity> extends AbstractContainerMenu {
    protected int inventoryYOffset;

    protected AbstractClayMobMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory inv) {
        super(pMenuType, pContainerId);
    }

    /**
     * Initializes the Player Inventory with all it slots.
     * @param inventory inventory of the team
     */
    protected void initPlayerInventory(Inventory inventory) {
        addPlayerHotbar(inventory);
        addPlayerInventory(inventory);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, inventoryYOffset + 84 + i * 18));
            }
        }
    }
    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142 + inventoryYOffset));
        }
    }

    /**
     * Returns the {@code Source} of this menu.
     * @return the {@code Source} of this menu
     */
    protected abstract Optional<T> getSource();

    @Override
    public boolean stillValid(Player pPlayer) {
        if (getSource().isEmpty()) {
            return false;
        }
        return getSource().get().isAlive() && getSource().get().distanceTo(pPlayer) < 8.0F;
    }

    /**
     * Performs an {@code Action}  if the {@code Source} of this menu is present.
     * @param action the action to perform
     */
    public void forSourceIfPresent(Consumer<T> action) {
        getSource().ifPresent(action);
    }
}
