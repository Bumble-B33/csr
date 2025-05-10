package net.bumblebee.claysoldiers.blueprint.templates;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BaseImmutableTemplate {
    private static final StreamCodec<ByteBuf, AABB> AABB_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, aabb -> aabb.minX,
            ByteBufCodecs.DOUBLE, aabb -> aabb.minY,
            ByteBufCodecs.DOUBLE, aabb -> aabb.minZ,
            ByteBufCodecs.DOUBLE, aabb -> aabb.maxX,
            ByteBufCodecs.DOUBLE, aabb -> aabb.maxY,
            ByteBufCodecs.DOUBLE, aabb -> aabb.maxZ,
            AABB::new
    );
    private static final StreamCodec<ByteBuf, VoxelShape> VOXEL_SHAPE_STREAM_CODEC = AABB_STREAM_CODEC.apply(ByteBufCodecs.list()).map(
            aabbs -> aabbs.stream().map(Shapes::create).reduce(Shapes.empty(), ((voxelShape, voxelShape2) -> Shapes.joinUnoptimized(voxelShape, voxelShape2, BooleanOp.OR))).optimize(),
            VoxelShape::toAabbs
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BaseImmutableTemplate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(Registries.ITEM), ByteBufCodecs.VAR_INT), BaseImmutableTemplate::getItemCountMap,
            BlockPos.STREAM_CODEC.map(b -> b, BlockPos::new), BaseImmutableTemplate::getSize,
            VOXEL_SHAPE_STREAM_CODEC, BaseImmutableTemplate::getShape,
            BaseImmutableTemplate::new
    );
    protected final Vec3i size;
    private final Map<Item, Integer> itemCountMap;
    private final List<ItemStack> neededItems;
    private final VoxelShape shape;
    private final int totalNeededItems;

    public BaseImmutableTemplate(Map<Item, Integer> itemCountMap, Vec3i size, VoxelShape shape) {
        this.size = size;
        this.itemCountMap = ImmutableMap.copyOf(itemCountMap);
        this.shape = shape;
        this.neededItems = BlueprintUtil.itemMapToList(itemCountMap);
        this.totalNeededItems = itemCountMap.values().stream().reduce(0, Integer::sum);
    }

    public List<ItemStack> getNeededItems() {
        return neededItems;
    }

    @UnmodifiableView
    private Map<Item, Integer> getItemCountMap() {
        return itemCountMap;
    }

    protected int totalNeededItems() {
        return totalNeededItems;
    }

    public Vec3i getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "BaseImmutableTemplate{%s Items(%s): %s}".formatted(size, totalNeededItems, itemCountMap);
    }

    public String toShortString() {
        return "BaseImmutableTemplate{%s Items(%s)}".formatted(size, totalNeededItems);
    }
    /**
     * @return the shape of this Blueprint
     */
    public VoxelShape getShape() {
        return shape;
    }

    public Optional<ServerBlueprintPlan> createServer() {
        return Optional.empty();
    }

    public Optional<BlueprintPlan> createClient() {
        return Optional.of(new ClientBlueprintPlan(new HashMap<>(itemCountMap), this.size));
    }
}
