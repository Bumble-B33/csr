package net.bumblebee.claysoldiers.networking;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.datamap.armor.SoldierMultiWearable;
import net.bumblebee.claysoldiers.platform.FabricDataMapGetter;
import net.bumblebee.claysoldiers.soldierpoi.SoldierPoi;
import net.bumblebee.claysoldiers.soldierproperties.SoldierVehicleProperties;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DataMapPayloadBuilder<H, T> {
    private static final List<DataMapPayloadBuilder<?, ?>> VALUES = new ArrayList<>();

    public static final DataMapPayloadBuilder<Item, SoldierHoldableEffect> HOLDABLE = register("fabric_holdable", Registries.ITEM, SoldierHoldableEffect.STREAM_CODEC, FabricDataMapGetter::updateSoldierHoldable);
    public static final DataMapPayloadBuilder<Item, SoldierMultiWearable> WEARABLE = register("fabric_wearable", Registries.ITEM, SoldierMultiWearable.STREAM_CODEC, FabricDataMapGetter::updateSoldierWearable);
    public static final DataMapPayloadBuilder<Item, SoldierPoi> ITEM_POI = register("fabric_item_poi", Registries.ITEM, SoldierPoi.STREAM_CODEC, FabricDataMapGetter::updateSoldierItemPoi);
    public static final DataMapPayloadBuilder<Block, SoldierPoi> BLOCK_POI = register("fabric_block_poi", Registries.BLOCK, SoldierPoi.STREAM_CODEC, FabricDataMapGetter::updateSoldierBlockPoi);
    public static final DataMapPayloadBuilder<EntityType<?>, SoldierVehicleProperties> VEHICLE_PROPERTIES = register("fabric_vehicle_properties", Registries.ENTITY_TYPE, SoldierVehicleProperties.STREAM_CODEC, FabricDataMapGetter::updateVehicleProperties);

    private final CustomPacketPayload.Type<Payload> id;
    private final StreamCodec<RegistryFriendlyByteBuf, Payload> streamCodec;
    private final Consumer<Map<Holder<H>, T>> clientEffect;

    private DataMapPayloadBuilder(String name, StreamCodec<RegistryFriendlyByteBuf, Map<Holder<H>, T>> streamCodec, Consumer<Map<Holder<H>, T>> clientEffect) {
        this.id = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, name));
        this.clientEffect = clientEffect;
        this.streamCodec = streamCodec.map(Payload::new, p -> p.map);
    }

    private static <H, T> DataMapPayloadBuilder<H, T> register(String name, ResourceKey<Registry<H>> registry, StreamCodec<RegistryFriendlyByteBuf, T> valueCodec, Consumer<Map<Holder<H>, T>> clientEffect) {
        var v = new DataMapPayloadBuilder<>(name, registry, valueCodec, clientEffect);
        VALUES.add(v);
        return v;
    }

    public DataMapPayloadBuilder(String name, ResourceKey<Registry<H>> registry, StreamCodec<RegistryFriendlyByteBuf, T> valueCodec, Consumer<Map<Holder<H>, T>> clientEffect) {
        this(name, ByteBufCodecs.map(HashMap::new, ByteBufCodecs.holderRegistry(registry), valueCodec), clientEffect);
    }

    public static void registerAll() {
        VALUES.forEach(DataMapPayloadBuilder::register);
    }
    public static void registerAllReceiver() {
        VALUES.forEach(DataMapPayloadBuilder::registerReceiver);
    }

    private void register() {
        PayloadTypeRegistry.playS2C().register(id, streamCodec);
    }
    public void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(id, Payload::handleClient);
    }

    public CustomPacketPayload create(Map<Holder<H>, T> map) {
       return new Payload(map);
    }

    public class Payload implements CustomPacketPayload {
        private final Map<Holder<H>, T> map;

        public Payload(Map<Holder<H>, T> map) {
            this.map = map;
        }

        public void handleClient(ClientPlayNetworking.Context context) {
            context.client().execute(() -> clientEffect.accept(map));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return id;
        }
    }
}
