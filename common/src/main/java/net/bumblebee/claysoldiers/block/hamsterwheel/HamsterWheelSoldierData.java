package net.bumblebee.claysoldiers.block.hamsterwheel;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.client.ClientClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.util.ErrorHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class HamsterWheelSoldierData {
    private static final Codec<EntityType<?>> ENTITY_TYPE_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec();
    private static final List<String> IGNORED_TAGS = Arrays.asList(
            "Air",
            "ArmorDropChances",
            "ArmorItems",
            "Brain",
            "CanPickUpLoot",
            "DeathTime",
            "FallDistance",
            "FallFlying",
            "Fire",
            "HandDropChances",
            "HandItems",
            "Health",
            "HurtByTimestamp",
            "HurtTime",
            "LeftHanded",
            "Motion",
            "OnGround",
            "PortalCooldown",
            "Pos",
            "Rotation",
            "SleepingX",
            "SleepingY",
            "SleepingZ",
            Entity.ID_TAG,
            Entity.PASSENGERS_TAG,
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
            AbstractClaySoldierEntity.BACKPACK_ITEMS_TAG,
            AbstractClaySoldierEntity.HAND_ITEMS_TAG,
            AbstractClaySoldierEntity.ARMOR_ITEMS_TAG,
            AbstractClaySoldierEntity.OFFSET_COLOR_TAG,
            AbstractClaySoldierEntity.SKIN_VARIANT_ID_TAG,
            ClayMobEntity.WAXED_TAG
    );
    private static final String ACTIVE_EFFECTS_TAG = "active_effects";

    private static final StreamCodec<RegistryFriendlyByteBuf, EntityType<?>> STREAM_CODEC_ENTITY_TYPE = ByteBufCodecs.registry(Registries.ENTITY_TYPE);
    public static final StreamCodec<RegistryFriendlyByteBuf, HamsterWheelSoldierData> STREAM_CODEC_CLIENT = new StreamCodec<RegistryFriendlyByteBuf, HamsterWheelSoldierData>() {
        @Override
        public HamsterWheelSoldierData decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            return createUnsafe(
                    STREAM_CODEC_ENTITY_TYPE.decode(registryFriendlyByteBuf),
                    ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf),
                    ByteBufCodecs.COMPOUND_TAG.decode(registryFriendlyByteBuf),
                    ByteBufCodecs.FLOAT.decode(registryFriendlyByteBuf),
                    ByteBufCodecs.FLOAT.decode(registryFriendlyByteBuf),
                    ByteBufCodecs.VAR_LONG.decode(registryFriendlyByteBuf),
                    registryFriendlyByteBuf.registryAccess()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf o, HamsterWheelSoldierData hamsterWheelSoldierData) {
            STREAM_CODEC_ENTITY_TYPE.encode(o, hamsterWheelSoldierData.type);
            ResourceLocation.STREAM_CODEC.encode(o, hamsterWheelSoldierData.getTeamId());
            ByteBufCodecs.FLOAT.encode(o, hamsterWheelSoldierData.soldierScale);
            ByteBufCodecs.FLOAT.encode(o, hamsterWheelSoldierData.speed);
            ByteBufCodecs.VAR_LONG.encode(o, hamsterWheelSoldierData.enterTime);

        }
    };

    public static final String CLIENT_TAG = "client";

    private static final String ENTITY_TYPE_TAG = "type";
    private static final String DATA_TAG = "SoldierData";
    private static final String SOLDIER_SIZE_TAG = "SoldierSize";
    private static final String SOLDIER_SPEED_TAG = "SoldierSpeed";
    private static final String SOLDIER_ENTER_TAG = "SoldierEnterTime";

    private final EntityType<? extends AbstractClaySoldierEntity> type;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final CompoundTag data;
    @NotNull
    private final Holder.Reference<ClayMobTeam> teamReference;
    private final float soldierScale;
    private final float speed;
    private final float roundedSpeed;
    private final long enterTime;

    @Nullable
    private ClientClaySoldierEntity clientSoldier;

    private HamsterWheelSoldierData(EntityType<? extends AbstractClaySoldierEntity> type, CompoundTag data, ResourceLocation teamId, float soldierScale, float speed, long enterTime, HolderLookup.Provider registries) {
        this.type = type;
        this.data = data;
        this.speed = speed;
        this.roundedSpeed = Math.round(speed * 3.4 * 10) / 10f;
        this.soldierScale = soldierScale;
        this.enterTime = enterTime;
        this.teamReference = ClayMobTeamManger.getHolder(teamId, registries).orElse(ClayMobTeamManger.getDefault(registries));
    }

    public ResourceLocation getTeamId() {
        return teamReference.key().location();
    }

    @SuppressWarnings("unchecked")
    private static HamsterWheelSoldierData createUnsafe(EntityType<?> type, ResourceLocation id, CompoundTag tag, float size, float speed, long enterTime, HolderLookup.Provider registries) {
        try {
            return new HamsterWheelSoldierData((EntityType<? extends AbstractClaySoldierEntity>) type, tag, id, size, speed, enterTime, registries);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Type " + type + " does not extend AbstractClaySoldierEntity");
        }
    }

    public static HamsterWheelSoldierData of(AbstractClaySoldierEntity soldier) {
        CompoundTag compoundtag = new CompoundTag();
        soldier.save(compoundtag);
        IGNORED_TAGS.forEach(compoundtag::remove);
        return HamsterWheelSoldierData.createUnsafe(soldier.getType(), soldier.getClayTeamType(), compoundtag, soldier.getScale(), (float) soldier.getAttribute(Attributes.MOVEMENT_SPEED).getValue(), soldier.level().getGameTime(), soldier.registryAccess());
    }

    /**
     * Saves this {@code ClaySoldierBlockData} to the give {@code CompoundTag}.
     */
    public void save(CompoundTag tag) {
        ENTITY_TYPE_CODEC.encodeStart(NbtOps.INSTANCE, type)
                .ifSuccess(typeTag -> tag.put(ENTITY_TYPE_TAG, typeTag))
                .ifError(err -> LOGGER.error(err.message()));
        ClayMobTeam.save(getTeamId(), tag);

        if (tag.contains(CLIENT_TAG)) {
            tag.put(DATA_TAG, getForClient());
        } else {
            tag.put(DATA_TAG, getForServer());
            tag.putLong(SOLDIER_ENTER_TAG, enterTime);
        }
        if (soldierScale != 1) {
            tag.putFloat(SOLDIER_SIZE_TAG, soldierScale);
        }
        if (speed >= 0.31f || speed <= 0.29f) {
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
    public static @Nullable HamsterWheelSoldierData load(CompoundTag tag, BlockPos pos, WalkAnimationState state, HolderLookup.Provider registries) {
        if (!tag.contains(ENTITY_TYPE_TAG)) {
            return null;
        }
        EntityType<?> type = ENTITY_TYPE_CODEC.parse(NbtOps.INSTANCE, tag.get(ENTITY_TYPE_TAG)).getOrThrow(err -> new IllegalStateException("Cannot load SoldierBlockData without EntityType: " + err));
        ResourceLocation id = ClayMobTeam.read(tag);

        CompoundTag data = tag.getCompound(DATA_TAG);
        float size = 1;
        if (tag.contains(SOLDIER_SIZE_TAG, Tag.TAG_ANY_NUMERIC)) {
            size = tag.getFloat(SOLDIER_SIZE_TAG);
        }
        float speed = 0.3f;
        if (tag.contains(SOLDIER_SPEED_TAG, Tag.TAG_ANY_NUMERIC)) {
            speed = tag.getFloat(SOLDIER_SPEED_TAG);
        }

        HamsterWheelSoldierData blockData = HamsterWheelSoldierData.createUnsafe(
                type, id, data, size, speed, tag.getLong(SOLDIER_ENTER_TAG), registries
        );
        if (tag.contains(CLIENT_TAG)) {
            blockData.setUpClient(pos, state);
        }


        return blockData;
    }

    public static void markTagAsClient(CompoundTag tag) {
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
    public AbstractClaySoldierEntity createSoldier(Level level) {
        CompoundTag newEntityData = this.data.copy();
        IGNORED_TAGS.forEach(newEntityData::remove);

        AbstractClaySoldierEntity entity = type.create(level);
        ClayMobTeam.save(getTeamId(), newEntityData);

        if (entity != null) {
            CustomData.of(newEntityData).loadInto(entity);
            if (data.hasUUID(Entity.UUID_TAG)) {
                entity.setUUID(data.getUUID(Entity.UUID_TAG));
            } else {
                ErrorHandler.INSTANCE.debug("Loaded a ClaySoldier without its UUID");
            }

            entity.getActiveEffectsMap().clear();
            entity.getActiveEffectsMap().putAll(loadEffects(data, (int) (level.getGameTime() - enterTime)));

            return entity;
        } else {
            return null;
        }
    }

    private static Map<Holder<MobEffect>, MobEffectInstance> loadEffects(CompoundTag tag, int elapsedTime) {
        Map<Holder<MobEffect>, MobEffectInstance> map = new HashMap<>();
        if (tag.contains(ACTIVE_EFFECTS_TAG, Tag.TAG_LIST)) {
            ListTag listtag = tag.getList(ACTIVE_EFFECTS_TAG, Tag.TAG_COMPOUND);

            for (int i = 0; i < listtag.size(); i++) {
                CompoundTag compoundtag = listtag.getCompound(i);
                MobEffectInstance oldEff = MobEffectInstance.load(compoundtag);
                if (oldEff != null) {
                    if (!oldEff.endsWithin(elapsedTime)) {
                        var newInstance = new MobEffectInstance(oldEff.getEffect(), Math.max(1, oldEff.getDuration() - elapsedTime),  oldEff.getAmplifier(), oldEff.isAmbient(), oldEff.isVisible(), oldEff.showIcon());
                        map.put(oldEff.getEffect(), newInstance);
                    }
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
        ClayMobTeam.save(getTeamId(), tag);
        return tag;
    }

    private CompoundTag getForServer() {
        CompoundTag tag = data.copy();
        ClayMobTeam.save(getTeamId(), tag);
        return tag;
    }

    private void setUpClient(BlockPos pos, WalkAnimationState state) {
        clientSoldier = ClientClaySoldierEntity.create(type, data, pos, state, teamReference, soldierScale);
        clientSoldier.setUpCape();
    }

    public void dropItems(ServerLevel level, double x, double y, double z) {
        List<ItemStackWithEffect> stackWithEffects = new ArrayList<>();
        AbstractClaySoldierEntity.getFromTag(data, AbstractClaySoldierEntity.HAND_ITEMS_TAG, (i, s) -> stackWithEffects.add(s), level.registryAccess());
        AbstractClaySoldierEntity.getFromTag(data, AbstractClaySoldierEntity.ARMOR_ITEMS_TAG, (i, s) -> stackWithEffects.add(s), level.registryAccess());
        AbstractClaySoldierEntity.getFromTag(data, AbstractClaySoldierEntity.BACKPACK_ITEMS_TAG, (i, s) -> stackWithEffects.add(s), level.registryAccess());

        AbstractClaySoldierEntity.dropInventory(level, slot -> stackWithEffects.get(slot.ordinal()), (slot, stack) -> spawnItemInWorld(level, stack, x, y, z));

        if (data.contains(ClayMobEntity.DROP_SPAWNED_FROM_TAG) && data.getBoolean(ClayMobEntity.DROP_SPAWNED_FROM_TAG)) {
            ClayMobEntity.dropSpawnedFrom(level, ClayMobEntity.getSpawnedFromFromTag(data, level.registryAccess()), (stack) -> spawnItemInWorld(level, stack, x, y, z), false, false);
        }

    }

    private static void spawnItemInWorld(ServerLevel level, ItemStack stack, double x, double y, double z) {
        ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    @Override
    public String toString() {
        return "HamsterWheelSoldierData(%s(%s))".formatted(type.getBaseClass().getSimpleName(), getTeamId());
    }
}
