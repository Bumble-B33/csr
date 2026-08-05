package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.capability.IBlockStorageAccess;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class PickUpItemsGoal extends AbstractWorkGoal {
    public static final String PICK_UP_ITEM_LANG = JOB_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "pick_up_items");
    private static final Predicate<ItemEntity> ALLOWED_ITEMS = itemEntity -> !itemEntity.hasPickUpDelay() && itemEntity.isAlive();
    private final int verticalSearchRange;
    private final int horizontalSearchRange;

    public PickUpItemsGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess, SearchRange searchRange) {
        super(soldier, workAccess);
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.horizontalSearchRange = searchRange.horizontalRange();
        this.verticalSearchRange = searchRange.verticalRange();
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

    public void pushToWardsItem(ItemEntity itemEntity) {
        pushToWardsPosition(itemEntity.getX(), itemEntity.getZ());
    }

    @Override
    public void tick() {
        if (isOnBreak()) {
            return;
        }

        if (soldier.getCarriedStack().isEmpty()) {
            workStatus.setSearching();
            List<ItemEntity> list = getItemsInArea();
            if (!list.isEmpty()) {
                soldier.getNavigation().moveTo(list.getFirst(), 1.2F);
                if (soldier.getNavigation().isDone()) {
                    pushToWardsItem(list.getFirst());
                }
            }
            return;
        }

        BlockPos pos = getPoiPos();
        if (pos == null) {
            workStatus.setNoChest();
            takeAShortBreak(false);
            return;
        }
        if (getCapCache() == null) {
            setCapCache();
            workStatus.setNoChest();
            takeAShortBreak(false);
            return;
        }
        if (!pos.equals(getCapCache().pos())) {
            return;
        }
        IBlockStorageAccess cap = getCapCache().getCapability();
        if (cap == null) {
            soldier.clearPoiInfo();
            workStatus.setNoChest();
            takeAShortBreak(false);
            return;
        }

        workStatus.setCarrying();
        if (moveToPoi(2f)) {
            soldier.setCarriedStack(cap.tryInserting(soldier.getCarriedStack()));
            workStatus.setBreak();
        }
    }


    @Override
    public void start() {
        setCapCache();
        List<ItemEntity> list = getItemsInArea();
        if (!list.isEmpty()) {
            soldier.getNavigation().moveTo(list.getFirst(), 1.2F);
        }
    }

    private List<ItemEntity> getItemsInArea() {
        return soldier.level().getEntitiesOfClass(ItemEntity.class, soldier.getBoundingBox().inflate(horizontalSearchRange, verticalSearchRange, horizontalSearchRange), ALLOWED_ITEMS);
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable(PICK_UP_ITEM_LANG);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean workRequiresItemPickUp(ItemStack stack) {
        return true;
    }

    @Override
    public boolean workRequiresItemCarrying(ItemStack stack) {
        return true;
    }
}
