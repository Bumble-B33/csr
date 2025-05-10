package net.bumblebee.claysoldiers.item.claypouch;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.blueprint.templates.BlueprintUtil;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.item.claymobspawn.MultiSpawnItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class ClayPouchContent implements TooltipComponent {
    public static final int MAX_CAPACITY = 256;
    public static final Codec<ClayPouchContent> CODEC = RecordCodecBuilder.create(in -> in.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("doll").forGetter(s -> s.item),
            Codec.intRange(0, MAX_CAPACITY).fieldOf("count").forGetter(s -> s.count),
            DataComponentMap.CODEC.optionalFieldOf("data", DataComponentMap.EMPTY).forGetter(s -> s.data)
    ).apply(in, ClayPouchContent::createUnsafe));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClayPouchContent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), s -> s.item,
            ByteBufCodecs.VAR_INT, s -> s.count,
            ByteBufCodecs.optional(ByteBufCodecs.fromCodec(DataComponentMap.CODEC)), ClayPouchContent::dataComponents,
            (item, count, opt) -> ClayPouchContent.createUnsafe(item, count, opt.orElse(DataComponentMap.EMPTY))
    );

    private final MultiSpawnItem<?> item;
    private final int count;
    private final DataComponentMap data;

    public ClayPouchContent(MultiSpawnItem<?> item, int count, DataComponentMap map) {
        this.item = item;
        this.count = count;
        this.data = map;
    }

    private static ClayPouchContent createUnsafe(Item item, int count, DataComponentMap map) {
        try {
            return new ClayPouchContent((MultiSpawnItem<?>) item, count, map);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("%s does not extend %s".formatted(item, MultiSpawnItem.class.getSimpleName()));
        }
    }

    public int getColor(LivingEntity player) {
        return item.getPouchColor(data, player);
    }

    public int maxRemaining() {
        return MAX_CAPACITY - count;
    }

    /**
     * Returns a value between [1 - 13]
     */
    public int getFillPercent() {
        return (int) Math.clamp(((count * 13f) / MAX_CAPACITY), 1, 13);
    }

    public int getCount() {
        return count;
    }

    public ClayPouchContent increment(int grow) {
        return new ClayPouchContent(item, this.count + grow, data);
    }

    public @Nullable ClayPouchContent shrink(int amount) {
        if (amount >= count) {
            return null;
        }
        return increment(-amount);
    }

    public boolean isFor(ItemStack stack) {
        if (stack.getItem() != item) {
            return false;
        }
        return data.stream().allMatch(type -> type.value().equals(stack.get(type.type())));
    }

    public MultiSpawnItem<?> getItem() {
        return item;
    }

    public @Nullable ClayPouchContent takeStack(Consumer<ItemStack> taken, HolderLookup.Provider registries) {
        int used = Math.min(count, item.getDefaultMaxStackSize());
        ItemStack stack = createStack(registries);
        stack.setCount(used);
        dataComponents().ifPresent(stack::applyComponents);
        taken.accept(stack);

        return shrink(used);
    }

    public ItemStack createStack(HolderLookup.Provider registries) {
        return item.recreateStackFromPouch(data, registries);
    }

    /**
     * Inserts the give {@code ItemStack} into this Pouch
     * @param stack the {@code ItemStack} to insert.
     * @param onSuccess called when the {@code ItemStack} got successfully inserted with the new {@code ClayPouchContent} and the inserted amount
     */
    public void insert(ItemStack stack, BiConsumer<ClayPouchContent, Integer> onSuccess) {
        if (isFor(stack)) {
            int inserted = Math.min(stack.getCount(), maxRemaining());
            onSuccess.accept(increment(inserted), inserted);
        }
    }

    public Iterable<ItemStack> copyItems() {
        List<ItemStack> stacks = new ArrayList<>();
        BlueprintUtil.addItemStackToList(stacks, item, count);
        return stacks;
    }

    public Optional<DataComponentMap> dataComponents() {
        return data == DataComponentMap.EMPTY ? Optional.empty() : Optional.of(data);
    }

    public static boolean onPouch(ItemStack stack, UnaryOperator<ClayPouchContent> operation) {
        var content = stack.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
        if (content != null) {
            stack.set(ModDataComponents.CLAY_POUCH_CONTENT.get(), operation.apply(content));
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ClayPouchContent content = (ClayPouchContent) o;
        return count == content.count && Objects.equals(item, content.item) && Objects.equals(data, content.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, count, data);
    }

    @Override
    public String toString() {
        return "PouchContent(%s: %d)".formatted(item, count);
    }
}
