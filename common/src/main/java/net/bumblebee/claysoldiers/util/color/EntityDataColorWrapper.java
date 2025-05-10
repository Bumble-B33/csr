package net.bumblebee.claysoldiers.util.color;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;

import java.util.function.Supplier;

public class EntityDataColorWrapper {
    private final EntityDataAccessor<Integer> intAccessor;
    private final EntityDataAccessor<Boolean> boolAccessor;
    private final Supplier<SynchedEntityData> entityData;

    public EntityDataColorWrapper(EntityDataAccessor<Integer> intAccessor, EntityDataAccessor<Boolean> boolAccessor, Supplier<SynchedEntityData> entityData) {
        this.intAccessor = intAccessor;
        this.boolAccessor = boolAccessor;
        this.entityData = entityData;
    }

    public static void define(SynchedEntityData.Builder builder, EntityDataAccessor<Integer> intAccessor, EntityDataAccessor<Boolean> boolAccessor) {
        builder.define(intAccessor, ColorHelper.EMPTY.getColorStatic());
        builder.define(boolAccessor, ColorHelper.EMPTY.isJeb());
    }
    public ColorHelper getColor() {
        return new ColorHelper(entityData.get().get(intAccessor), entityData.get().get(boolAccessor));
    }
    public void setColor(ColorHelper color) {
        entityData.get().set(intAccessor, color.getColorStatic());
        entityData.get().set(boolAccessor, color.isJeb());
    }


}
