package net.bumblebee.claysoldiers.team;

import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * This interface represents any entity that belongs to a {@code ClayMobTeam}.
 */
public interface TeamHolder extends OwnableEntity {
    @NotNull
    Holder.Reference<ClayMobTeam> getClayTeamHolder();

    @NotNull
    ResourceKey<ClayMobTeam> getClayTeamKey();

    default @NotNull ClayMobTeam getClayTeam() {
        return getClayTeamHolder().value();
    }

    default void setClayTeamType(ResourceKey<ClayMobTeam> type) {
    }

    default void setClayTeamType(Holder.Reference<ClayMobTeam> type) {
    }

    /**
     * @return whether this {@code TeamHolder} belongs to any team that cooperates.
     */
    default boolean hasNoTeam() {
        return !getClayTeam().isCooperative();
    }

    /**
     * Returns whether this {@code TeamHolder} should attack another {@code TeamHolder}
     * @param teamHolder the other team holder to attack
     * @return whether this {@code TeamHolder} should attack another {@code TeamHolder}
     */
    default boolean shouldAttackTeamHolder(TeamHolder teamHolder) {
        if (hasNoTeam() || teamHolder.hasNoTeam()) {
            return true;
        }
        return !getClayTeamHolder().is(teamHolder.getClayTeamHolder());
    }

    /**
     * Checks whether this entity is on the same team as the given one
     * @param other the other entity to check on
     * @return whether this entity is on the same as the given one
     */
    default boolean sameTeamAs(Entity other) {
        if (!(other instanceof TeamHolder teamHolder)) {
            return false;
        }
        if (hasNoTeam() || teamHolder.hasNoTeam()) {
            return false;
        }
        return getClayTeamHolder().is(teamHolder.getClayTeamHolder());
    }


    /**
     * Returns the Player which is in favor with this team.
     */
    default Player getClayTeamOwner() {
        if (getClayTeamOwnerUUID() == null) {
            return null;
        }
        return level().getPlayerByUUID(getClayTeamOwnerUUID());
    }

    default boolean hasClayTeamOwner() {
        return getClayTeamOwnerUUID() != null;
    }

    @Nullable UUID getClayTeamOwnerUUID();

    /**
     * Returns whether this {@code TeamHolder} is in favor with give {@code Player}.
     */
    default boolean isOwnedBy(Player player) {
        return player.getUUID().equals(this.getClayTeamOwnerUUID());
    }

    /**
     * Returns whether this {@code TeamHolder} wants to attack the give target.
     * This is the case when they have not the same by the same owner
     */
    default boolean wantsToAttackCommanded(LivingEntity target, LivingEntity owner) {
        if (target instanceof ClayMobEntity clayMobEntity) {
            return !owner.equals(clayMobEntity.getClayTeamOwner());
        }
        return !(target instanceof OwnableEntity ownableEntity) || !owner.equals(ownableEntity.getOwner());
    }

    /**
     * Returns whether the {@code Player} claimed the loyalty of this team successfully.
     */
    boolean tryClaimingTeam(Player player);

    /**
     * Returns whether this {@code TeamHolder} can move to its owner.
     */
    default boolean unableToMoveToOwner() {
        return getClayTeamOwner() == null || getClayTeamOwner().isSpectator() || getOrderedCommand();
    }

    /**
     * Returns whether this {@code TeamHolder} is ordered to sit.
     */
    boolean getOrderedCommand();

    /**
     * Returns whether this {@code TeamHolder} is in a sitting pose
     */
    boolean isInSittingPose();

    /**
     * Sets this {@code TeamHolder} in sitting pose.
     */
    void setInSittingPose(boolean pSitting);

    @Override
    default @Nullable EntityReference<LivingEntity> getOwnerReference() {
        var uuid = getClayTeamOwnerUUID();
        return uuid == null ? null : EntityReference.of(uuid);
    }

    @Override
    Level level();

    @Override
    @Nullable
    default LivingEntity getOwner() {
        return getClayTeamOwner();
    }
}
