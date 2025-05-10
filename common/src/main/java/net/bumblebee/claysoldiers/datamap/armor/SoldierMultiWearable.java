package net.bumblebee.claysoldiers.datamap.armor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.armor.accessories.RenderableAccessory;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessorySlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class SoldierMultiWearable {
    public static final Codec<SoldierMultiWearable> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.unboundedMap(SoldierEquipmentSlot.CODEC, SoldierWearableEffect.CODEC).optionalFieldOf("armor", Map.of()).forGetter(m -> m.armorItemSlotMap),
            SoldierAccessorySlot.MAP_CODEC.optionalFieldOf("accessories", Map.of()).forGetter(m -> m.accessories)
    ).apply(in, SoldierMultiWearable::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SoldierMultiWearable> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(i -> new EnumMap<>(SoldierEquipmentSlot.class), SoldierEquipmentSlot.STREAM_CODEC, SoldierWearableEffect.STREAM_CODEC, SoldierEquipmentSlot.values().length), m -> m.armorItemSlotMap,
            SoldierAccessorySlot.MAP_STREAM_CODEC, m -> m.accessories,
            SoldierMultiWearable::new
    );

    private final EnumMap<SoldierEquipmentSlot, SoldierWearableEffect> armorItemSlotMap;
    private final Map<SoldierAccessorySlot<?>, RenderableAccessory> accessories;

    private SoldierMultiWearable(Map<SoldierEquipmentSlot, SoldierWearableEffect> map, Map<SoldierAccessorySlot<?>, RenderableAccessory> accessories) {
        this.armorItemSlotMap = map.isEmpty() ? new EnumMap<>(SoldierEquipmentSlot.class) : new EnumMap<>(map);
        this.accessories = accessories;
    }

    @Nullable
    public SoldierWearableEffect wearableEffect(SoldierEquipmentSlot slot) {
        return armorItemSlotMap.get(slot);
    }

    public Map<SoldierAccessorySlot<?>, RenderableAccessory> getAccessories() {
        return accessories;
    }

    @Override
    public String toString() {
        if (armorItemSlotMap.isEmpty()) {
            if (accessories.isEmpty()) {
                return "SoldierMultiWearable{Empty}";
            } else {
                return "SoldierMultiWearable{Accessories: %s}".formatted(accessories.keySet());
            }
        } else {
            if (accessories.isEmpty()) {
                return "SoldierMultiWearable{Armor: %s}".formatted(armorItemSlotMap);
            } else {
                return "SoldierMultiWearable{Armor: %s, Accessories: %s}".formatted(armorItemSlotMap, accessories.values());
            }
        }
    }

    public boolean isEmpty() {
        return accessories.isEmpty() && armorItemSlotMap.isEmpty();
    }

    public static SoldierMultiWearable empty() {
        return new SoldierMultiWearable(new EnumMap<>(SoldierEquipmentSlot.class), Map.of());
    }
    public static Builder of() {
        return new Builder();
    }
    public static SoldierMultiWearable single(SoldierEquipmentSlot slot, SoldierWearableEffect effect) {
        return of().put(slot, effect).build();
    }
    public static <T extends RenderableAccessory> SoldierMultiWearable accessory(SoldierAccessorySlot<T> key, T value) {
        return of().put(key, value).build();
    }

    public static class Builder {
        private final Map<SoldierEquipmentSlot, SoldierWearableEffect> map = new EnumMap<>(SoldierEquipmentSlot.class);
        private final Map<SoldierAccessorySlot<?>, RenderableAccessory> accessories = new HashMap<>();

        public Builder put(SoldierEquipmentSlot key, SoldierWearableEffect value) {
            map.put(key, value);
            return this;
        }
        public <T extends RenderableAccessory>Builder put(SoldierAccessorySlot<T> key, T value) {
            accessories.put(key, value);
            return this;
        }

        public SoldierMultiWearable build() {
            return new SoldierMultiWearable(map, accessories);
        }
        public int wearableSize() {
            return map.size();
        }
    }
}
