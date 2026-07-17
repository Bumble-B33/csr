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
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.revive.ReviveType;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.OwnerQuery;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class OccupantSoldierData implements Comparable<OccupantSoldierData> {
    private static final Codec<ResourceKey<ClayMobTeam>> CLAY_TEAM_KEY_CODEC = ClayMobTeam.KEY_CODEC;
    private static final List<String> IGNORED_TAGS = Arrays.asList(
            Entity.TAG_FALL_DISTANCE,
            Entity.TAG_FIRE,
            Entity.TAG_AIR,
            Entity.TAG_MOTION,
            Entity.TAG_ON_GROUND,
            Entity.TAG_PORTAL_COOLDOWN,
            Entity.TAG_POS,
            Entity.TAG_ROTATION,
            Entity.TAG_ID,
            Entity.TAG_PASSENGERS,
            LivingEntity.TAG_BRAIN,
            LivingEntity.TAG_FALL_FLYING,
            LivingEntity.TAG_DEATH_TIME,
            LivingEntity.TAG_HEALTH,
            LivingEntity.TAG_HURT_BY_TIMESTAMP,
            LivingEntity.TAG_HURT_TIME,
            LivingEntity.TAG_SLEEPING_POS,
            Mob.TAG_CAN_PICK_UP_LOOT,
            Mob.TAG_DROP_CHANCES,
            Mob.TAG_LEFT_HANDED,
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
            ClayMobEntity.WAXED_TAG,
            ClayMobEntity.DROP_SPAWNED_FROM_TAG,
            ClayMobEntity.SPAWNED_FROM_TAG
    );

    private static final String ENTITY_TYPE_TAG = "type";
    private static final String DATA_TAG = "SoldierData";
    private static final String SOLDIER_SPEED_TAG = "SoldierSpeed";
    private static final String SOLDIER_ENTER_TAG = "SoldierEnterTime";
    private static final float DEFAULT_SPEED = AbstractClaySoldierEntity.BASE_MOVEMENT_SPEED;
    private static final CompoundTag EMPTY_DATA = new CompoundTag();

    public static final Codec<Compressed> CODEC = RecordCodecBuilder.create(in -> in.group(
            EntityTypesCodecs.CLAY_SOLDIER_CODEC.fieldOf(ENTITY_TYPE_TAG).forGetter(d -> d.type),
            CompoundTag.CODEC.optionalFieldOf(DATA_TAG, EMPTY_DATA).forGetter(d -> d.data),
            Data.CODEC.fieldOf("explicitData").forGetter(d -> d.explicitData),
            TimeData.CODEC.optionalFieldOf("timeData", TimeData.EMPTY).forGetter(d -> d.timeData),
            CLAY_TEAM_KEY_CODEC.fieldOf(ClayMobTeam.TEAM_ID_TAG).forGetter(d -> d.teamReference),
            Codec.FLOAT.optionalFieldOf(SOLDIER_SPEED_TAG, DEFAULT_SPEED).forGetter(d -> d.speed),
            Codec.LONG.optionalFieldOf(SOLDIER_ENTER_TAG, 0L).forGetter(d -> d.enterTime),
            Codec.BOOL.fieldOf("client").forGetter(s -> s.client)
    ).apply(in, Compressed::new));

    private final EntityType<? extends AbstractClaySoldierEntity> type;
    private final CompoundTag serverData;
    private final Data explicitData;
    private final TimeData timeData;
    @NotNull
    private final Holder.Reference<ClayMobTeam> teamReference;
    private final float speed;
    private final float roundedSpeed;
    private final long enterTime;

    @Nullable
    private FakeClaySoldierAccess clientSoldier;

    private OccupantSoldierData(EntityType<? extends AbstractClaySoldierEntity> type, CompoundTag data, Data explicitData, @Nullable TimeData timeData, Holder.Reference<ClayMobTeam> teamId, float speed, long enterTime) {
        this.type = type;
        this.serverData = data;
        this.explicitData = explicitData;
        this.timeData = timeData;
        this.speed = speed;
        this.roundedSpeed = Math.round(speed * 3.4 * 10) / 10f;
        this.enterTime = enterTime;
        this.teamReference = teamId;
    }

    public static OccupantSoldierData of(AbstractClaySoldierEntity soldier, int acceleration) {
        TimeData timeData = TimeData.of(soldier);
        soldier.getActiveEffectsMap().clear();

        TagValueOutput output = TagValueOutput.createWithContext(ClaySoldiersCommon.PROBLEM_REPORTER, soldier.registryAccess());
        soldier.save(output);

        IGNORED_TAGS.forEach(output::discard);
        //Todo
        return new OccupantSoldierData(soldier.getType(),
                output.buildResult(),
                Data.of(soldier),
                timeData,
                soldier.getClayTeamHolder(),
                (float) soldier.getAttribute(Attributes.MOVEMENT_SPEED).getValue() * (acceleration + 1),
                soldier.level().getGameTime()
        );
    }

    public static OccupantSoldierData create(EntityType<? extends AbstractClaySoldierEntity> type, Holder.Reference<ClayMobTeam> team, OccupantSoldierData.Data data, long gameTime) {
        return new OccupantSoldierData(
                type,
                new CompoundTag(),
                data,
                TimeData.EMPTY,
                team,
                (float) DefaultAttributes.getSupplier(type).getValue(Attributes.MOVEMENT_SPEED),
                gameTime
        );
    }


    public ResourceKey<ClayMobTeam> getTeamKey() {
        return teamReference.key();
    }

    public boolean canBeKilledBy(ServerLevel level, ServerPlayer player) {
        return player.getUUID().equals(explicitData.ownerQuery().getOwner(level));
    }

    public Component getTypeDescription() {
        return type.getDescription();
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

            entity.getActiveEffectsMap().putAll(timeData.loadEffects((int) (level.getGameTime() - enterTime)));

            entity.setClayTeamType(teamReference);
            entity.setCanPickUpLoot(true);

            entity.setWaxed(explicitData.waxed());
            entity.getInventory().copyFrom(explicitData.inventory());
            entity.setOffsetColor(explicitData.offsetColor());
            entity.setSpawnedFrom(explicitData.spawnedFrom, explicitData.dropSpawnedFrom);
            return entity;
        } else {
            return null;
        }
    }

    public @Nullable FakeClaySoldierAccess getClientSoldier() {
        return clientSoldier;
    }

    private void setUpClient(BlockPos pos, WalkAnimationState state) {
        clientSoldier = ClientClaySoldierEntity.create(type, explicitData, pos, state, teamReference);
        clientSoldier.setUpCape();
    }

    public void dropItems(ServerLevel level, double x, double y, double z) {
        explicitData.inventory().dropInventory(level, (_, stack) -> spawnItemInWorld(level, stack, x, y, z));

        if (explicitData.dropSpawnedFrom()) {
            ClayMobEntity.dropSpawnedFrom(level, explicitData.spawnedFrom(), (stack) -> spawnItemInWorld(level, stack, x, y, z), false, false);
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
                type, client ? EMPTY_DATA : serverData, explicitData, timeData, teamReference.key(), speed, enterTime, client
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
        private final TimeData timeData;
        private final ResourceKey<ClayMobTeam> teamReference;
        private final float speed;
        private final long enterTime;
        private final boolean client;

        private Compressed(EntityType<? extends AbstractClaySoldierEntity> type, CompoundTag data, Data explicitData, TimeData timeData, ResourceKey<ClayMobTeam> team, float speed, long enterTime, boolean client) {
            this.type = type;
            this.data = data;
            this.explicitData = explicitData;
            this.timeData = timeData;
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
                    type, data, explicitData,
                    client ? TimeData.EMPTY : timeData,
                    team.orElseThrow(), speed, enterTime
            );

            if (client) {
                res.setUpClient(pos, state);
            }

            return Optional.of(res);
        }
    }

    public record Data(float scale, boolean waxed, boolean dropSpawnedFrom, ItemStack spawnedFrom,
                       ClaySoldierInventory inventory, int skinVariantId,
                       ColorHelper offsetColor, Optional<ClaySoldierChip<?>> chip, OwnerQuery ownerQuery) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(in -> in.group(
                Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(Data::scale),
                Codec.BOOL.optionalFieldOf(ClayMobEntity.WAXED_TAG, false).forGetter(Data::waxed),
                Codec.BOOL.optionalFieldOf(ClayMobEntity.DROP_SPAWNED_FROM_TAG, true).forGetter(Data::dropSpawnedFrom),
                ItemStack.OPTIONAL_CODEC.optionalFieldOf(ClayMobEntity.SPAWNED_FROM_TAG, ItemStack.EMPTY).forGetter(Data::spawnedFrom),
                ClaySoldierInventory.DIRECT_CODEC.optionalFieldOf(ClaySoldierInventory.INVENTORY_TAG, ClaySoldierInventory.EMPTY).forGetter(Data::inventory),
                Codec.INT.optionalFieldOf(AbstractClaySoldierEntity.SKIN_VARIANT_ID_TAG, 0).forGetter(Data::skinVariantId),
                ColorHelper.CODEC.optionalFieldOf(AbstractClaySoldierEntity.OFFSET_COLOR_TAG, ColorHelper.EMPTY).forGetter(Data::offsetColor),
                ClaySoldierChip.CODEC.optionalFieldOf(ProgrammableClaySoldierEntity.CHIP_TAG).forGetter(Data::chip),
                OwnerQuery.CODEC.optionalFieldOf(ProgrammableClaySoldierEntity.OWNER_UUID_TAG, OwnerQuery.none()).forGetter(Data::ownerQuery)
        ).apply(in, Data::new));

        public static Data of(AbstractClaySoldierEntity soldier) {
            ClaySoldierChip<?> chip = soldier instanceof ProgrammableClayMobAccess access ? access.getInstalledChip() : null;

            return new Data(
                    soldier.getScale(),
                    soldier.isWaxed(),
                    soldier.shouldDropSpawnedFrom(),
                    soldier.getSpawnedFrom(),
                    soldier.getInventory(),
                    soldier.getSkinVariant(),
                    soldier.getOffsetColor(),
                    Optional.ofNullable(chip),
                    soldier.createOwnerQuery());
        }
    }

    private record TimeData(List<MobEffectInstance> effects) {
        public static final Codec<TimeData> CODEC = RecordCodecBuilder.create(in -> in.group(
                MobEffectInstance.CODEC.listOf().optionalFieldOf("active_effects", List.of()).forGetter(TimeData::effects)
        ).apply(in, TimeData::new));
        public static final TimeData EMPTY = new TimeData(List.of());

        public static TimeData of(LivingEntity entity) {
            return new TimeData(List.copyOf(entity.getActiveEffectsMap().values()));
        }

        private Map<Holder<MobEffect>, MobEffectInstance> loadEffects(int elapsedTime) {
            Map<Holder<MobEffect>, MobEffectInstance> map = new HashMap<>();

            for (var oldEff : effects()) {
                if (oldEff != null) {
                    if (!oldEff.endsWithin(elapsedTime)) {
                        var newInstance = new MobEffectInstance(oldEff.getEffect(), Math.max(1, oldEff.getDuration() - elapsedTime), oldEff.getAmplifier(), oldEff.isAmbient(), oldEff.isVisible(), oldEff.showIcon());
                        map.put(oldEff.getEffect(), newInstance);
                    }
                }
            }

            return map;
        }
    }
}
