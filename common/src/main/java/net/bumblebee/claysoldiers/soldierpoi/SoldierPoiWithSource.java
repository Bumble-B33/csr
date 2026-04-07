package net.bumblebee.claysoldiers.soldierpoi;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiSource;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModCriterions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
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
            var createdSource = createPoiSource(source);
            if (createdSource.getOwner() instanceof ServerPlayer serverPlayer) {
                ModCriterions.SOLDIER_POI_USE_TRIGGER.get().trigger(serverPlayer, getType());
            }
            poi.performEffect(soldier, createdSource);
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

    protected abstract Type getType();

    public enum Type implements StringRepresentable {
        ITEM("item"),
        BLOCK("block");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final String serializedName;

        Type(String serializedName) {
            this.serializedName = serializedName;
        }


        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
