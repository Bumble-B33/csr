package net.bumblebee.claysoldiers.entity.soldier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;

import java.util.Objects;

public final class AdditionalSoldierData {
    private static final String ERROR_MESSAGE_CASTING = "Wrong Entity Type for AdditionalSoldierData. %s Does not extend ClayMobEntity and ClaySoldierLike";

    public static final Codec<AdditionalSoldierData> CODEC = RecordCodecBuilder.create(in -> in.group(
            createEntityTypeCodec().fieldOf("type").forGetter(AdditionalSoldierData::soldierType),
            CompoundTag.CODEC.optionalFieldOf("additional_data", new CompoundTag()).forGetter(AdditionalSoldierData::tag)
    ).apply(in, AdditionalSoldierData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AdditionalSoldierData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ENTITY_TYPE), AdditionalSoldierData::soldierType,
            ByteBufCodecs.COMPOUND_TAG, AdditionalSoldierData::tag,
            (type, tag) -> new AdditionalSoldierData(cast(type).getOrThrow(), tag)
    );

    private final EntityType<? extends ClayMobEntity> soldierType;
    private final CompoundTag tag;

    public <T extends ClayMobEntity & ClaySoldierLike> AdditionalSoldierData(EntityType<T> soldierType, CompoundTag tag) {
        this.soldierType = soldierType;
        this.tag = tag;
    }

    /**
     * Converts the give SoldierLike to the stored entity type.
     */
    public <T extends ClayMobEntity & ClaySoldierLike> void convert(T soldier) {
        soldier.convertToSoldier(soldierType(), (newSoldier) -> {
            newSoldier.readItemPersistentData(tag);
            newSoldier.setClayTeamType(soldier.getClayTeamType());
            newSoldier.onConversion(soldier, tag);
        });
    }

    public Component displayName() {
        return soldierType.getDescription();
    }

    @SuppressWarnings("unchecked")
    public <T extends ClayMobEntity & ClaySoldierLike> EntityType<T> soldierType() {
        return (EntityType<T>) soldierType;
    }

    public CompoundTag tag() {
        return tag;
    }

    private static <T extends ClayMobEntity & ClaySoldierLike> Codec<EntityType<T>> createEntityTypeCodec() {
        return BuiltInRegistries.ENTITY_TYPE.byNameCodec().comapFlatMap(AdditionalSoldierData::cast, entityType -> entityType);
    }

    @SuppressWarnings("unchecked")
    private static <T extends ClayMobEntity & ClaySoldierLike> DataResult<EntityType<T>> cast(EntityType<?> type) {
        try {
            return DataResult.success((EntityType<T>) type);
        } catch (ClassCastException e) {
            return DataResult.error(() -> ERROR_MESSAGE_CASTING.formatted(type));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AdditionalSoldierData) obj;
        return this.soldierType == that.soldierType &&
                Objects.equals(this.tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(soldierType, tag);
    }

    @Override
    public String toString() {
        return "AdditionalSoldierData[" +
                "Type: " + soldierType + ", " +
                "Data: " + tag + ']';
    }

}
