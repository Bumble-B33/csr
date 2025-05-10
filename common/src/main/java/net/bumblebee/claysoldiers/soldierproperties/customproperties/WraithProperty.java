package net.bumblebee.claysoldiers.soldierproperties.customproperties;

import com.google.common.collect.Multimaps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayWraithEntity;
import net.bumblebee.claysoldiers.soldierproperties.combined.ValueCombiner;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttack;
import net.bumblebee.claysoldiers.soldierproperties.translation.ITranslatableProperty;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

public record WraithProperty(int duration, float damage, List<SpecialAttack<?>> attackModifiers) implements ITranslatableProperty {
    public static final WraithProperty EMPTY = new WraithProperty(-1, 0, List.of());
    public static final ToIntFunction<WraithProperty> TO_INT = WraithProperty::duration;
    public static final ValueCombiner<WraithProperty> COMBINER = WraithProperty::combine;
    public static final Codec<WraithProperty> CODEC = RecordCodecBuilder.create(in -> in.group(
            CodecUtils.TIME_CODEC.fieldOf("duration").forGetter(WraithProperty::duration),
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("damage", 0f).forGetter(WraithProperty::damage),
            SpecialAttack.LIST_CODEC.optionalFieldOf("attack_effect", List.of()).forGetter(WraithProperty::attackModifiers)
    ).apply(in, WraithProperty::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, WraithProperty> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WraithProperty::duration,
            ByteBufCodecs.FLOAT, WraithProperty::damage,
            SpecialAttack.STREAM_CODEC.apply(ByteBufCodecs.list()), WraithProperty::attackModifiers,
            WraithProperty::new
    );

    private WraithProperty combine(WraithProperty other) {
        return new WraithProperty(Math.max(duration, other.duration), Math.max(damage, other.damage), SpecialAttack.COMBINER.combine(attackModifiers, other.attackModifiers));
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Wraith: " + duration + "s").withColor(0x03FCDF);
    }

    public Consumer<ClayWraithEntity> onSpawnEffect() {
        return wraith -> {
            wraith.getAttributes().addTransientAttributeModifiers(
                    Multimaps.forMap(Map.of(Attributes.ATTACK_DAMAGE, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "bonus_damage"), damage, AttributeModifier.Operation.ADD_VALUE)))
            );
            wraith.setAttackFunctions(attackModifiers);
        };
    }
}
