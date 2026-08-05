package net.bumblebee.claysoldiers.claysoldierchips.addon;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ClaySoldierChipAddon implements ItemLike, DataComponentGetter {
    public static final Codec<ClaySoldierChipAddon> CODEC = ModRegistries.CLAY_SOLDIER_CHIP_ADDONS_REGISTRY.byNameCodec();
    public static final Codec<List<ClaySoldierChipAddon>> LIST_CODEC = CODEC.listOf();

    public static final StreamCodec<RegistryFriendlyByteBuf, ClaySoldierChipAddon> STREAM_CODEC = ByteBufCodecs.registry(ModRegistries.CLAY_SOLDIER_CHIP_ADDONS);
    public static final StreamCodec<RegistryFriendlyByteBuf, List<ClaySoldierChipAddon>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final Map<ClaySoldierChipAddon, Item> BY_ITEM = new ConcurrentHashMap<>();

    private @Nullable String descriptionId;
    private Holder.@Nullable Reference<ClaySoldierChipAddon> holder;
    private final String name;
    private @Nullable Item item;
    private @Nullable DataComponentMap dataComponents;
    private final Supplier<DataComponentMap> builder;

    public ClaySoldierChipAddon(String name) {
        this.name = name;

        this.dataComponents = DataComponentMap.EMPTY;
        this.builder = null;
    }

    public ClaySoldierChipAddon(String name, @NonNull Supplier<DataComponentMap> builder) {
        this.name = name;
        this.builder = builder;
    }

    private Holder.Reference<ClaySoldierChipAddon> getOrCreateReference() {
        if (holder == null) {
            holder = ModRegistries.CLAY_SOLDIER_CHIP_ADDONS_REGISTRY.get(
                    ModRegistries.CLAY_SOLDIER_CHIP_ADDONS_REGISTRY.getKey(this)
            ).orElseThrow();
        }
        return holder;
    }

    public Component getDisplayName() {
        return Component.translatable(getDescriptionId());
    }

    public String getDescriptionId() {
        if (descriptionId == null) {
            descriptionId = Util.makeDescriptionId("clay_soldier_addon", getOrCreateReference().key().identifier());
        }
        return descriptionId;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this != obj && obj instanceof ClaySoldierChipAddon ad && ad.name.equals(name)) {
            ClaySoldiersCommon.ERROR_HANDLER.warn("Addons should be equal: " + this + ", " + obj);
        }
        return this == obj;
    }

    @Override
    public String toString() {
        return "ClaySoldierAddon[%s]".formatted(name);
    }

    @Override
    public @NonNull Item asItem() {
        if (item == null) {
            item = BY_ITEM.remove(this);
        }
        return item;
    }

    @Override
    public @Nullable <T> T get(@NonNull DataComponentType<? extends T> dataComponentType) {
        if (dataComponents == null) {
            dataComponents = builder.get();
        }
        return dataComponents.get(dataComponentType);
    }
}
