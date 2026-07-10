package net.bumblebee.claysoldiers.entity.common;

import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * The abstract class of a {@code ClayMob} that can be ridden.
 */
public abstract class ClayMobRideableEntity extends ClayMobEntity {
    private final Holder.Reference<ClayMobTeam> defaultTeam;

    protected ClayMobRideableEntity(EntityType<? extends ClayMobRideableEntity> entityType, Level level) {
        super(entityType, level);
        this.defaultTeam = ClayMobTeamManger.getDefault(level.registryAccess());
    }

    protected Holder.Reference<ClayMobTeam> getDefaultTeam() {
        return defaultTeam;
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
    public @NonNull ResourceKey<ClayMobTeam> getClayTeamKey() {
        if (getFirstPassenger() instanceof ClayMobEntity clayMob) {
            return clayMob.getClayTeamKey();
        }
        return getDefaultTeam().key();
    }

    @Override
    public @NotNull Holder.Reference<ClayMobTeam> getClayTeamHolder() {
        if (getFirstPassenger() instanceof ClayMobEntity clayMob) {
            return clayMob.getClayTeamHolder();
        }
        return getDefaultTeam();
    }


    @Override
    public int getTeamColor() {
        return getFirstPassenger() != null ? getFirstPassenger().getTeamColor() : super.getTeamColor();
    }

    @Override
    public boolean getOrderedCommand() {
        return super.getOrderedCommand() || (getFirstPassenger() instanceof ClayMobEntity clayMob && clayMob.getOrderedCommand()) ;
    }

    @Override
    public List<String> getInfoState() {
        List<String> info = super.getInfoState();
        info.add("CachedTeam: " + defaultTeam);
        info.add("Rider: " + (getFirstPassenger() == null ? "Null" : getFirstPassenger().getClass().getSimpleName()));
        return info;
    }

    @Override
    public boolean showInStatDisplay() {
        return false;
    }
}
