package net.bumblebee.claysoldiers.menu.horse;

import net.bumblebee.claysoldiers.entity.horse.AbstractClayHorse;
import net.bumblebee.claysoldiers.init.ModMenuTypes;
import net.bumblebee.claysoldiers.menu.AbstractClayMobMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ClayHorseMenu extends AbstractClayMobMenu<AbstractClayHorse> {
    @Nullable
    private final AbstractClayHorse horse;

    public ClayHorseMenu(int pContainerId, Inventory inv, int extraData) {
        this(pContainerId, inv, inv.player.level().getEntity(extraData) instanceof AbstractClayHorse clayHorse ? clayHorse : null);
    }
    public ClayHorseMenu(int pContainerId, Inventory inv, @Nullable AbstractClayHorse clayHorse) {
        super(ModMenuTypes.CLAY_HORSE_MENU.get(), pContainerId, inv);
        this.horse = clayHorse;
        if (horse != null) {
            this.addSlot(ClayHorseSlot.createArmorSlot(horse, 8, 36));
        }
        initPlayerInventory(inv);
    }

    @Override
    protected Optional<AbstractClayHorse> getSource() {
        return Optional.ofNullable(horse);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }
}
