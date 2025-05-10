package net.bumblebee.claysoldiers.soldierpoi;

import net.bumblebee.claysoldiers.claypoifunction.ClayPoiSource;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.Nullable;

public abstract class SoldierPoiWithSource<T> {
    @Nullable
    private final SoldierPoi poi;
    private final T source;

    public SoldierPoiWithSource(@Nullable SoldierPoi poi, T source) {
        this.poi = poi;
        this.source = source;
    }
    public static SoldierPoiWithSource<ItemEntity> getPoiFromItem(ItemEntity itemEntity) {
        return new SoldierPoiWithItem(itemEntity);
    }
    @Nullable
    public SoldierPoi getPoi() {
        return poi;
    }
    public T getSource() {
        return source;
    }
    public void performEffect(AbstractClaySoldierEntity soldier) {
        if (poi != null && stillValid(soldier)) {
            poi.performEffect(soldier, createPoiSource(source));
            animateEffect(soldier);
            onUse(source, soldier);

        }
    }
    protected abstract ClayPoiSource createPoiSource(T source);
    protected boolean stillValid(AbstractClaySoldierEntity soldier) {
        return true;
    }

    public abstract void animateEffect(ClayMobEntity claySoldier);
    public boolean canPerformEffect(ClaySoldierInventoryQuery soldier) {
        if (poi == null) {
            return false;
        }
        return poi.canPerformEffect(soldier);
    }
    public abstract void startPath(PathNavigation navigation);
    public abstract void onUse(T source, AbstractClaySoldierEntity soldier);

    @Override
    public String toString() {
        return "SoldierPoiWithSource{" + poi +
                ", " + source +
                '}';
    }
}
