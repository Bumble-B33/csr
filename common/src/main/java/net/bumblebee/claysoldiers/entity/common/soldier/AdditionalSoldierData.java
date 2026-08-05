package net.bumblebee.claysoldiers.entity.common.soldier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.util.codec.EntityTypesCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class AdditionalSoldierData {
    public static final Codec<AdditionalSoldierData> CODEC = RecordCodecBuilder.create(in -> in.group(
            EntityTypesCodecs.createClaySoldierLikeCodec().fieldOf("type").forGetter(AdditionalSoldierData::soldierType),
            CompoundTag.CODEC.optionalFieldOf("additional_data", new CompoundTag()).forGetter(AdditionalSoldierData::tag)
    ).apply(in, AdditionalSoldierData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AdditionalSoldierData> STREAM_CODEC = StreamCodec.composite(
            EntityTypesCodecs.createClaySoldierLikeStreamCodec(), AdditionalSoldierData::soldierType,
            ByteBufCodecs.COMPOUND_TAG, AdditionalSoldierData::tag,
            AdditionalSoldierData::new
    );

    private final EntityType<? extends ClayMobEntity> soldierType;
    private final CompoundTag tag;

    public <T extends ClayMobEntity & ClaySoldierLike> AdditionalSoldierData(EntityType<T> soldierType, CompoundTag tag) {
        this.soldierType = soldierType;
        this.tag = tag;
    }

    /**
     * Converts the give SoldierLike to the stored entity type.
     * @param cause The player how should receive credit for this conversion.
     */
    public <T extends ClayMobEntity & ClaySoldierLike> void convert(T soldier, @Nullable Player cause) {
        ValueInput input = TagValueInput.create(ClaySoldiersCommon.PROBLEM_REPORTER, soldier.registryAccess(), tag);
        soldier.convertToSoldier(soldierType(), (newSoldier) -> {
            newSoldier.readItemPersistentData(input);
            newSoldier.setClayTeamType(soldier.getClayTeamHolder());
            newSoldier.onConversion(soldier, input, cause);
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
        return "AdditionalSoldierData[Type: %s, Data: %s]".formatted(soldierType, tag);
    }
}
