package net.bumblebee.claysoldiers.networking.spawnpayloads;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.common.inventory.ClaySoldierInventory;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.networking.IClientPayload;
import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.revive.ReviveType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClaySoldierSpawnPayload implements IClientPayload {
    public static final Type<ClaySoldierSpawnPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier_spawn"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaySoldierSpawnPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ClaySoldierSpawnPayload decode(RegistryFriendlyByteBuf buffer) {
            return new ClaySoldierSpawnPayload(
                    ByteBufCodecs.INT.decode(buffer),
                    ClaySoldierInventory.STREAM_CODEC.decode(buffer),
                    ReviveType.INT_LIST_STREAM_CODEC.decode(buffer),
                    buffer.readInt()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ClaySoldierSpawnPayload payload) {
            ByteBufCodecs.INT.encode(buffer, payload.getEntityId());
            ClaySoldierInventory.STREAM_CODEC.encode(buffer, payload.inventory);
            ReviveType.INT_LIST_STREAM_CODEC.encode(buffer, payload.reviveCooldowns);
            buffer.writeInt(payload.skinId);
        }
    };

    private final int entityId;
    private final Map<SoldierEquipmentSlot, ItemStack> inventory;
    private final List<Integer> reviveCooldowns;
    private final int skinId;

    protected ClaySoldierSpawnPayload(int entity, Map<SoldierEquipmentSlot, ItemStack> inventory, List<Integer> reviveCooldowns, int skinId) {
        this.entityId = entity;
        this.inventory = inventory;
        this.reviveCooldowns = reviveCooldowns;
        this.skinId = skinId;
    }

    public ClaySoldierSpawnPayload(AbstractClaySoldierEntity claySoldier) {
        this.entityId = claySoldier.getId();
        List<Integer> cooldowns = new ArrayList<>(ReviveType.values().length);
        for (ReviveType type : ReviveType.values()) {
            cooldowns.add(Objects.requireNonNullElse(claySoldier.getReviveTypeCooldown().get(type), -1));
        }
        this.reviveCooldowns = cooldowns;
        this.skinId = claySoldier.getSkinVariant();
        this.inventory = claySoldier.getInventory().asMap();

    }

    @Override
    public void handleClient(NetworkManger.PayloadContext context) {
        var entity = context.client().level.getEntity(entityId);
        if (entity instanceof AbstractClaySoldierEntity soldier) {
            soldier.handleSpawnPayload(this);
        }
    }

    public Map<SoldierEquipmentSlot, ItemStack> getInventory() {
        return inventory;
    }

    public List<Integer> getReviveCooldowns() {
        return reviveCooldowns;
    }

    public int getSkinId() {
        return skinId;
    }

    protected int getEntityId() {
        return entityId;
    }


    @Override
    public Type<? extends ClaySoldierSpawnPayload> type() {
        return ID;
    }
}
