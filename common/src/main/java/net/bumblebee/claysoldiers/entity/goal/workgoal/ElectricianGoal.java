package net.bumblebee.claysoldiers.entity.goal.workgoal;

import com.google.common.primitives.Ints;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.capability.EnergyCapability;
import net.bumblebee.claysoldiers.capability.IBlockCache;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModPoiTypes;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.util.PoiPosInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class ElectricianGoal extends AbstractWorkGoal {
    public static final String ELECTRICIAN_LANG = JOB_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "electrician");
    public static final int DELAY_BETWEEN_INSERTION = 5;
    public static final int SEARCH_RADIUS = 16;

    private final int maxInsert;
    private int delay = 0;
    @Nullable
    private Action action = null;

    @Nullable
    private IBlockCache<EnergyCapability> insertBlock = null;

    public ElectricianGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess, int maxInsert) {
        super(soldier, workAccess);
        if (maxInsert < 0) {
            ClaySoldiersCommon.ERROR_HANDLER.error("Acceleration cannot be negative");
            this.maxInsert = 1;
        } else {
            this.maxInsert = maxInsert;

        }

    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(ELECTRICIAN_LANG);
    }

    @Override
    public void tick() {
        if (isOnBreak()) {
            return;
        }

        if (delay > 0) {
            delay--;
            return;
        }


        if (soldier.getCarriedStack().isEmpty()) {
            takeAShortBreak(false);
            workStatus.setNeedsBattery();
            return;
        }

        if (action == null) {
            int energy = currentEnergyStored();
            if (energy <= 0) {
                action = Action.EXTRACTING;
            } else {
                action = Action.INSERTING;
            }
            return;
        }


        if (action == Action.EXTRACTING) {
            BlockPos pos = findChargingPad().orElse(null);
            if (pos == null) {
                workStatus.setNoChargingPad();
                return;
            }

            workStatus.setInserting();
            if (!extract(pos)) {
                action = null;
            }
        } else if (action == Action.INSERTING) {
            PoiPosInfo poiInfo = getPoiInfo();
            if (poiInfo.isEmpty() && poiInfo.side() == null) {
                takeAShortBreak(false);
                workStatus.setNothingToCharge();
                return;
            }
            workStatus.setInserting();
            if (!insert(poiInfo)) {
                action = null;
            }
        }
    }

    private boolean insert(@NonNull PoiPosInfo poiInfo) {
        if (!moveToPoi(0.7d)) {
            return true;
        }

        if (insertBlock == null) {
            insertBlock = getEnergyCache(poiInfo.getPos(), poiInfo.side());
            return false;
        }
        if (!insertBlock.pos().equals(poiInfo.getPos())) {
            insertBlock = null;
            soldier.clearPoiInfo();
            takeAShortBreak(false);
            return false;
        }
        var cap = insertBlock.getCapability();
        if (cap == null) {
            insertBlock = null;
            soldier.clearPoiInfo();
            takeAShortBreak(false);
            return false;
        }

        int toInsert = getBatteryEnergyExtract(soldier.getCarriedStack());
        if (toInsert <= 0) {
            return false;
        }

        int inserted = insertBlock.getCapability().insert(toInsert);


        ClaySoldiersCommon.ENERGY_HELPER.extract(soldier.getCarriedStack(), inserted);
        soldier.updateCarriedStack();
        setDelay();

        return ClaySoldiersCommon.ENERGY_HELPER.getEnergyStoredAsInt(soldier.getCarriedStack()) >= 0;
    }

    private boolean extract(@NonNull BlockPos chargingPos) {
        if (moveToPos(chargingPos, 0)) {
            return true;
        }
        double xCharging = chargingPos.getX() + 0.5f - soldier.position().x();
        double zCharging = chargingPos.getX() + 0.5f - soldier.position().x();

        double distanceSquare = xCharging * xCharging + zCharging * zCharging;

        if (distanceSquare >= 0.3f) {
            pushToWardsPosition(chargingPos.getX() + 0.5f, chargingPos.getZ() + 0.5f);
            return true;
        }

        workStatus.setExtracting();

        return !isEnergyStorageFull();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean workRequiresItemPickUp(ItemStack stack) {
        return isBattery(stack);
    }

    @Override
    public boolean workRequiresItemCarrying(ItemStack stack) {
        return isBattery(stack);
    }

    private int currentEnergyStored() {
        return ClaySoldiersCommon.ENERGY_HELPER.getEnergyStoredAsInt(soldier.getCarriedStack());
    }

    private boolean isEnergyStorageFull() {
        return ClaySoldiersCommon.ENERGY_HELPER.getMaxEnergyStorage(soldier.getCarriedStack()) - currentEnergyStored() <= 0;
    }

    public static boolean isBattery(ItemStack stack) {
        return stack.is(ModTags.Items.BATTERY);
    }

    private IBlockCache<EnergyCapability> getEnergyCache(BlockPos pos, Direction side) {
        if (pos != null) {
            return ClaySoldiersCommon.CAPABILITY_MANGER.createEnergyCache(getServerLevel(soldier), pos, side);
        }
        return null;
    }

    private int getBatteryEnergyExtract(ItemStack stack) {
        int limit = ClaySoldiersCommon.ENERGY_HELPER.getEnergyStoredAsInt(stack);

        return Math.min(maxInsert * DELAY_BETWEEN_INSERTION, limit);
    }

    private void setDelay() {
        delay = DELAY_BETWEEN_INSERTION;
    }

    private Optional<BlockPos> findChargingPad() {
        return getServerLevel(soldier).getPoiManager().findClosest(s -> s.is(ModPoiTypes.CHARGING_PAD_KEY), soldier.blockPosition(), SEARCH_RADIUS, PoiManager.Occupancy.ANY);
    }

    private enum Action {
        INSERTING,
        EXTRACTING
    }
}
