package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PickUpItemsGoal extends AbstractWorkGoal {
    public static final String PICK_UP_ITEM_LANG = JOB_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "pick_up_items");
    private static final Predicate<ItemEntity> ALLOWED_ITEMS = itemEntity -> !itemEntity.hasPickUpDelay() && itemEntity.isAlive();
    private static final float VERTICAL_SEARCH_RANGE = 8;

    public PickUpItemsGoal(AbstractClaySoldierEntity soldier, Supplier<WorkSelectorGoal> workSelector) {
        super(soldier, workSelector);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (isOnBreak()) {
            return false;
        }
        return !getItemsInArea().isEmpty() || !soldier.getCarriedStack().isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        if (isOnBreak()) {
            return false;
        }
        return !getItemsInArea().isEmpty();
    }

    public void pushToWardsItem(ItemEntity itemEntity) {
        double xDif = itemEntity.getX() - soldier.getX();
        double zDif = itemEntity.getZ() - soldier.getZ();
        double absMax = Mth.absMax(xDif, zDif);
        if (absMax >= 0.01F) {
            absMax = Math.sqrt(absMax);
            xDif /= absMax;
            zDif /= absMax;
            double invertedAbsMax = 1.0 / absMax;
            if (invertedAbsMax > 1.0) {
                invertedAbsMax = 1.0;
            }

            xDif *= invertedAbsMax;
            zDif *= invertedAbsMax;
            xDif *= 0.05F;
            zDif *= 0.05F;
            soldier.setDeltaMovement(soldier.getDeltaMovement().add(xDif, 0, zDif));
        }
    }

    @Override
    public void tick() {
        if (soldier.getCarriedStack().isEmpty()) {
            setStatus(SEARCHING_ID);
            List<ItemEntity> list = getItemsInArea();
            if (soldier.getItemBySlot(SoldierEquipmentSlot.MAINHAND).isEmpty() && !list.isEmpty()) {
                soldier.getNavigation().moveTo(list.getFirst(), 1.2F);
                if (soldier.getNavigation().isDone()) {
                    pushToWardsItem(list.getFirst());
                }

            }
        } else {
            BlockPos pos = getPoiPos();
            if (pos != null) {
                setStatus(CARRYING_ID);
                if (pos.closerToCenterThan(soldier.position(), 2f)) {
                    this.soldier.getNavigation().stop();
                    if (getCapCache() != null && pos.equals(getCapCache().pos())) {
                        var cap = getCapCache().getCapability();
                        if (cap != null) {
                            soldier.setCarriedStack(cap.tryInserting(soldier.getCarriedStack()));
                        }
                        takeAShortBreak();
                    } else {
                        setCapCache();
                    }
                } else {
                    this.soldier.getNavigation().moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 1.2);
                }
            }
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
        return soldier.level().getEntitiesOfClass(ItemEntity.class, soldier.getBoundingBox().inflate(8.0, VERTICAL_SEARCH_RANGE, 8.0), ALLOWED_ITEMS);
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
    public boolean workRequiresItemPickUp() {
        return true;
    }

    @Override
    public boolean workRequiresItemCarrying() {
        return true;
    }
}
