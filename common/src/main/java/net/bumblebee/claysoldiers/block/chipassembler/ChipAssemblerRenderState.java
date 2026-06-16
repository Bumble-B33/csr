package net.bumblebee.claysoldiers.block.chipassembler;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import java.util.EnumMap;
import java.util.Map;

public class ChipAssemblerRenderState extends BlockEntityRenderState {
    public final Map<ChipAssemblerInventory.Slot, ItemStackRenderState> map;
    public float yRot;
    public long storedEnergy;
    public long maxEnergyStored;
    public float progress;
    public int startProgress;


    public ChipAssemblerRenderState() {
        this.map = new EnumMap<>(ChipAssemblerInventory.Slot.class);
        for (var slot : ChipAssemblerInventory.Slot.values()) {
            map.put(slot, new ItemStackRenderState());
        }
    }
}
