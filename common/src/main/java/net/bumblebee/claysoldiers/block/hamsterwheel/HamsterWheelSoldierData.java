package net.bumblebee.claysoldiers.block.hamsterwheel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.client.ClientClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.inventory.ClaySoldierInventory;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class HamsterWheelSoldierData {
    private static final Logger LOGGER = ClaySoldiersCommon.LOGGER;
    private static final Codec<EntityType<?>> ENTITY_TYPE_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec();
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
            ClayMobTeam.TEAM_ID_TAG
    );
    private static final List<String> CLIENT_NEEDED_TAGS = Arrays.asList(
            ClaySoldierInventory.INVENTORY_TAG,
            AbstractClaySoldierEntity.OFFSET_COLOR_TAG,
            AbstractClaySoldierEntity.SKIN_VARIANT_ID_TAG,
            ClayMobEntity.WAXED_TAG
    );

    public static final String CLIENT_TAG = "client";

    private static final String ENTITY_TYPE_TAG = "type";
    private static final String DATA_TAG = "SoldierData";
    private static final String SOLDIER_SIZE_TAG = "SoldierSize";
    private static final String SOLDIER_SPEED_TAG = "SoldierSpeed";
    private static final String SOLDIER_ENTER_TAG = "SoldierEnterTime";
    private static final float DEFAULT_SPEED = AbstractClaySoldierEntity.BASE_MOVEMENT_SPEED * ClaySoldiersCommon.CONFIG.getCommonConfig().getHamsterWheelSpeed();

    private static final Codec<HamsterWheelSoldierData> CODEC = RecordCodecBuilder.create(in -> in.group(
            ENTITY_TYPE_CODEC.fieldOf(ENTITY_TYPE_TAG).forGetter(d -> d.type),
            CompoundTag.CODEC.fieldOf(DATA_TAG).forGetter(d -> d.data),
            CLAY_TEAM_KEY_CODEC.fieldOf(ClayMobTeam.TEAM_ID_TAG).forGetter(d -> d.teamReference),
            Codec.FLOAT.optionalFieldOf(SOLDIER_SIZE_TAG, 1f).forGetter(d -> d.soldierScale),
            Codec.FLOAT.optionalFieldOf(SOLDIER_SPEED_TAG, DEFAULT_SPEED).forGetter(d -> d.speed),
            Codec.LONG.optionalFieldOf(SOLDIER_ENTER_TAG, 0L).forGetter(d -> d.enterTime)
    ).apply(in, HamsterWheelSoldierData::createUnsafe));

    private final EntityType<? extends AbstractClaySoldierEntity> type;
    private final CompoundTag data;
    @NotNull
    private final ResourceKey<ClayMobTeam> teamReference;
    private final float soldierScale;
    private final float speed;
    private final float roundedSpeed;
    private final long enterTime;

    @Nullable
    private ClientClaySoldierEntity clientSoldier;

    private HamsterWheelSoldierData(EntityType<? extends AbstractClaySoldierEntity> type, CompoundTag data, ResourceKey<ClayMobTeam> teamId, float soldierScale, float speed, long enterTime) {
        this.type = type;
        this.data = data;
        this.speed = speed;
        this.roundedSpeed = Math.round(speed * 3.4 * 10) / 10f;
        this.soldierScale = soldierScale;
        this.enterTime = enterTime;
        this.teamReference = teamId;

    }

    public ResourceKey<ClayMobTeam> getTeamKey() {
        return teamReference;
    }

    @SuppressWarnings("unchecked")
    private static HamsterWheelSoldierData createUnsafe(EntityType<?> type, CompoundTag tag, ResourceKey<ClayMobTeam> id, float size, float speed, long enterTime) {
        try {
            return new HamsterWheelSoldierData((EntityType<? extends AbstractClaySoldierEntity>) type, tag, id, size, speed, enterTime);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Type " + type + " does not extend AbstractClaySoldierEntity");
        }
    }

    public static HamsterWheelSoldierData of(AbstractClaySoldierEntity soldier) {
        TagValueOutput compoundtag = TagValueOutput.createWithContext(ClaySoldiersCommon.PROBLEM_REPORTER, soldier.registryAccess());
        soldier.save(compoundtag);
        IGNORED_TAGS.forEach(compoundtag::discard);
        return HamsterWheelSoldierData.createUnsafe(soldier.getType(), compoundtag.buildResult(), soldier.getClayTeamKey(), soldier.getScale(), (float) soldier.getAttribute(Attributes.MOVEMENT_SPEED).getValue(), soldier.level().getGameTime());
    }

    /**
     * Saves this {@code ClaySoldierBlockData} to the give {@code CompoundTag}.
     */
    public void save(ValueOutput tag, boolean client) {
        tag.store(ENTITY_TYPE_TAG, ENTITY_TYPE_CODEC, type);
        tag.store(ClayMobTeam.TEAM_ID_TAG, CLAY_TEAM_KEY_CODEC, teamReference);


        if (client) {
            tag.store(DATA_TAG, CompoundTag.CODEC, getForClient());
            tag.putBoolean(CLIENT_TAG, true);
        } else {
            tag.store(DATA_TAG, CompoundTag.CODEC, getForServer());
            tag.putLong(SOLDIER_ENTER_TAG, enterTime);
        }
        if (soldierScale != 1) {
            tag.putFloat(SOLDIER_SIZE_TAG, soldierScale);
        }
        if (speed >= DEFAULT_SPEED + 0.1f || speed <= DEFAULT_SPEED - 0.1f) {
            tag.putFloat(SOLDIER_SPEED_TAG, speed);
        }
    }

    /**
     * Loads a {@link HamsterWheelSoldierData} instance from the given {@link CompoundTag}.
     * <p>
     * This method reads the entity type, team ID, and additional data from the tag and constructs
     * a {@link HamsterWheelSoldierData} object. If the tag contains the client-specific data,
     * the {@code setUpClient} method is called to initialize client-side information.
     * <p>
     * Note: The {@code CompoundTag} should be appended with the {@link #CLIENT_TAG} tag beforehand when on the client.
     *
     * @param tag   The {@link CompoundTag} containing the data necessary to reconstruct the {@link HamsterWheelSoldierData}.
     * @param pos   The {@link BlockPos} representing the block's position, used for client-side setup.
     * @param state The {@link WalkAnimationState} used for animation setup on the client.
     * @return A {@link HamsterWheelSoldierData} instance if the entity type exists in the tag, otherwise {@code null}.
     * @throws IllegalStateException If the entity type parameter does not extend the AbstractSoldierEntity.
     */
    public static @Nullable HamsterWheelSoldierData load(ValueInput tag, BlockPos pos, WalkAnimationState state, @Nullable RegistryAccess registryAccess) {
        EntityType<?> type = tag.read(ENTITY_TYPE_TAG, ENTITY_TYPE_CODEC).orElse(null);
        if (type == null) {
            return null;
        }

        ResourceKey<ClayMobTeam> id = tag.read(ClayMobTeam.TEAM_ID_TAG, CLAY_TEAM_KEY_CODEC).orElse(null);

        CompoundTag data = tag.read(DATA_TAG, CompoundTag.CODEC).orElse(null);
        if (data == null || id == null) {
            return null;
        }
        if (registryAccess != null && registryAccess.get(id).isEmpty()) {
            LOGGER.warn("Loaded Soldier Data with non existing Team: {}", id);
            ClaySoldiersCommon.ERROR_HANDLER.warn("Loaded Soldier Data with no-existing Team");
            return null;
        }

        float size = tag.getFloatOr(SOLDIER_SIZE_TAG, 1f);
        float speed = tag.getFloatOr(SOLDIER_SPEED_TAG, DEFAULT_SPEED);
        long enterTime = tag.getLongOr(SOLDIER_ENTER_TAG, 0);

        HamsterWheelSoldierData soldierData = HamsterWheelSoldierData.createUnsafe(
                type, data, id, size, speed, enterTime
        );
        if (tag.getBooleanOr(CLIENT_TAG, false)) {
            soldierData.setUpClient(pos, state);
        }




        return soldierData;
    }

    public static void markTagAsClient(ValueOutput tag) {
        tag.putBoolean(CLIENT_TAG, true);
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
        CompoundTag newEntityData = this.data.copy();
        IGNORED_TAGS.forEach(newEntityData::remove);

        AbstractClaySoldierEntity entity = type.create(level, EntitySpawnReason.EVENT);
        ClayMobTeam.save(getTeamKey(), newEntityData);

        if (entity != null) {
            entity.load(TagValueInput.create(ClaySoldiersCommon.PROBLEM_REPORTER, level.registryAccess(), newEntityData));
            data.read(Entity.TAG_UUID, UUIDUtil.CODEC).ifPresentOrElse(entity::setUUID, () -> ClaySoldiersCommon.ERROR_HANDLER.debug("Loaded a ClaySoldier without its UUID"));

            entity.getActiveEffectsMap().clear();
            entity.getActiveEffectsMap().putAll(loadEffects(data, (int) (level.getGameTime() - enterTime), level.registryAccess()));

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

    public @Nullable ClientClaySoldierEntity getClientSoldier() {
        return clientSoldier;
    }

    private CompoundTag getForClient() {
        CompoundTag tag = new CompoundTag();
        List<String> missingTags = new ArrayList<>();
        CLIENT_NEEDED_TAGS.forEach(tagKey -> {
            Tag entry = data.get(tagKey);
            if (entry != null) {
                tag.put(tagKey, entry);
            } else {
                missingTags.add(tagKey);
            }
        });
        if (!missingTags.isEmpty()) {
            LOGGER.debug("Missing Client Tags: {}", missingTags);
        }
        ClayMobTeam.save(getTeamKey(), tag);
        return tag;
    }

    private CompoundTag getForServer() {
        CompoundTag tag = data.copy();
        ClayMobTeam.save(getTeamKey(), tag);
        return tag;
    }

    private void setUpClient(BlockPos pos, WalkAnimationState state) {
        clientSoldier = ClientClaySoldierEntity.create(type, data, pos, state, teamReference, soldierScale);
        clientSoldier.setUpCape();
    }

    public void dropItems(ServerLevel level, double x, double y, double z) {
        ValueInput input = TagValueInput.create(ClaySoldiersCommon.PROBLEM_REPORTER, level.registryAccess(), data);
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
        return "HamsterWheelSoldierData(%s(%s), Speed: (%s - %s)".formatted("ClaySoldier", getTeamKey().identifier(), speed, roundedSpeed);
    }
}
