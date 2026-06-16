package net.bumblebee.claysoldiers.blueprint.templates;

import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.blueprint.plan.BlueprintItemCountMap;
import net.bumblebee.claysoldiers.blueprint.plan.BlueprintPlan;
import net.bumblebee.claysoldiers.blueprint.plan.ClientBlueprintPlan;
import net.bumblebee.claysoldiers.blueprint.plan.ServerBlueprintPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
            BlueprintItemCountMap.STREAM_CODEC, s -> s.itemCountMap,
            BlockPos.STREAM_CODEC.map(b -> b, BlockPos::new), BaseImmutableTemplate::getSize,
            VOXEL_SHAPE_STREAM_CODEC, BaseImmutableTemplate::getShape,
            BaseImmutableTemplate::new
    );
    protected final Vec3i size;
    private final BlueprintItemCountMap.Immutable itemCountMap;
    @Nullable
    private List<ItemStack> neededItems;
    private final VoxelShape shape;
    private final int totalNeededItems;

    public BaseImmutableTemplate(BlueprintItemCountMap.Immutable itemCountMap, Vec3i size, VoxelShape shape) {
        this.size = size;
        this.itemCountMap = itemCountMap;
        this.shape = shape;
        this.totalNeededItems = itemCountMap.getNumberOfItems();
    }

    public List<ItemStack> getNeededItems(LevelReader levelReader) {
        if (neededItems == null) {
            this.neededItems = itemCountMap.asList();
        }
        return neededItems;
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

    public Optional<ServerBlueprintPlan> createServer(ServerLevel serverLevel) {
        return Optional.empty();
    }

    public Optional<BlueprintPlan> createClient() {
        return Optional.of(new ClientBlueprintPlan(itemCountMap, this.size));
    }
}
