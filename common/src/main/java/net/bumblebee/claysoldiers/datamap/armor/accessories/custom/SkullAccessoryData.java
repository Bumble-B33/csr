package net.bumblebee.claysoldiers.datamap.armor.accessories.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessoryData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SkullAccessoryData implements SoldierAccessoryData {
    public static final Codec<SkullAccessoryData> CODEC = RecordCodecBuilder.create(in -> in.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(s -> s.headItem),
            ResolvableProfile.CODEC.optionalFieldOf("profile").forGetter(s -> Optional.ofNullable(s.profile))
    ).apply(in, (head, profile) -> new SkullAccessoryData(head, profile.orElse(null))));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkullAccessoryData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), s -> s.headItem,
            ByteBufCodecs.optional(ResolvableProfile.STREAM_CODEC), s -> Optional.ofNullable(s.profile),
            (head, profile) -> new SkullAccessoryData(head, profile.orElse(null))
    );

    private final Item headItem;
    @Nullable
    private ItemStack headItemStack;
    @Nullable
    private final SkullBlock.Type type;
    @Nullable
    private final ResolvableProfile profile;

    public SkullAccessoryData(Item headStack) {
        this(headStack, null);
    }

    public SkullAccessoryData(Item headStack, @Nullable ResolvableProfile profile) {
        this.headItem = headStack;
        this.profile = profile;
        if (headItem instanceof BlockItem blockitem && blockitem.getBlock() instanceof AbstractSkullBlock abstractskullblock) {
            this.type = abstractskullblock.getType();
        } else {
            this.type = null;
        }
    }

    public @Nullable SkullBlock.Type getType() {
        return type;
    }

    public @Nullable ResolvableProfile getProfile() {
        return profile;
    }

    @NotNull
    public ItemStack getHeadStack() {
        if (headItemStack == null) {
            headItemStack = headItem.getDefaultInstance();
        }
        return headItemStack;
    }

}
