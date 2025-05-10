package net.bumblebee.claysoldiers.networking.spawnpayloads;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.networking.IClientPayload;
import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class ClayMobSpawnPayload implements IClientPayload {
    public static final Type<ClayMobSpawnPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_mob_spawn"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClayMobSpawnPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClayMobSpawnPayload::getEntity,
            ResourceLocation.STREAM_CODEC, ClayMobSpawnPayload::getTeamId,
            ClayMobSpawnPayload::new
    );

    private final int entity;
    private final ResourceLocation teamId;

    public ClayMobSpawnPayload(ClayMobEntity entity) {
        this(entity.getId(), entity.getClayTeamType());
    }

    public ClayMobSpawnPayload(int entity, ResourceLocation teamId) {
        this.entity = entity;
        this.teamId = teamId;
    }

    protected int getEntity() {
        return entity;
    }

    protected ResourceLocation getTeamId() {
        return teamId;
    }

    @Override
    public void handleClient(INetworkManger.PayloadContext context) {
        if (context.client().level.getEntity(entity) instanceof ClayMobEntity clayMobEntity) {
            clayMobEntity.setClayTeamType(teamId);
        }
    }

    @Override
    public Type<? extends ClayMobSpawnPayload> type() {
        return ID;
    }
}
