package net.bumblebee.claysoldiers.soldierproperties.types;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.combined.ValueCombiner;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

public class AttributePropertyType extends SoldierPropertyType<Map<Holder<Attribute>, List<AttributeModifier>>> {
    public static final Codec<Map<Holder<Attribute>, List<AttributeModifier>>> CODEC = Codec.unboundedMap(BuiltInRegistries.ATTRIBUTE.holderByNameCodec(), AttributeModifier.CODEC.listOf());
    public static final StreamCodec<RegistryFriendlyByteBuf, Map<Holder<Attribute>, List<AttributeModifier>>> STREAM_CODEC = ByteBufCodecs.map(
            HashMap::new,
            ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE),
            AttributeModifier.STREAM_CODEC.apply(ByteBufCodecs.list())
    );
    public static final Map<Holder<Attribute>, List<AttributeModifier>> EMPTY = Map.of();
    public static final ToIntFunction<Map<Holder<Attribute>, List<AttributeModifier>>> TO_INT = Map::size;
    public static final ValueCombiner<Map<Holder<Attribute>, List<AttributeModifier>>> COMBINER = (m1, m2) -> {
        Map<Holder<Attribute>, List<AttributeModifier>> newMap = new HashMap<>(Math.max(m1.size(), m2.size()));
        m1.forEach((k, v) -> {
            newMap.put(k, new ArrayList<>(v));
        });

        m2.forEach((k, v) -> newMap.merge(k, v, (l1, l2) -> {
            l1.addAll(l2);
            return l1;
        }));

        return newMap;
    };

    public AttributePropertyType() {
        super(CODEC, STREAM_CODEC, EMPTY, TO_INT, COMBINER, null);
    }

    @Override
    public List<Component> getDisplayNameWithValue(Map<Holder<Attribute>, List<AttributeModifier>> value, @Nullable ClaySoldierInventoryQuery soldier) {
        var name = Component.translatable(this.getDescriptionId()).withStyle(ChatFormatting.DARK_GRAY);
        name.append(":");
        var list = new ArrayList<Component>();
        list.add(name);
        for (var entry : value.entrySet()) {
            combine(entry.getValue()).forEach(m -> addModifierTooltip(list::add, entry.getKey(), m));
        }

        return list;
    }
    private static List<AttributeModifier> combine(List<AttributeModifier> modifiers) {
        List<AttributeModifier> combined = new ArrayList<>();
        modifiers.forEach(mod -> {
            boolean merged = false;
            for (int i = 0; i < combined.size(); i++) {
                var other = combined.get(i);
                if (mod.operation() == other.operation()) {
                    combined.set(i, new AttributeModifier(mod.id(), mod.amount() + other.amount(), mod.operation()));
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                combined.add(mod);
            }
        });
        return combined;
    }

    private void addModifierTooltip(Consumer<Component> pTooltipAdder, Holder<Attribute> pAttribute, AttributeModifier modifier) {
        double amount = modifier.amount();
        double displayAmount;
        if (modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                || modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            displayAmount = amount * 100.0;
        } else if (pAttribute.is(Attributes.KNOCKBACK_RESISTANCE)) {
            displayAmount = amount * 10.0;
        } else {
            displayAmount = amount;
        }

        if (amount > 0.0) {
            pTooltipAdder.accept(
                    Component.literal("  ").append(
                            Component.translatable(
                                            "attribute.modifier.plus." + modifier.operation().id(),
                                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(displayAmount),
                                            Component.translatable(pAttribute.value().getDescriptionId())
                                    )
                                    .withStyle(pAttribute.value().getStyle(true))
                    ));
        } else if (amount < 0.0) {
            pTooltipAdder.accept(
                    Component.literal("  ").append(
                            Component.translatable(
                                            "attribute.modifier.take." + modifier.operation().id(),
                                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-displayAmount),
                                            Component.translatable(pAttribute.value().getDescriptionId())
                                    )
                                    .withStyle(pAttribute.value().getStyle(false))
                    ));
        }
    }
}
