package net.bumblebee.claysoldiers.networking.spawnpayloads;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.boss.BossClaySoldierBehaviour;
import net.bumblebee.claysoldiers.entity.common.boss.BossClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.networking.IClientPayload;
import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public final class ClayBossSpawnPayload implements IClientPayload {
    public static final Type<ClayBossSpawnPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_boss_spawn"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClayBossSpawnPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClayBossSpawnPayload::entity,
            SoldierPropertyMap.STREAM_CODEC, ClayBossSpawnPayload::baseProperties,
            ByteBufCodecs.registry(ModRegistries.BOSS_CLAY_SOLDIER_BEHAVIOURS), s -> s.bossAI,
            ClayBossSpawnPayload::new
    );
    private final int entity;
    private final SoldierPropertyMap baseProperties;
    private final BossClaySoldierBehaviour bossAI;

    private ClayBossSpawnPayload(int entity, SoldierPropertyMap baseProperties, BossClaySoldierBehaviour bossAI) {
        this.entity = entity;
        this.baseProperties = baseProperties;
        this.bossAI = bossAI;
    }
    public ClayBossSpawnPayload(BossClaySoldierEntity boss) {
        this(boss.getId(), boss.getBaseProperties(), boss.getOptionalBossAI(true).orElseThrow());
    }


    @Override
    public void handleClient(NetworkManger.PayloadContext context) {
        if (context.client().level.getEntity(entity) instanceof BossClaySoldierEntity boss) {
            boss.setBossAI(bossAI);
            boss.setBaseProperties(baseProperties);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public int entity() {
        return entity;
    }

    public SoldierPropertyMap baseProperties() {
        return baseProperties;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ClayBossSpawnPayload) obj;
        return this.entity == that.entity &&
                Objects.equals(this.baseProperties, that.baseProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity, baseProperties);
    }

    @Override
    public String toString() {
        return "ClayBossSpawnPayload[baseProperties: %s, bossAI: %s]".formatted(baseProperties, bossAI);
    }

}
