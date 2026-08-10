package net.bumblebee.claysoldiers.entity.common.soldier;

import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.inventory.ClaySoldierInventoryHandler;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface ClaySoldierLike {
    ClayMobEntity asClayMob();

    /**
     * Convert this soldier to given other soldier type copying all properties.
     * Including the inventory.
     * The TeamId is not copied.
     *
     * @param pEntityType the new Soldier Type
     * @param applyEffect additional effect that should be applied to the new soldier
     * @return the new soldier entity
     */
    default <T extends ClayMobEntity & ClaySoldierLike> T convertToSoldier(@NotNull EntityType<T> pEntityType, Consumer<T> applyEffect) {
        if (asClayMob().isRemoved()) {
            return null;
        } else {
            T convertTo = pEntityType.create(asClayMob().level(), EntitySpawnReason.CONVERSION);
            if (convertTo == null) {
                return null;
            } else {
                copyBasePropertiesTo(convertTo, true);
                asClayMob().level().addFreshEntity(convertTo);
                if (asClayMob().isPassenger()) {
                    Entity entity = asClayMob().getVehicle();
                    asClayMob().stopRiding();
                    convertTo.startRiding(entity, true, false);
                }
                applyEffect.accept(convertTo);


                asClayMob().discard();
                return convertTo;
            }
        }
    }

    /**
     * Copies the base properties of this soldier to the given one.
     *
     * @param toCopyTo      to copy the properties to
     * @param copyInventory whether to copy the entity
     */
    default <T extends ClayMobEntity & ClaySoldierLike> void copyBasePropertiesTo(T toCopyTo, boolean copyInventory) {
        toCopyTo.copyPosition(asClayMob());
        toCopyTo.setBaby(asClayMob().isBaby());
        toCopyTo.setNoAi(asClayMob().isNoAi());
        if (asClayMob().hasCustomName()) {
            toCopyTo.setCustomName(asClayMob().getCustomName());
            toCopyTo.setCustomNameVisible(asClayMob().isCustomNameVisible());
        }

        if (asClayMob().isPersistenceRequired()) {
            toCopyTo.setPersistenceRequired();
        }

        toCopyTo.setInvulnerable(asClayMob().isInvulnerable());
        if (copyInventory && toCopyTo instanceof ClaySoldierInventoryHandler inventoryHandler) {
            copyInventory(inventoryHandler);
        }
        toCopyTo.setSpawnedFrom(asClayMob().getSpawnedFrom(), asClayMob().shouldDropSpawnedFrom());

        if (asClayMob().isPassenger()) {
            Entity entity = asClayMob().getVehicle();
            asClayMob().stopRiding();
            toCopyTo.startRiding(entity, true, false);
        }
    }

    /**
     * Copies this inventory to given clay soldier and drops non-compatible items
     *
     * @param toCopyTo the soldier to copy the inventory to
     */
    <T extends ClaySoldierInventoryHandler> void copyInventory(T toCopyTo);

    /**
     * Reads data from the given {@code Tag} when spawned by an {@code Item} or from a {@link AdditionalSoldierData#convert conversion}.
     */
    void readItemPersistentData(ValueInput tag);

    /**
     * Called when a soldier {@link AdditionalSoldierData#convert conversion} happens.
     *
     * @param oldSoldier the Soldier to be converted
     * @param tag        additional conversion data
     * @param cause The player how should get credit for this conversion
     */
    default void onConversion(ClayMobEntity oldSoldier, ValueInput tag, @Nullable Player cause) {
    }

    void setClayTeamType(Holder.Reference<ClayMobTeam> type);
}
