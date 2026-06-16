package net.bumblebee.claysoldiers.advancements.entity;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.soldierproperties.SoldierProperty;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record PropertyClaySoldierSubPredicate(SoldierPropertyMap map) implements EntitySubPredicate {
    public static final MapCodec<PropertyClaySoldierSubPredicate> CODEC = SoldierPropertyMap.CODEC
            .xmap(PropertyClaySoldierSubPredicate::new, PropertyClaySoldierSubPredicate::map)
    .fieldOf("clay_soldier_property");

    public static PropertyClaySoldierSubPredicate hasProperty(SoldierProperty<?>... propertyType) {
        return new PropertyClaySoldierSubPredicate(SoldierPropertyMap.of(propertyType));
    }

    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
        if (entity instanceof ClaySoldierInventoryQuery inventoryQuery) {
            var soldierProperties = inventoryQuery.allProperties();
            for (var prop : map) {
                var optProp = soldierProperties.getProperty(prop.type());
                if (optProp == null || optProp.value() != prop.value()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
