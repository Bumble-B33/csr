package net.bumblebee.claysoldiers.claysoldierchips.addon;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClaySoldierChipAddon {
    public static final Codec<ClaySoldierChipAddon> CODEC = ModRegistries.CLAY_SOLDIER_CHIP_ADDONS_REGISTRY.byNameCodec();
    public static final Codec<List<ClaySoldierChipAddon>> LIST_CODEC = CODEC.listOf();

    public static final StreamCodec<RegistryFriendlyByteBuf, ClaySoldierChipAddon> STREAM_CODEC = ByteBufCodecs.registry(ModRegistries.CLAY_SOLDIER_CHIP_ADDONS);
    public static final StreamCodec<RegistryFriendlyByteBuf, List<ClaySoldierChipAddon>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

    @Nullable
    private String descriptionId;
    @Nullable
    private Holder.Reference<ClaySoldierChipAddon> holder;
    private final String name;

    public ClaySoldierChipAddon(String name) {
        this.name = name;
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
            descriptionId = Util.makeDescriptionId("clay_soldier_module", getOrCreateReference().key().identifier());
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
            ClaySoldiersCommon.ERROR_HANDLER.warn("Addons should be equal:" + this + ", " + obj);
        }
        return this == obj;
    }

    @Override
    public String toString() {
        return "ClaySoldierAddon[%s]".formatted(name);
    }
}
