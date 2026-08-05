package net.bumblebee.claysoldiers.block.chargingpad;

import net.bumblebee.claysoldiers.block.BlockEntityWithEnergy;
import net.bumblebee.claysoldiers.block.chipassembler.ChipAssemblerBlock;
import net.bumblebee.claysoldiers.energy.BatteryProperties;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class SoldierChargingPadBlockEntity extends BlockEntityWithEnergy {
    public SoldierChargingPadBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.SOLDIER_CHARGING_PAD.get(), worldPosition, blockState, BatteryProperties.of(3).allowInsertion().build());
    }

    public int getEnergyForCharging(int wanted) {
        return Math.min(wanted, energyStorage.energyStored());
    }

    public void removeEnergyForCharging(int amount) {
        energyStorage.remove(amount);
    }

    @Override
    public boolean isValidDirectionForEnergy(Direction direction) {
        return getBlockState().getValue(ChipAssemblerBlock.FACING).getOpposite() == direction;
    }
}
