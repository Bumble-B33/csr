package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ClayMobItemBreakParticles implements IClientPayload {
    public static final Type<ClayMobItemBreakParticles> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_mob_item_break_particle"));

    private final int entity;
    private final Item stack;

    public ClayMobItemBreakParticles(int entity, Item stack) {
        this.entity = entity;
        this.stack = stack;
    }
    public static final StreamCodec<RegistryFriendlyByteBuf, ClayMobItemBreakParticles> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ClayMobItemBreakParticles::getEntity,
            ByteBufCodecs.registry(Registries.ITEM),
            ClayMobItemBreakParticles::getItem,
            ClayMobItemBreakParticles::new
    );

    public int getEntity() {
        return entity;
    }

    public Item getItem() {
        return stack;
    }

    @Override
    public @NotNull Type<ClayMobItemBreakParticles> type() {
        return ID;
    }

    @Override
    public void handleClient(INetworkManger.PayloadContext context) {
        Level level = context.player().level();
        Entity entity = level.getEntity(getEntity());
        if (entity instanceof ClayMobEntity clayMob) {
            clayMob.spawnItemBreakParticles(getItem().getDefaultInstance(), 5);
        }
    }
}
