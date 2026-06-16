package net.bumblebee.claysoldiers.init;

import net.bumblebee.claysoldiers.entity.common.boss.BossClaySoldierBehaviour;
import net.bumblebee.claysoldiers.entity.common.boss.BossClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.programmable.ClaySoldierFishingData;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class ModEntitySerializers {
    public static final EntityDataSerializer<Optional<UUID>> UUID = EntityDataSerializer.forValueType(ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC));
    public static final EntityDataSerializer<ColorHelper> COLOR = EntityDataSerializer.forValueType(ColorHelper.STREAM_CODEC);
    public static final EntityDataSerializer<Holder.Reference<ClayMobTeam>> CLAY_TEAM = EntityDataSerializer.forValueType(ClayMobTeam.STREAM_CODEC);
    public static final EntityDataSerializer<ClaySoldierFishingData> FISHING_DATA = EntityDataSerializer.forValueType(ClaySoldierFishingData.STREAM_CODEC);

    public static final EntityDataSerializer<BossClaySoldierEntity.BossTypes> BOSS_TYPES = EntityDataSerializer.forValueType(BossClaySoldierEntity.BossTypes.STREAM_CODEC);
    public static final EntityDataSerializer<Optional<BossClaySoldierBehaviour>> BOSS_BEHAVIOUR = EntityDataSerializer.forValueType(ByteBufCodecs.optional(BossClaySoldierBehaviour.STREAM_CODEC));


    public static void register(BiConsumer<String, EntityDataSerializer<?>> event) {
        event.accept("owner_uuid", ModEntitySerializers.UUID);
        event.accept("color", ModEntitySerializers.COLOR);
        event.accept("clay_mob_team", ModEntitySerializers.CLAY_TEAM);
        event.accept("clay_soldier_fishing_data", ModEntitySerializers.FISHING_DATA);
        event.accept("clay_soldier_boss_type", ModEntitySerializers.BOSS_TYPES);
        event.accept("clay_soldier_boss_behaviour", ModEntitySerializers.BOSS_BEHAVIOUR);

    }
}
