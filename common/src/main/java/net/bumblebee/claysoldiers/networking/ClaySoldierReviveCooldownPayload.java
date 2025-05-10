package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.revive.ReviveType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ClaySoldierReviveCooldownPayload implements IClientPayload {
    public static final Type<ClaySoldierReviveCooldownPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_revive_map"));

    private final int entity;
    private final ReviveType reviveType;
    private final int cooldown;
    private static final StreamCodec<FriendlyByteBuf, ReviveType> REVIVE_TYPE_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ReviveType decode(FriendlyByteBuf byteBuf) {
            return byteBuf.readEnum(ReviveType.class);
        }

        @Override
        public void encode(FriendlyByteBuf buf, ReviveType reviveType) {
            buf.writeEnum(reviveType);
        }
    };

    public ClaySoldierReviveCooldownPayload(int entity, ReviveType reviveType, int cooldown) {
        this.entity = entity;
        this.reviveType = reviveType;
        this.cooldown = cooldown;
    }
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaySoldierReviveCooldownPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ClaySoldierReviveCooldownPayload::getEntity,
            REVIVE_TYPE_STREAM_CODEC,
            ClaySoldierReviveCooldownPayload::getReviveType,
            ByteBufCodecs.VAR_INT,
            ClaySoldierReviveCooldownPayload::getCooldown,
            ClaySoldierReviveCooldownPayload::new
    );

    public int getEntity() {
        return entity;
    }
    public int getCooldown() {
        return cooldown;
    }
    public ReviveType getReviveType() {
        return reviveType;
    }

    @Override
    public @NotNull Type<ClaySoldierReviveCooldownPayload> type() {
        return ID;
    }

    @Override
    public void handleClient(INetworkManger.PayloadContext context) {
        Level level = context.player().level();
        Entity entity = level.getEntity(getEntity());
        if (entity instanceof AbstractClaySoldierEntity soldier) {
            soldier.setReviveOnCooldown(getReviveType(), getCooldown());
        }
    }
}
