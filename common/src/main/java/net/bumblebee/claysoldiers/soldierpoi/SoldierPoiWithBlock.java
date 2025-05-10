package net.bumblebee.claysoldiers.soldierpoi;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiSource;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.networking.ClayMobItemBreakParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class SoldierPoiWithBlock extends SoldierPoiWithSource<SoldierPoiWithBlock.BlockPoiData> {
    public SoldierPoiWithBlock(Block block, BlockPos pos) {
        this(new BlockPoiData(block, pos));
    }

    private SoldierPoiWithBlock(BlockPoiData data) {
        super(ClaySoldiersCommon.DATA_MAP.getBlockPoi(data.block), data);
    }

    @Override
    protected ClayPoiSource createPoiSource(BlockPoiData source) {
        return ClayPoiSource.createSource(source.block);
    }

    @Override
    public void animateEffect(ClayMobEntity claySoldier) {
        ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingEntity(claySoldier, new ClayMobItemBreakParticles(claySoldier.getId(), getSource().asItem()));
    }

    @Override
    public void startPath(PathNavigation navigation) {
        var path = navigation.createPath(getSource().pos, 0);
        navigation.moveTo(path, 1.3d);
    }

    @Override
    public void onUse(BlockPoiData source, AbstractClaySoldierEntity soldier) {
        assert getPoi() != null;
        float breakChance = getPoi().getBreakChance();
        if (breakChance <= 0) {
            return;
        }

        if (soldier.level().isClientSide()) {
            return;
        }
        if (soldier.getRandom().nextFloat() <= breakChance) {
            soldier.level().destroyBlock(source.pos, false, soldier);
        }
    }

    @Override
    protected boolean stillValid(AbstractClaySoldierEntity soldier) {
        return soldier.level().getBlockState(getSource().pos).is(getSource().block);
    }

    public record BlockPoiData(Block block, BlockPos pos) {
        public Item asItem() {
            return block.asItem();
        }
    }
}
