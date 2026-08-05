package net.bumblebee.claysoldiers.block.chipassembler;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChipAssemblerInventory {
    private static final Codec<Map<Slot, ItemStack>> CODEC = Codec.unboundedMap(Slot.CODEC,  ItemStack.CODEC);
    private static final String INVENTORY_TAG = "inventory";
    public static final int MAX_SIZE = Slot.values().length;

    private final Map<Slot, ItemStack> inventory;

    public ChipAssemblerInventory() {
        this.inventory = new EnumMap<>(Slot.class);
        fillWithEmpty();
    }

    public Optional<ItemStack> insert(ItemStack stack, BlockHitResult hitResult, Direction facing) {
        BlockPos hitBlockPos = hitResult.getBlockPos();
        Vec3 relativeHit = hitResult.getLocation().subtract(hitBlockPos.getX(), hitBlockPos.getY(), hitBlockPos.getZ());

        float x;
        float z;

        switch (facing) {
            case EAST -> {
                x = (float) relativeHit.z();
                z = 1f - (float) relativeHit.x();
            }
            case SOUTH -> {
                x = 1f - (float) relativeHit.x();
                z = 1f - (float) relativeHit.z();
            }
            case WEST -> {
                x = 1f - (float) relativeHit.z();
                z = (float) relativeHit.x();
            }
            default -> {
                x = (float) relativeHit.x();
                z = (float) relativeHit.z();
            }
        }

        var slot = getSlotFromClickedPos(x, z);
        if (slot.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(put(slot.orElseThrow(), stack));
    }

    public ItemStack put(Slot slot, ItemStack stack) {
        var res = inventory.get(slot);
        inventory.put(slot, stack);
        return res;
    }

    public void load(ValueInput input) {
        fillWithEmpty();
        input.read(INVENTORY_TAG, CODEC).ifPresent(inventory::putAll);
    }

    public void save(ValueOutput output) {
        output.store(INVENTORY_TAG, CODEC, withoutEmpty());
    }

    public Map<Slot, ItemStack> withoutEmpty() {
        Map<Slot, ItemStack> map = new EnumMap<>(Slot.class);
        inventory.forEach((s, i) -> {
            if (!i.isEmpty())   {
                map.put(s, i);
            }
        });
        return map;
    }

    public void fillWithEmpty() {
        for (Slot slot : Slot.values()) {
            inventory.put(slot, ItemStack.EMPTY);
        }
    }

    public void reduceByOne() {
        for (Slot slot : Slot.values()) {
            ItemStack stack = inventory.get(slot);
            stack.shrink(1);
            inventory.put(slot, stack);
        }
    }

    public void forEach(BiConsumer<Slot, ItemStack> action) {
        inventory.forEach(action);
    }

    public void forEachNonEmpty(Consumer<ItemStack> action) {
        inventory.values().forEach(i -> {
            if (!i.isEmpty()) {
                action.accept(i);
            }
        });
    }

    @NotNull
    public ItemStack get(Slot slot) {
        return inventory.get(slot);
    }

    @Override
    public String toString() {
        return "ChipAssemblerInventory{" + withoutEmpty() +
                '}';
    }

    private static Optional<Slot> getSlotFromClickedPos(float x, float z) {

        for (Slot slot : Slot.values()) {
            if (slot.isInside(x, z)) {
                return Optional.of(slot);
            }
        }

        return Optional.empty();
    }

    public enum Slot implements StringRepresentable {
        CENTER("center",5, 4, 0.295f, 6),
        RF("right_front", 0, 3, 0.32f,4),
        RB("right_back",0, 8, 0.32f,4),
        LF("left_back", 12, 3, 0.32f,4),
        LB("left_front", 12, 8,0.32f, 4);

        private static final Codec<Slot> CODEC = StringRepresentable.fromEnum(Slot::values);

        private final String serializedName;
        private final float minX;
        private final float maxX;
        private final float minZ;
        private final float maxZ;
        private final float scale;
        private final float height;

        Slot(String serializedName, int x, int z, float height, int size) {
            this.serializedName = serializedName;
            this.minX = x / 16f;
            this.maxX = minX + (size / 16f);
            this.minZ = z / 16f;
            this.maxZ = minZ + (size / 16f);
            this.scale = size / 16f;
            this.height = height;
        }

        public float height() {
            return height;
        }

        public float scale() {
            return scale;
        }

        public float x() {
            return (minX + maxX) / 2;
        }

        public float z() {
            return (minZ + maxZ) / 2;
        }

        public boolean isInside(float x, float z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
