package net.bumblebee.claysoldiers.block.soldiercontainer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.entity.client.ClientClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.client.FakeClaySoldierAccess;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.inventory.ClaySoldierInventory;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClayMobAccess;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.loyalty.TeamLoyaltyManger;
import net.bumblebee.claysoldiers.util.codec.EntityTypesCodecs;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class OccupantSoldierData implements Comparable<OccupantSoldierData> {
    private static final Codec<ResourceKey<ClayMobTeam>> CLAY_TEAM_KEY_CODEC = ClayMobTeam.KEY_CODEC;
    private static final List<String> IGNORED_TAGS = Arrays.asList(
            Entity.TAG_AIR,
            LivingEntity.TAG_BRAIN,
            Mob.TAG_CAN_PICK_UP_LOOT,
            LivingEntity.TAG_DEATH_TIME,
            Entity.TAG_FALL_DISTANCE,
            LivingEntity.TAG_FALL_FLYING,
            Entity.TAG_FIRE,
            Mob.TAG_DROP_CHANCES,
            LivingEntity.TAG_HEALTH,
            LivingEntity.TAG_HURT_BY_TIMESTAMP,
            LivingEntity.TAG_HURT_TIME,
            Mob.TAG_LEFT_HANDED,
            Entity.TAG_MOTION,
            Entity.TAG_ON_GROUND,
            Entity.TAG_PORTAL_COOLDOWN,
            Entity.TAG_POS,
            Entity.TAG_ROTATION,
            LivingEntity.TAG_SLEEPING_POS,
            Entity.TAG_ID,
            Entity.TAG_PASSENGERS,
            Leashable.LEASH_TAG,
            ClayMobEntity.SITTING_TAG,
            AbstractClaySoldierEntity.FUSE_TAG,
            AbstractClaySoldierEntity.IGNITED_TAG,
            AbstractClaySoldierEntity.EXPLOSION_RADIUS_TAG,
            AbstractClaySoldierEntity.POI_POS_TAG,
            AbstractClaySoldierEntity.CARRIED_ITEM_TAG,
            ClayMobTeam.TEAM_ID_TAG,

            ClaySoldierInventory.INVENTORY_TAG,
            AbstractClaySoldierEntity.OFFSET_COLOR_TAG,
            ClayMobEntity.WAXED_TAG
    );

    private static final String ENTITY_TYPE_TAG = "type";
    private static final String DATA_TAG = "SoldierData";
    private static final String SOLDIER_SPEED_TAG = "SoldierSpeed";
    private static final String SOLDIER_ENTER_TAG = "SoldierEnterTime";
    private static final float DEFAULT_SPEED = AbstractClaySoldierEntity.BASE_MOVEMENT_SPEED * ClaySoldiersCommon.CONFIG.getCommonConfig().getHamsterWheelSpeed();
    private static final CompoundTag EMPTY_DATA = new CompoundTag();

    public static final Codec<Compressed> CODEC = RecordCodecBuilder.create(in -> in.group(
            EntityTypesCodecs.CLAY_SOLDIER_CODEC.fieldOf(ENTITY_TYPE_TAG).forGetter(d -> d.type),
            CompoundTag.CODEC.optionalFieldOf(DATA_TAG, EMPTY_DATA).forGetter(d -> d.data),
            Data.CODEC.fieldOf("explicitData").forGetter(d -> d.explicitData),
            CLAY_TEAM_KEY_CODEC.fieldOf(ClayMobTeam.TEAM_ID_TAG).forGetter(d -> d.teamReference),
            Codec.FLOAT.optionalFieldOf(SOLDIER_SPEED_TAG, DEFAULT_SPEED).forGetter(d -> d.speed),
            Codec.LONG.optionalFieldOf(SOLDIER_ENTER_TAG, 0L).forGetter(d -> d.enterTime),
            Codec.BOOL.fieldOf("client").forGetter(s -> s.client)
    ).apply(in, (Compressed::new)));

    private final EntityType<? extends AbstractClaySoldierEntity> type;
    private final CompoundTag serverData;
    private final Data explicitData;
    @NotNull
    private final Holder.Reference<ClayMobTeam> teamReference;
    private final float speed;
    private final float roundedSpeed;
    private final long enterTime;

    @Nullable
    private FakeClaySoldierAccess clientSoldier;

    private OccupantSoldierData(EntityType<? extends AbstractClaySoldierEntity> type, CompoundTag data, Data explicitData, Holder.Reference<ClayMobTeam> teamId, float speed, long enterTime) {
        this.type = type;
        this.serverData = data;
        this.explicitData = explicitData;
        this.speed = speed;
        this.roundedSpeed = Math.round(speed * 3.4 * 10) / 10f;
        this.enterTime = enterTime;
        this.teamReference = teamId;
    }

    public ResourceKey<ClayMobTeam> getTeamKey() {
        return teamReference.key();
    }

    public boolean canBeKilledBy(ServerLevel level, Player player) {
        var owner = TeamLoyaltyManger.getTeamPlayerData(level).getPlayerForTeam(getTeamKey());
        return owner == null || owner.is(player);
    }

    public Component getTypeDescription() {
        return type.getDescription();
    }


    public static OccupantSoldierData of(AbstractClaySoldierEntity soldier) {
        TagValueOutput output = TagValueOutput.createWithContext(ClaySoldiersCommon.PROBLEM_REPORTER, soldier.registryAccess());
        soldier.save(output);
        IGNORED_TAGS.forEach(output::discard);
        return new OccupantSoldierData(soldier.getType(), output.buildResult(), Data.of(soldier), soldier.getClayTeamHolder(), (float) soldier.getAttribute(Attributes.MOVEMENT_SPEED).getValue(), soldier.level().getGameTime());
    }

    /**
     * @return the speed of the ClaySoldier.
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * @return the speed of the ClaySoldier adjusted to {@code 1}
     */
    public float getAdjustedSpeed() {
        return roundedSpeed;
    }

    /**
     * Recreates a ClaySoldier form this SoldierBlockData.
     */
    public AbstractClaySoldierEntity createSoldier(ServerLevel level) {
        CompoundTag newEntityData = this.serverData.copy();
        IGNORED_TAGS.forEach(newEntityData::remove);

        AbstractClaySoldierEntity entity = type.create(level, EntitySpawnReason.EVENT);

        if (entity != null) {
            entity.load(TagValueInput.create(ClaySoldiersCommon.PROBLEM_REPORTER, level.registryAccess(), newEntityData));
            serverData.read(Entity.TAG_UUID, UUIDUtil.CODEC).ifPresentOrElse(entity::setUUID, () -> ClaySoldiersCommon.ERROR_HANDLER.debug("Loaded a ClaySoldier without its UUID"));

            entity.getActiveEffectsMap().clear();
            entity.getActiveEffectsMap().putAll(loadEffects(serverData, (int) (level.getGameTime() - enterTime), level.registryAccess()));

            entity.setClayTeamType(teamReference);
            entity.setCanPickUpLoot(true);

            entity.setWaxed(explicitData.waxed());
            entity.getInventory().copyFrom(explicitData.inventory());
            entity.setOffsetColor(explicitData.offsetColor());
            return entity;
        } else {
            return null;
        }
    }

    private static Map<Holder<MobEffect>, MobEffectInstance> loadEffects(CompoundTag tag, int elapsedTime, RegistryAccess registryAccess) {

        RegistryOps<Tag> registryops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
        List<MobEffectInstance> list = tag.read("active_effects", MobEffectInstance.CODEC.listOf(), registryops).orElse(List.of());

        Map<Holder<MobEffect>, MobEffectInstance> map = new HashMap<>();

        for (var oldEff : list) {
            if (oldEff != null) {
                if (!oldEff.endsWithin(elapsedTime)) {
                    var newInstance = new MobEffectInstance(oldEff.getEffect(), Math.max(1, oldEff.getDuration() - elapsedTime), oldEff.getAmplifier(), oldEff.isAmbient(), oldEff.isVisible(), oldEff.showIcon());
                    map.put(oldEff.getEffect(), newInstance);
                }
            }
        }

        return map;
    }

    public @Nullable FakeClaySoldierAccess getClientSoldier() {
        return clientSoldier;
    }

    private void setUpClient(BlockPos pos, WalkAnimationState state) {
        clientSoldier = ClientClaySoldierEntity.create(type, explicitData, pos, state, teamReference);
        clientSoldier.setUpCape();
    }

    public void dropItems(ServerLevel level, double x, double y, double z) {
        ValueInput input = TagValueInput.create(ClaySoldiersCommon.PROBLEM_REPORTER, level.registryAccess(), serverData);
        ClaySoldierInventory inventory = new ClaySoldierInventory();
        inventory.load(input);
        inventory.dropInventory(level, (slot, stack) -> spawnItemInWorld(level, stack, x, y, z));

        if (input.getBooleanOr(ClayMobEntity.DROP_SPAWNED_FROM_TAG, false)) {
            ClayMobEntity.getSpawnedFromFromTag(input).ifPresent(s -> {
                ClayMobEntity.dropSpawnedFrom(level, s, (stack) -> spawnItemInWorld(level, stack, x, y, z), false, false);
            });
        }
    }

    private static void spawnItemInWorld(ServerLevel level, ItemStack stack, double x, double y, double z) {
        ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    @Override
    public String toString() {
        return "SoldierData(%s(%s), Speed: (%s - %s)".formatted(type.getDescription().getString(), getTeamKey().identifier(), speed, roundedSpeed);
    }

    public Compressed compress(boolean client) {
        return new Compressed(
                type, client ? EMPTY_DATA : serverData, explicitData, teamReference.key(), speed, enterTime, client
        );
    }

    @Override
    public int compareTo(@NonNull OccupantSoldierData o) {
        return Long.compare(o.enterTime, enterTime);
    }

    public static class Compressed {
        private final EntityType<? extends AbstractClaySoldierEntity> type;
        private final CompoundTag data;
        private final Data explicitData;
        private final ResourceKey<ClayMobTeam> teamReference;
        private final float speed;
        private final long enterTime;
        private final boolean client;

        public Compressed(EntityType<? extends AbstractClaySoldierEntity> type, CompoundTag data, Data explicitData, ResourceKey<ClayMobTeam> team, float speed, long enterTime, boolean client) {
            this.type = type;
            this.data = data;
            this.explicitData = explicitData;
            this.teamReference = team;
            this.speed = speed;
            this.enterTime = enterTime;
            this.client = client;
        }

        public Optional<OccupantSoldierData> build(BlockPos pos, WalkAnimationState state, HolderLookup.Provider provider) {
            var team = provider.get(teamReference);
            if (team.isEmpty()) {
                return Optional.empty();
            }

            OccupantSoldierData res = new OccupantSoldierData(
                    type, data, explicitData, team.orElseThrow(), speed, enterTime
            );

            if (client) {
                res.setUpClient(pos, state);
            }

            return Optional.of(res);
        }
    }

    public record Data(float scale, boolean waxed, ClaySoldierInventory inventory, int skinVariantId, ColorHelper offsetColor, Optional<ClaySoldierChip<?>> chip) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(in -> in.group(
                Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(Data::scale),
                Codec.BOOL.optionalFieldOf("waxed", false).forGetter(Data::waxed),
                ClaySoldierInventory.DIRECT_CODEC.fieldOf("inventory").forGetter(Data::inventory),
                Codec.INT.optionalFieldOf("skinVariant", 0).forGetter(Data::skinVariantId),
                ColorHelper.CODEC.optionalFieldOf("color", ColorHelper.EMPTY).forGetter(Data::offsetColor),
                ClaySoldierChip.CODEC.optionalFieldOf("chip").forGetter(Data::chip)
        ).apply(in, Data::new));

        public static Data of(AbstractClaySoldierEntity soldier) {
            ClaySoldierChip<?> chip = soldier instanceof ProgrammableClayMobAccess access ? access.getInstalledChip() : null;
            return new Data(soldier.getScale(), soldier.isWaxed(), soldier.getInventory(), soldier.getSkinVariant(), soldier.getOffsetColor(), Optional.ofNullable(chip));
        }
    }
}
