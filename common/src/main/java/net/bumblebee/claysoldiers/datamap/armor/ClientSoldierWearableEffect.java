package net.bumblebee.claysoldiers.datamap.armor;

import net.bumblebee.claysoldiers.util.ErrorHandler;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClientSoldierWearableEffect extends SoldierWearableEffect {

    private Equippable equippable;
    private boolean createdEquippable = false;
    private final boolean shouldRenderArmor;
    private List<TrimHolder> finishedArmorTrims;


    public ClientSoldierWearableEffect(@Nullable Item item, ColorHelper color, Set<SoldierArmorTrim> trims, boolean offsetColor) {
        super(item, color, trims, offsetColor);
        this.shouldRenderArmor = item != null;
    }

    public static SoldierWearableEffect create(Item item, ColorHelper color, Set<SoldierArmorTrim> trims, boolean offsetColor) {
        return new ClientSoldierWearableEffect(item, color, trims, offsetColor);
    }

    public boolean shouldRenderArmor() {
        return shouldRenderArmor;
    }

    @Nullable
    public Equippable getEquippable() {
        if (createdEquippable || equippable != null) {
            return equippable;
        }
        Item copyModel = copyModel();
        createdEquippable = true;
        if (copyModel == null) {
            return null;
        }

        equippable = copyModel.getDefaultInstance().get(DataComponents.EQUIPPABLE);

        return equippable;
    }

    @Override
    public void buildTrims(HolderLookup.Provider access) {
        if (finishedArmorTrims != null) {
            return;
        }
        finishedArmorTrims = new ArrayList<>(trims.size());

        for (SoldierArmorTrim trim : trims) {
            ArmorTrim armorTrim = trim.createTrim(access);
            if (armorTrim != null) {
                finishedArmorTrims.add(new TrimHolder(armorTrim, trim.getColor()));
            } else {
                ErrorHandler.INSTANCE.error("Failed to create an ArmorTrim for " + trim);
            }
        }
    }

    public Iterable<TrimHolder> getArmorTrims() {
        return finishedArmorTrims == null ? List.of() : finishedArmorTrims;
    }

    @Override
    public String toString() {
        return "%s{%s, %s}".formatted(this.getClass().getSimpleName(),
                copyModel(),
                "Finished Trims(" + finishedArmorTrims.size() + ")"
        );
    }

    /*public Iterable<TrimHolder> getArmorTrims(RegistryAccess access) {
        if (finishedArmorTrims != null) {
            return finishedArmorTrims;
        }
        finishedArmorTrims = new ArrayList<>(trims.size());

        for (SoldierArmorTrim trim : trims) {
            ArmorTrim armorTrim = trim.createTrim(access);
            if (armorTrim != null) {
                finishedArmorTrims.add(new TrimHolder(armorTrim, trim.getColor()));

            }
        }
        return finishedArmorTrims;
    }*/

    public record TrimHolder(ArmorTrim trim, ColorHelper color) {}

}
