package net.bumblebee.claysoldiers.team;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * This interface represents any entity that belongs to a {@code ClayMobTeam}.
 */
public interface TeamHolder extends OwnableEntity {

    /**
     * Returns the {@code ClayMobTeam} of this {@code TeamHolder}.
     * If the {@code ClayMobTeam} was an invalid, the {@link ClayMobTeamManger#DEFAULT_TYPE Default Team} is set as the new {@code ClayMobTeam} and returned.
     * @return the {@code ClayMobTeam} of this {@code TeamHolder}
     */
    @NotNull ClayMobTeam getClayTeam();

    /**
     * Returns the {@code ClayMobTeamId} of this {@code TeamHolder}.
     * @return the {@code ClayMobTeamId} of this {@code TeamHolder}
     */
    ResourceLocation getClayTeamType();

    /**
     * Sets the {@code ClayMobTeamId} to the given one, if this {@code TeamHolder} can change teams.
     * @param type the new team
     */
    default void setClayTeamType(ResourceLocation type) {}

    /**
     * @return whether this {@code TeamHolder} belongs to any team that cooperates.
     */
    default boolean hasNoTeam() {
        return !getClayTeam().isCooperative();
    }

    /**
     * Returns whether this {@code TeamHolder} should attack another {@code TeamHolder}
     * @param teamHolder the other team holder
     * @return whether this {@code TeamHolder} should attack another {@code TeamHolder}
     */
    default boolean shouldAttackTeamHolder(TeamHolder teamHolder) {
        if (hasNoTeam() || teamHolder.hasNoTeam()) {
            return true;
        }
        return !getClayTeam().equals(teamHolder.getClayTeam());
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
        return getClayTeam().equals(teamHolder.getClayTeam());
    }


    /**
     * Returns the Player which is in favor with this team.
     */
    @Nullable Player getClayTeamOwner();

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
     * This is usually called when {@code TeamHolder} is commanded to attack by his owner
     */
    default boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
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
        return getClayTeamOwner() == null || getClayTeamOwner().isSpectator() || isOrderedToSit();
    }

    /**
     * Returns whether this {@code TeamHolder} is ordered to sit.
     */
    boolean isOrderedToSit();

    void setOrderedToSit(boolean sit);

    /**
     * Returns whether this {@code TeamHolder} is in a sitting pose
     */
    boolean isInSittingPose();

    /**
     * Sets this {@code TeamHolder} in sitting pose.
     */
    void setInSittingPose(boolean pSitting);

    @Override
    @Nullable
    default UUID getOwnerUUID() {
        return getClayTeamOwnerUUID();
    }

    @Override
    EntityGetter level();

    @Override
    @Nullable
    default LivingEntity getOwner() {
        return getClayTeamOwner();
    }
}
