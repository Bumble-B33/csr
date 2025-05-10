package net.bumblebee.claysoldiers.entity;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public final class ClaySoldierRideableMap {
    private static final Map<EntityType<?>, BiPredicate<? extends Entity, AbstractClaySoldierEntity>> MAP = new HashMap<>();
    private static final Map<EntityType<?>, BiConsumer<? extends Entity, AbstractClaySoldierEntity>> ON_RIDE_MAP = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Entity> boolean test(T entity, AbstractClaySoldierEntity claySoldierEntity) {
        var vehicleProperties = ClaySoldiersCommon.DATA_MAP.getVehicleProperties(entity.getType());
        if (vehicleProperties != null && !vehicleProperties.predicate().test(claySoldierEntity)) {
            return false;
        }

        var predicate = (BiPredicate<T, AbstractClaySoldierEntity>) MAP.get(entity.getType());
        return predicate != null && predicate.test(entity, claySoldierEntity);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> void onRide(T entity, AbstractClaySoldierEntity claySoldierEntity) {
        var consumer = (BiConsumer<T, AbstractClaySoldierEntity>) ON_RIDE_MAP.get(entity.getType());
        if (consumer != null) {
            consumer.accept(entity, claySoldierEntity);
        }
    }

    private static <T extends Entity> void setPredicate(EntityType<T> entityType, BiPredicate<T, AbstractClaySoldierEntity> predicate) {
        MAP.put(entityType, predicate);
    }
    private static <T extends Entity> void setPredicate(EntityType<T> entityType, BiPredicate<T, AbstractClaySoldierEntity> predicate, BiConsumer<T, AbstractClaySoldierEntity> onRide) {
        setPredicate(entityType, predicate);
        ON_RIDE_MAP.put(entityType, onRide);
    }

    static {
        setPredicate(EntityType.RABBIT, (rabbit, claySoldier) -> true);
        setPredicate(EntityType.ENDERMITE, (endermite, claySoldier) -> true, ((endermite, soldier) -> endermite.setPersistenceRequired()));

        setPredicate(ModEntityTypes.CLAY_HORSE_ENTITY.get(), (horse, soldier) -> true);
        setPredicate(ModEntityTypes.CLAY_PEGASUS_ENTITY.get(), (horse, soldier) -> true);
        setPredicate(ModEntityTypes.CLAY_SOLDIER_ENTITY.get(), (soldierToRide, soldier) -> {
            if (!soldierToRide.getAttackType().rideable() || !soldier.sameTeamAs(soldierToRide)) {
                return false;
            }
            return (soldierToRide.getSoldierSize() >= soldier.getSoldierSize() + 1 && soldier.canPerformRangeAttack());
        });
        setPredicate(EntityType.TURTLE, (turtle, claySoldier) -> turtle.isBaby());
        setPredicate(EntityType.SLIME, (slime, soldier) -> slime.getSize() == 1 && soldier.canPerformRangeAttack());
    }
}
