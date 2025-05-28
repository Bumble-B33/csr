package net.bumblebee.claysoldiers.platform.services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.block.hamsterwheel.IHamsterWheelEnergyStorage;
import net.bumblebee.claysoldiers.capability.*;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public abstract class AbstractCapabilityManger extends SimpleJsonResourceReloadListener {
    public static final String PATH = "clay_soldiers";
    public static final String FILE_NAME = "capabilities";
    private static final EnumMap<Types, Map<Item, EnabledHolder>> ENABLED_MAP = new EnumMap<>(Types.class);
    private static final Logger LOGGER = ClaySoldiersCommon.LOGGER;
    private static final Codec<Boolean> ENABLED_CODEC = Codec.STRING.comapFlatMap(s -> {
        if (s.equals("enabled")) {
            return DataResult.success(true);
        } else if (s.equals("disabled")) {
            return DataResult.success(false);
        } else {
            return DataResult.error(() -> "Cannot parse %s as enabled/disabled".formatted(s));
        }
    }, b -> b ? "enabled" : "disabled");
    private static final Decoder<Map<Types, Map<Item, Boolean>>> DECODER = Codec.unboundedMap(Types.CODEC, Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), ENABLED_CODEC));

    protected AbstractCapabilityManger() {
        super(new Gson(), PATH);
    }

    @Nullable
    public BiFunction<ItemStack, SoldierHoldableEffect, CustomEquipCapability> getCustomEquip(ItemStack stack) {
        return ifEnabledOrNull(Types.EQUIP, stack, CustomEquipCapability.CUSTOM_EQUIP_MAP.get(stack.getItem()));
    }

    @Nullable
    public BiFunction<ItemStack, @Nullable SoldierHoldableEffect, ThrowableItemCapability> getThrowableItem(ItemStack stack) {
        return ifEnabledOrNull(Types.THROW, stack, ThrowableItemCapability.THROWABLE_ITEM_MAP.get(stack.getItem()));
    }

    public abstract IHamsterWheelEnergyStorage createEnergyStorage(HamsterWheelBlockEntity hamsterWheelBlockEntity);

    private <T> @Nullable T ifEnabledOrNull(Types type, ItemStack stack, T cap) {
        return Objects.requireNonNullElse(ENABLED_MAP.get(type).get(stack.getItem()), new EnabledHolder()).isEnabled() ? cap : null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        ENABLED_MAP.forEach((type, status) -> {
            builder.append(type.serializedName);
            builder.append("{ Enabled: ");
            builder.append(status.entrySet().stream().filter(entry -> entry.getValue().isEnabled()).map(Map.Entry::getKey).toList());
            builder.append(", Disabled: ");
            builder.append(status.entrySet().stream().filter(entry -> entry.getValue().isDisabled()).map(Map.Entry::getKey).toList());
            builder.append("} ");
        });
        return builder.toString();
    }

    public abstract IBlockCache<IBlockStorageAccess> create(ServerLevel level, BlockPos pos);

    public abstract IBlockCache<BlueprintRequestHandler> createBlueprint(ServerLevel level, BlockPos pos);

    public abstract IBlockCache<AssignablePoiCapability> createPoiCache(ServerLevel level, BlockPos pos);


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        registerCapabilities();
        jsonElementMap.forEach((path, jsonElement) -> {
            if (path.getPath().equals(FILE_NAME)) {
                DECODER.parse(JsonOps.INSTANCE, jsonElement)
                        .ifSuccess(AbstractCapabilityManger::updateCapabilityStatus)
                        .ifError(err -> LOGGER.error(err.message()));
            }
        });
        finalizeCapStatuses(false);
    }

    public static void setForClient(Map<Types, Map<Item, Boolean>> map) {
        ENABLED_MAP.putAll(Map.of(
                Types.THROW, new HashMap<>(), Types.EQUIP, new HashMap<>()
        ));
        map.forEach(((types, itemBooleanMap) -> {
            Map<Item, EnabledHolder> typeMap = ENABLED_MAP.get(types);
            itemBooleanMap.forEach((item, enabled) -> typeMap.put(item, EnabledHolder.createImmutable(enabled)));
        }));
        finalizeCapStatuses(true);
    }

    private static void finalizeCapStatuses(boolean client) {

        ENABLED_MAP.forEach((type, status) -> {
            status.values().forEach(EnabledHolder::makeImmutable);

            LOGGER.info("Disabled {} Capabilities on {} for: {}", type.serializedName, (client ? "Client" : "Server"), status.entrySet().stream().filter(entry -> entry.getValue().isDisabled()).map(Map.Entry::getKey).toList());
        });
    }

    public static Map<Types, Map<Item, Boolean>> getEnabledMap() {
        EnumMap<Types, Map<Item, Boolean>> map = new EnumMap<>(Types.class);

        ENABLED_MAP.forEach((type, enMap) -> {
            HashMap<Item, Boolean> enabledMap = new HashMap<>(enMap.size());
            enMap.forEach(((item, enabledHolder) -> enabledMap.put(item, enabledHolder.isEnabled())));
            map.put(type, enabledMap);
        });

        return map;
    }

    private static void registerCapabilities() {
        for (Types type : Types.values()) {
            ENABLED_MAP.put(type, new HashMap<>());
        }
        var equipMap = ENABLED_MAP.get(Types.EQUIP);
        CustomEquipCapability.CUSTOM_EQUIP_MAP.keySet().forEach(itemSupplier -> equipMap.put(itemSupplier.asItem(), new EnabledHolder()));


        var throwMap = ENABLED_MAP.get(Types.THROW);
        ThrowableItemCapability.THROWABLE_ITEM_MAP.keySet().forEach(itemSupplier -> throwMap.put(itemSupplier.asItem(), new EnabledHolder()));
    }

    private static void updateCapabilityStatus(Map<Types, Map<Item, Boolean>> enabledMap) {
        enabledMap.forEach((type, newMap) -> {
            var map = ENABLED_MAP.get(type);
            newMap.forEach((item, enabledBool) -> {
                if (map.containsKey(item)) {
                    map.get(item).and(enabledBool);
                } else {
                    LOGGER.error("Tried to {} Capability for an {}, but {} has no Capability", (enabledBool ? "enable" : "disable"), item, item);
                }
            });
        });
    }

    public enum Types implements StringRepresentable {
        EQUIP("equip"),
        THROW("throwable");

        private static final Codec<Types> CODEC = StringRepresentable.fromEnum(Types::values);
        private final String serializedName;

        Types(String serializedName) {
            this.serializedName = serializedName;
        }


        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    private static class EnabledHolder {
        private boolean enabled = true;
        private boolean immutable = false;

        public void and(boolean enabled) {
            if (immutable) {
                throw new UnsupportedOperationException("Cannot change the value of an Immutable EnabledHolder");
            }
            this.enabled = this.enabled && enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isDisabled() {
            return !enabled;
        }

        public static EnabledHolder createImmutable(boolean enabled) {
            EnabledHolder enabledHolder = new EnabledHolder();
            enabledHolder.enabled = enabled;
            enabledHolder.makeImmutable();
            return enabledHolder;
        }

        public void makeImmutable() {
            immutable = true;
        }
    }
}
