package net.bumblebee.claysoldiers.networking.spawnpayloads;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.programmable.chips.ClaySoldierChip;
import net.bumblebee.claysoldiers.networking.IClientPayload;
import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ProgrammableClaySoldierSpawnPayload implements IClientPayload {
    public static final Type<ProgrammableClaySoldierSpawnPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "programmable_soldier_spawn"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgrammableClaySoldierSpawnPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ProgrammableClaySoldierSpawnPayload::entity,
            ClaySoldierChip.STREAM_CODEC, ProgrammableClaySoldierSpawnPayload::module,
            ProgrammableClaySoldierSpawnPayload::new
    );
    private final int entity;
    private final ClaySoldierChip<?> chip;

    public ProgrammableClaySoldierSpawnPayload(int entity, ClaySoldierChip<?> chip) {
        this.entity = entity;
        this.chip = chip;
    }

    public ProgrammableClaySoldierSpawnPayload(ProgrammableClaySoldierEntity entity, ClaySoldierChip<?> chip) {
        this(entity.getId(), chip);
    }


    public int entity() {
        return entity;
    }

    public ClaySoldierChip<?> module() {
        return chip;
    }

    @Override
    public void handleClient(INetworkManger.PayloadContext context) {
        if (context.client().level.getEntity(entity) instanceof ProgrammableClaySoldierEntity programmableClaySoldierEntity) {
            programmableClaySoldierEntity.setupChip(chip.withSoldier(programmableClaySoldierEntity));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
