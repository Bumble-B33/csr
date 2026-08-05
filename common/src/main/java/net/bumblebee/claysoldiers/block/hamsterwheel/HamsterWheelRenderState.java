package net.bumblebee.claysoldiers.block.hamsterwheel;

import net.bumblebee.claysoldiers.entity.client.FakeClaySoldierAccess;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class HamsterWheelRenderState extends BlockEntityRenderState {
    public static final HamsterWheelRenderState EMPTY = new HamsterWheelRenderState();

    public float rotation;
    public float yRot;
    public boolean hasEnergyStorage;
    public boolean hasSecondBattery;
    public FakeClaySoldierAccess clientClaySoldierEntity;
    public float partialTicks;
    public boolean hasEnergy;
    public int energyStored;
    public int maxEnergyStored;

    public HamsterWheelRenderState() {
        this.rotation = 0;
        this.yRot = 0;
        this.hasEnergyStorage = false;
        this.hasSecondBattery = false;
        this.clientClaySoldierEntity = null;
        this.partialTicks = 0f;
        this.hasEnergy = false;
        this.energyStored = 0;
        this.maxEnergyStored = 0;
    }
}
