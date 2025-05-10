package net.bumblebee.claysoldiers.networking.spawnpayloads;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.networking.IClientPayload;
import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.revive.ReviveType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClaySoldierSpawnPayload implements IClientPayload {
    public static final Type<ClaySoldierSpawnPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_spawn"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaySoldierSpawnPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ClaySoldierSpawnPayload decode(RegistryFriendlyByteBuf buffer) {
            return new ClaySoldierSpawnPayload(
                    ByteBufCodecs.INT.decode(buffer),
                    ResourceLocation.STREAM_CODEC.decode(buffer),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),
                    ReviveType.INT_LIST_STREAM_CODEC.decode(buffer),
                    buffer.readInt(),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),
                    buffer.readByte()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ClaySoldierSpawnPayload payload) {
            ByteBufCodecs.INT.encode(buffer, payload.getEntityId());
            ResourceLocation.STREAM_CODEC.encode(buffer, payload.getTeamId());

            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, payload.cape);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, payload.backpack1);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, payload.backpack2);

            ReviveType.INT_LIST_STREAM_CODEC.encode(buffer, payload.reviveCooldowns);

            buffer.writeInt(payload.skinId);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, payload.carried);
            buffer.writeByte(payload.workStatus);
        }
    };

    private final int entityId;
    private final ResourceLocation teamId;
    private final ItemStack cape;
    private final ItemStack backpack1;
    private final ItemStack backpack2;
    private final List<Integer> reviveCooldowns;
    private final int skinId;
    private final ItemStack carried;
    private final byte workStatus;

    protected ClaySoldierSpawnPayload(int entity, ResourceLocation teamId, ItemStack cape, ItemStack backpack1, ItemStack backpack2, List<Integer> reviveCooldowns, int skinId, ItemStack carried, byte workStatus) {
        this.entityId = entity;
        this.teamId = teamId;
        this.cape = cape;
        this.backpack1 = backpack1;
        this.backpack2 = backpack2;
        this.reviveCooldowns = reviveCooldowns;
        this.skinId = skinId;
        this.carried = carried;
        this.workStatus = workStatus;
    }

    public ClaySoldierSpawnPayload(AbstractClaySoldierEntity claySoldier) {
        this.entityId = claySoldier.getId();
        this.teamId = claySoldier.getClayTeamType();
        this.cape = claySoldier.getItemBySlot(SoldierEquipmentSlot.CAPE).stack();
        this.backpack1 = claySoldier.getItemBySlot(SoldierEquipmentSlot.BACKPACK).stack();
        this.backpack2 = claySoldier.getItemBySlot(SoldierEquipmentSlot.BACKPACK_PASSIVE).stack();
        List<Integer> cooldowns = new ArrayList<>(ReviveType.values().length);
        for (ReviveType type : ReviveType.values()) {
            cooldowns.add(Objects.requireNonNullElse(claySoldier.getReviveTypeCooldown().get(type), -1));
        }
        this.reviveCooldowns = cooldowns;
        this.skinId = claySoldier.getSkinVariant();
        this.carried = claySoldier.getCarriedStack();
        this.workStatus = claySoldier.getDataWorkStatus();

    }

    @Override
    public void handleClient(INetworkManger.PayloadContext context) {
        var entity = context.client().level.getEntity(entityId);
        if (entity instanceof AbstractClaySoldierEntity soldier) {
            soldier.handleSpawnPayload(this);
        }
    }

    public ItemStack getCape() {
        return cape;
    }

    public ItemStack getBackpack1() {
        return backpack1;
    }

    public ItemStack getBackpack2() {
        return backpack2;
    }

    public List<Integer> getReviveCooldowns() {
        return reviveCooldowns;
    }

    public int getSkinId() {
        return skinId;
    }

    public ItemStack getCarried() {
        return carried;
    }

    public byte getWorkStatus() {
        return workStatus;
    }
    protected int getEntityId() {
        return entityId;
    }

    protected ResourceLocation getTeamId() {
        return teamId;
    }

    @Override
    public Type<? extends ClaySoldierSpawnPayload> type() {
        return ID;
    }
}
