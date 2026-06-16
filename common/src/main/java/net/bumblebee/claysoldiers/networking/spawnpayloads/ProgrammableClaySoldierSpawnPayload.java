package net.bumblebee.claysoldiers.networking.spawnpayloads;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.networking.IClientPayload;
import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ProgrammableClaySoldierSpawnPayload implements IClientPayload {
    public static final Type<ProgrammableClaySoldierSpawnPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "programmable_soldier_spawn"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgrammableClaySoldierSpawnPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ProgrammableClaySoldierSpawnPayload::entity,
            ClaySoldierChip.STREAM_CODEC, s -> s.chip,
            ItemStack.OPTIONAL_STREAM_CODEC, s -> s.carriedItemStack,
            ByteBufCodecs.BYTE, s -> s.workStatus,
            ProgrammableClaySoldierSpawnPayload::new
    );
    private final int entity;
    private final ClaySoldierChip<?> chip;
    private final ItemStack carriedItemStack;
    private final byte workStatus;

    public ProgrammableClaySoldierSpawnPayload(int entity, @NotNull ClaySoldierChip<?> chip, ItemStack carriedItemStack, byte workStatus) {
        this.entity = entity;
        this.chip = chip;
        this.carriedItemStack = carriedItemStack;
        this.workStatus = workStatus;
    }

    public ProgrammableClaySoldierSpawnPayload(ProgrammableClaySoldierEntity entity, ClaySoldierChip<?> chip) {
        this(entity.getId(), chip, entity.getCarriedStack(), entity.getDataWorkStatus());
    }

    public int entity() {
        return entity;
    }

    @Override
    public void handleClient(NetworkManger.PayloadContext context) {
        if (context.client().level.getEntity(entity) instanceof ProgrammableClaySoldierEntity programmableClaySoldierEntity) {
            programmableClaySoldierEntity.setupChip(chip);
            programmableClaySoldierEntity.setCarriedStack(carriedItemStack);
            programmableClaySoldierEntity.setDataWorkStatus(workStatus);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
