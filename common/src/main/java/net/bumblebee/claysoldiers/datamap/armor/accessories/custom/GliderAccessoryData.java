package net.bumblebee.claysoldiers.datamap.armor.accessories.custom;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessoryData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GliderAccessoryData implements SoldierAccessoryData {
    public static final Codec<GliderAccessoryData> CODEC = BuiltInRegistries.ITEM.byNameCodec().xmap(GliderAccessoryData::new, g -> g.glider);
    public static final StreamCodec<RegistryFriendlyByteBuf, GliderAccessoryData> STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM).map(GliderAccessoryData::new, g -> g.glider);

    private final Item glider;
    private final ItemStack gliderStack;

    public GliderAccessoryData(Item glider) {
        this.glider = glider;
        this.gliderStack = glider.getDefaultInstance();
    }

    public ItemStack getGliderStack() {
        return gliderStack;
    }
}
