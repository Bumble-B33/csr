package net.bumblebee.claysoldiers.soldierproperties.customproperties.revive;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;

public enum ReviveResult {
    SUCCESS(false),
    FAIL(true);

    private final boolean dropInventory;

    ReviveResult(boolean dropInventory) {
        this.dropInventory = dropInventory;
    }
    public boolean dropInventory() {
        return dropInventory;
    }

    public boolean success() {
        return this != FAIL;
    }

    public static ReviveResult zombieOrFail(ClayMobEntity entity) {
        return entity != null ? SUCCESS : FAIL;
    }
    public static ReviveResult soldierOrFail(ClayMobEntity entity) {
        return entity != null ? SUCCESS : FAIL;
    }
    public static ReviveResult wraithOrFail(ClayMobEntity entity) {
        return entity != null ? SUCCESS : FAIL;
    }
}
