package net.bumblebee.claysoldiers.entity.goal;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.capability.AssignablePoiCapability;
import net.bumblebee.claysoldiers.capability.IBlockCache;
import net.bumblebee.claysoldiers.capability.IBlockStorageAccess;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.SoldierSlotCallback;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class UseAssignedPoiGoal extends Goal {
    private static final int MAX_WAIT_BEFORE_NEXT_ATTEMPT = 200;
    private final ClayMobEntity clayMob;
    private final double speedModifier;
    private int waitTime = 0;

    public UseAssignedPoiGoal(ClayMobEntity clayMob, double pSpeedModifier) {
        this.clayMob = clayMob;
        this.speedModifier = pSpeedModifier;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return isPoiUsable();
    }

    @Override
    public boolean canContinueToUse() {
        return clayMob.getPoiPos() != null && clayMob.getPoiCapability() != null;
    }

    private boolean isPoiUsable() {
        if (--waitTime > 0) {
            return false;
        }

        if (clayMob.getPoiPos() == null) {
            return false;
        }
        var cap = clayMob.getPoiCapability();
        if (cap == null || !cap.canUse(clayMob)) {
            waitTime = MAX_WAIT_BEFORE_NEXT_ATTEMPT;
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        moveToPoi();
        clayMob.setUsingPoi(true);
    }

    @Override
    public void tick() {
        if (!moveToPoi()) {
            return;
        }

        var cap = clayMob.getPoiCapability();
        if (cap != null && cap.canUse(clayMob)) {
            cap.use(clayMob);
            if (cap.isOneTimeUse()) {
                clayMob.setPoiPos(null);
            }
        } else {
            clayMob.setPoiPos(null);
        }
    }

    @Override
    public void stop() {
        clayMob.setUsingPoi(false);
    }

    private boolean moveToPoi() {
        BlockPos pos = clayMob.getPoiPos();
        if (pos != null) {
            if (pos.closerToCenterThan(clayMob.position(), 1f)) {
                this.clayMob.getNavigation().stop();
                return true;
            } else {
                this.clayMob.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, speedModifier);
                return false;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public static IBlockCache<AssignablePoiCapability> createCache(ClayMobEntity clayMob, ServerLevel level) {
        var cache = ClaySoldiersCommon.CAPABILITY_MANGER.createPoiCache(level, clayMob.getPoiPos());

        if (cache.getCapability() != null) {
            return cache;
        }
        var storageCache = ClaySoldiersCommon.CAPABILITY_MANGER.create(level, clayMob.getPoiPos());
        if (storageCache.getCapability() != null) {
            return new StorageWrappedPoiCache(storageCache);
        }

        return cache;
    }

    // Storage Poi

    private static int canEquipStack(AbstractClaySoldierEntity inventory, ItemStack stack, Set<SoldierEquipmentSlot> slots) {
        if (!inventory.canHoldItem(stack)) {
            return 0;
        }

        var effect = ClaySoldiersCommon.DATA_MAP.getEffect(stack);
        if (effect == null) {
            return 0;
        }
        boolean anySuitableSlot = false;
        for (SoldierEquipmentSlot suitableSlot : effect.slots()) {
            if (slots.contains(suitableSlot)) {
                anySuitableSlot = true;
                break;
            }
        }
        if (!anySuitableSlot) {
            return 0;
        }

        if (!inventory.couldEquipStack(effect)) {
            return 0;
        }
        return Math.min(effect.getMaxStackSize(), stack.getMaxStackSize());
    }

    private static void equipStack(AbstractClaySoldierEntity soldier, ItemStack stack, List<ItemStack> overflow, Set<SoldierEquipmentSlot> remainingSlots) {
        SlotCallBack slotHolder = new SlotCallBack();
        var equippedStack = soldier.equipItemIfPossible(stack, slotHolder);
        int remaining = stack.getCount() - equippedStack.getCount();
        if (remaining > 0) {
            overflow.add(equippedStack.copyWithCount(remaining));
        }
        if (slotHolder.slot != null) {
            remainingSlots.remove(slotHolder.slot);
        }
    }

    private record StorageWrappedPoiCache(IBlockCache<IBlockStorageAccess> cache) implements IBlockCache<AssignablePoiCapability> {

        @Override
        public BlockPos pos() {
            return cache.pos();
        }

        @Override
        public @Nullable AssignablePoiCapability getCapability() {
            return cache.getCapability() == null ? null : new StorageWrappedPoiCap(cache.getCapability());
        }
    }

    private record StorageWrappedPoiCap(IBlockStorageAccess storage) implements AssignablePoiCapability {

        @Override
        public boolean canUse(ClayMobEntity clayMob) {
            return clayMob instanceof AbstractClaySoldierEntity;
        }

        @Override
        public void use(ClayMobEntity clayMob) {
            if (!(clayMob instanceof AbstractClaySoldierEntity soldier)) {
                return;
            }
            Set<SoldierEquipmentSlot> emptySlots = EnumSet.noneOf(SoldierEquipmentSlot.class);

            for (SoldierEquipmentSlot slot : SoldierEquipmentSlot.values()) {
                if (!soldier.getItemBySlot(slot).isEmpty()) {
                    continue;
                }
                emptySlots.add(slot);
            }

            List<ItemStack> notNeededStacks = new ArrayList<>();
            storage.forEach(
                    stack -> canEquipStack(soldier, stack, emptySlots),
                    (stack) -> equipStack(soldier, stack, notNeededStacks, emptySlots),
                    emptySlots::isEmpty
            );
            notNeededStacks.forEach(storage::tryInserting);
        }
    }

    private static class SlotCallBack implements SoldierSlotCallback {
        private SoldierEquipmentSlot slot = null;

        @Override
        public void slot(SoldierEquipmentSlot slot) {
            this.slot = slot;
        }

        @Override
        public void capability() {
        }

        @Override
        public void carried() {
        }
    }
}
