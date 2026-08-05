package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class ClaySoldierChipUpdatePayload implements IClientPayload {
    public static final Type<ClaySoldierChipUpdatePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "programmable_clay_soldier_chip_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaySoldierChipUpdatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, s -> s.entityId,
            ClaySoldierChip.STREAM_CODEC, s -> s.chip,
            ClaySoldierChipUpdatePayload::new
    );

    private final int entityId;
    private final ClaySoldierChip chip;

    public ClaySoldierChipUpdatePayload(int entity, ClaySoldierChip chip) {
        this.entityId = entity;
        this.chip = chip;
    }

    @Override
    public void handleClient(NetworkManger.PayloadContext context) {
        Entity entity = context.player().level().getEntity(entityId);
        if (entity instanceof ProgrammableClaySoldierEntity claySoldier) {
            claySoldier.setupChip(chip);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
