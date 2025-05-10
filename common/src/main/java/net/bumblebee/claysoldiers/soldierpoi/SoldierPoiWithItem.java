package net.bumblebee.claysoldiers.soldierpoi;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claypoifunction.ClayPoiSource;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.networking.ClayMobItemBreakParticles;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;

public class SoldierPoiWithItem extends SoldierPoiWithSource<ItemEntity> {
    public SoldierPoiWithItem(ItemEntity source) {
        super(ClaySoldiersCommon.DATA_MAP.getItemPoi(source.getItem()), source);
    }

    @Override
    protected ClayPoiSource createPoiSource(ItemEntity source) {
        return ClayPoiSource.createSource(source);
    }

    @Override
    public void animateEffect(ClayMobEntity entity) {
        ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingEntity(entity, new ClayMobItemBreakParticles(entity.getId(), getSource().getItem().getItem()));
    }

    @Override
    public void startPath(PathNavigation navigation) {
        var path = navigation.createPath(getSource(), 0);
        navigation.moveTo(path, 1.3d);
    }

    @Override
    public void onUse(ItemEntity source, AbstractClaySoldierEntity soldier) {
        assert getPoi() != null;
        source.setExtendedLifetime();
        float breakChance = getPoi().getBreakChance();
        if (breakChance <= 0) {
            return;
        }
        if (source.getItem().isEmpty()) {
            source.discard();
            return;
        }

        if (source.level().isClientSide()) {
            return;
        }
        if (source.getRandom().nextFloat() <= breakChance) {
            source.getItem().shrink(1);
            take(source, soldier);
            if (source.getItem().isEmpty()) {
                source.discard();
            }
        }
    }

    private void take(ItemEntity pEntity, AbstractClaySoldierEntity soldier) {
        if (!pEntity.isRemoved() && (pEntity.level() instanceof ServerLevel level)) {
            level.getChunkSource().broadcast(pEntity, new ClientboundTakeItemEntityPacket(pEntity.getId(), soldier.getId(), 1));
        }
    }

    @Override
    public String toString() {
        return "Poi{" + getPoi() + ", " + getSource().getItem() + '}';
    }
}
