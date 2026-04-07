package net.bumblebee.claysoldiers.entity.common.inventory;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackEffectHolder;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class ClaySoldierInventory {
    public static final String INVENTORY_TAG = "clay_soldier_invetory";
    public static final EntityEquipment EMPTY_ENTITY_EQUIPMENT = new EmptyEntityEquipment();
    public static StreamCodec<RegistryFriendlyByteBuf, Map<SoldierEquipmentSlot, ItemStack>> STREAM_CODEC = ByteBufCodecs.map(
            i -> new EnumMap<>(SoldierEquipmentSlot.class),
            SoldierEquipmentSlot.STREAM_CODEC,
            ItemStack.STREAM_CODEC,
            SoldierEquipmentSlot.values().length
    );
    private final Codec<Map<SoldierEquipmentSlot, ItemStackWithEffect>> CODEC = Codec.unboundedMap(SoldierEquipmentSlot.CODEC, ItemStackWithEffect.CODEC);

    private final Map<SoldierEquipmentSlot, ItemStackWithEffect> inventory;

    public ClaySoldierInventory() {
        this(new EnumMap<>(SoldierEquipmentSlot.class));
    }

    private ClaySoldierInventory(Map<SoldierEquipmentSlot, ItemStackWithEffect> inventory) {
        this.inventory = inventory;
    }

    public void save(ValueOutput output) {
        inventory.values().removeIf(ItemStackEffectHolder::isEmpty);
        if (inventory.isEmpty()) {
            return;
        }
        output.store(INVENTORY_TAG, CODEC, inventory);
    }

    public void load(ValueInput input) {
        input.read(INVENTORY_TAG, CODEC).ifPresent(inventory::putAll);
    }

    public void setItemSlot(SoldierEquipmentSlot slot, ItemStackWithEffect stack) {
        Objects.requireNonNull(stack, "Stack cannot be null");
        this.inventory.put(slot, stack);
    }

    public ItemStackWithEffect getItemBySlot(SoldierEquipmentSlot slot) {
        return inventory.getOrDefault(slot, ItemStackWithEffect.EMPTY);
    }

    public Iterable<ItemStack> getAllSlotsAsStacks() {
        return inventory.values().stream().map(ItemStackWithEffect::stack).toList();
    }

    public Iterable<ItemStackWithEffect> getAllSlots() {
        return inventory.values();
    }

    public Map<SoldierEquipmentSlot, ItemStack> asMap() {
        var map = new EnumMap<SoldierEquipmentSlot, ItemStack>(SoldierEquipmentSlot.class);
        inventory.forEach((slot, stack) -> {
            if (stack != null) {
                map.put(slot, stack.stack());
            }
        });
        return map;
    }

    // Old

    @Deprecated
    public ItemStack get(EquipmentSlot slot) {
        var opt = SoldierEquipmentSlot.getFromSlot(slot);
        if (opt.isPresent()) {
            return inventory.getOrDefault(opt.orElseThrow(), ItemStackWithEffect.EMPTY).stack();
        }
        return ItemStack.EMPTY;
    }

    @Deprecated
    public void set(EquipmentSlot slot, ItemStack stack) {
        SoldierEquipmentSlot.getFromSlot(slot).ifPresent(s -> setItemSlot(s, new ItemStackWithEffect(stack)));
    }

    public void dropInventory(ServerLevel level, BiConsumer<SoldierEquipmentSlot, ItemStack> dropInWorld) {
        if (!level.getGameRules().getBoolean(ClaySoldiersCommon.CLAY_SOLDIER_INVENTORY_DROP_RULE)) {
            return;
        }
        for (SoldierEquipmentSlot slot : SoldierEquipmentSlot.values()) {
            var stack = inventory.get(slot);
            if (stack == null) {
                continue;
            }
            if (!stack.isEmpty() && !EnchantmentHelper.has(stack.stack(), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP) && level.getRandom().nextFloat() < stack.dropRate()) {
                dropInWorld.accept(slot, stack.stack());
            }
        }
    }

    private static class EmptyEntityEquipment extends EntityEquipment {
        public EmptyEntityEquipment() {
            super();
        }

        @Override
        public ItemStack set(EquipmentSlot slot, ItemStack stack) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack get(EquipmentSlot slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void tick(Entity entity) {
        }

        @Override
        public void setAll(EntityEquipment equipment) {
        }

        @Override
        public void dropAll(LivingEntity entity) {
        }

        @Override
        public void clear() {
        }
    }
}
