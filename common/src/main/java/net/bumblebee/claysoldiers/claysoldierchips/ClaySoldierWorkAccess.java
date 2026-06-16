package net.bumblebee.claysoldiers.claysoldierchips;

import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;

public record ClaySoldierWorkAccess(ProgrammableClaySoldierEntity soldier) implements ClayMobWorkAccess {
    @Override
    public void setDataWorkStatus(byte id) {
        soldier.setDataWorkStatus(id);
    }

    @Override
    public byte getDataWorkStatus() {
        return soldier.getDataWorkStatus();
    }
}