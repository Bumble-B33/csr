package net.bumblebee.claysoldiers.blueprint.plan;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.blueprint.BlueprintUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class BlueprintBlockInfoList implements Iterable<BlueprintBlockInfo> {
    private static final Codec<Compressed> COMPRESSED_CODEC = RecordCodecBuilder.create(in -> in.group(
            BlockState.CODEC.listOf().fieldOf("pallet").forGetter(s -> s.palletInfos),
            BlockInfo.CODEC.listOf().fieldOf("blocks").forGetter(s -> s.blockInfos)
    ).apply(in, Compressed::new));

    public static final Codec<BlueprintBlockInfoList> CODEC = COMPRESSED_CODEC.xmap(
            Compressed::toInfoList, Compressed::fromInfoList
    );
    private final List<BlueprintBlockInfo> list;

    private BlueprintBlockInfoList(LinkedList<BlueprintBlockInfo> list) {
        this.list = list;
    }

    public static BlueprintBlockInfoList of(List<BlueprintBlockInfo> list) {
        return new BlueprintBlockInfoList(new LinkedList<>(list));
    }

    public static BlueprintBlockInfoList fromStructureInfoList(List<StructureTemplate.StructureBlockInfo> list) {
        return new BlueprintBlockInfoList(list.stream().map(BlueprintBlockInfo::fromInfo)
                .collect(Collectors.toCollection(LinkedList::new)));

    }

    public List<BlueprintBlockInfo> getList() {
        return list;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    @Override
    public @NonNull Iterator<BlueprintBlockInfo> iterator() {
        return list.iterator();
    }

    private record Compressed(List<BlockState> palletInfos, List<BlockInfo> blockInfos) {
        public static Compressed fromInfoList(BlueprintBlockInfoList blockInfoList) {
            BlueprintUtil.SimplePalette simplePalette = new BlueprintUtil.SimplePalette();


            List<BlockInfo> blockInfos = new ArrayList<>(blockInfoList.list.size());

            blockInfoList.forEach(b -> {
                int stateId = simplePalette.idFor(b.getState());
                blockInfos.add(new BlockInfo(b.getPos(), stateId, Optional.ofNullable(b.getNbt())));
            });

            List<BlockState> pallet = new ArrayList<>();
            for (BlockState blockstate : simplePalette) {
                pallet.add(blockstate);
            }
            return new Compressed(pallet, blockInfos);
        }

        public BlueprintBlockInfoList toInfoList() {
            LinkedList<BlueprintBlockInfo> list = new LinkedList<>();

            blockInfos.forEach(b -> list.add(new BlueprintBlockInfo(b.pos(), palletInfos.get(b.state), b.nbt.orElse(null))));

            return new BlueprintBlockInfoList(list);
        }
    }


    private record BlockInfo(BlockPos pos, int state, Optional<CompoundTag> nbt) {
        private static final Codec<BlockInfo> CODEC = RecordCodecBuilder.create(in -> in.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(BlockInfo::pos),
                Codec.INT.fieldOf("state").forGetter(BlockInfo::state),
                CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(BlockInfo::nbt)
        ).apply(in, BlockInfo::new));

    }
}
