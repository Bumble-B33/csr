package net.bumblebee.claysoldiers.entity;

import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The abstract class of a {@code ClayMob} that can be ridden.
 */
public abstract class ClayMobRideableEntity extends ClayMobEntity {
    protected ClayMobRideableEntity(EntityType<? extends ClayMobRideableEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof AbstractClaySoldierEntity) {
            return (AbstractClaySoldierEntity) entity;
        }
        return super.getControllingPassenger();
    }

    /**
     * Returns the percent of the damage the rider shares with this entity.
     */
    public float shareDamagePercent() {
        return 0.25f;
    }

    @Override
    public ResourceLocation getClayTeamType() {
        if (getFirstPassenger() instanceof ClayMobEntity clayMob) {
            return clayMob.getClayTeamType();
        }
        return ClayMobTeamManger.NO_TEAM_TYPE;
    }

    @Override
    public int getTeamColor() {
        return getFirstPassenger() != null ? getFirstPassenger().getTeamColor() : super.getTeamColor();
    }

    @Override
    public boolean isOrderedToSit() {
        return super.isOrderedToSit() || (getFirstPassenger() instanceof ClayMobEntity clayMob && clayMob.isOrderedToSit()) ;
    }

    @Override
    public List<String> getInfoState() {
        List<String> info = super.getInfoState();
        info.add("Rider: " + (getFirstPassenger() == null ? "Null" : getFirstPassenger().getClass().getSimpleName()));
        return info;
    }
}
