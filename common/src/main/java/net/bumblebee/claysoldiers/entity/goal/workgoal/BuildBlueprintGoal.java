package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintRequest;
import net.bumblebee.claysoldiers.blueprint.BlueprintRequestResult;
import net.bumblebee.claysoldiers.capability.BlueprintRequestHandler;
import net.bumblebee.claysoldiers.capability.IBlockCache;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BuildBlueprintGoal extends AbstractWorkGoal {
    public static final String BUILDING_LANG = JOB_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "building");

    @Nullable
    private IBlockCache<BlueprintRequestHandler> easelPos = null;
    private final int searchRange;
    private final int verticalSearchRange;
    protected int verticalSearchStart;
    @Nullable
    private BlueprintRequest request = null;
    private boolean bringBack = false;

    public BuildBlueprintGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workSelector, SearchRange searchRange) {
        super(soldier, workSelector);
        this.searchRange = searchRange.horizontalRange();
        this.verticalSearchRange = searchRange.verticalRange();
    }

    @Override
    public void tick() {
        if (isOnBreak()) {
            return;
        }
        if (getPoiInfo().isEmpty()) {
            workStatus.setRequiresPoi();
            takeAShortBreak(false);
            return;
        }
        if (getCapCacheResetIfInvalid() == null) {
            workStatus.setRequiresPoi();
            takeAShortBreak(false);
            return;
        }

        if (easelPos == null) {
            workStatus.setRequiresPoi();
            easelPos = findNearestBlock().map(this::getBlueprintCache).orElse(null);
            return;
        }

        if (request == null) {
            if (bringBack) {
                workStatus.setReturning();
                if (returnCarried()) {
                    bringBack = false;
                }
            } else {
                getRequestJob();
                workStatus.setSearching();
            }
        } else if (request.isCancelled() || request.isFinished()) {
            bringBack = !soldier.getCarriedStack().isEmpty();
            request = null;
        } else if (request.hasStarted()) {
            getRequestFromStorage();
        } else if (request.isPlacing()) {
            workStatus.setCarrying();
            if (moveToRequest()) {
                placeBlock();
            }
        }
    }

    private void getRequestFromStorage() {
        if (moveToPoi(2d)) {
            assert getCapCache() != null;
            var storage = getCapCache().getCapability();
            if (storage != null) {
                if (!soldier.getCarriedStack().isEmpty()) {
                    var returned = storage.tryInserting(soldier.getCarriedStack());
                    soldier.dropItemStack(returned);
                }
                ItemStack extracted = storage.tryExtracting(stack -> stack.is(request.getItem()), 1);
                soldier.setCarriedStack(extracted);
                if (!soldier.getCarriedStack().isEmpty()) {
                    request.setPlacing();
                } else {
                    takeAShortBreak(true);
                    workStatus.setCannotFindItem();
                }

            } else {
                easelPos = null;
                request = null;
            }
        }
    }

    private boolean returnCarried() {
        if (moveToPoi(2d)) {
            assert getCapCache() != null;
            var storage = getCapCache().getCapability();
            if (storage != null) {
                soldier.spawnAtLocation(getServerLevel(soldier), storage.tryInserting(soldier.getCarriedStack()));
                soldier.setCarriedStack(ItemStack.EMPTY);
            } else {
                soldier.dropCarried();
            }
            return true;
        }
        return false;
    }

    private void getRequestJob() {
        if (!moveMobToEasel()) {
            return;
        }
        var requestHandler = easelPos.getCapability();
        if (requestHandler != null) {
            request = requestHandler.getRequest(this::canReach);
        } else {
            easelPos = null;
        }
    }

    private boolean canReach(BlockPos pos) {
        var path = soldier.getNavigation().createPath(pos, 2);
        return path != null && path.canReach();
    }


    private void placeBlock() {
        var requestHandler = easelPos.getCapability();
        if (requestHandler != null) {
            BlueprintRequestResult result = requestHandler.doRequest(request, soldier);
            if (result.isSuccess()) {
                soldier.setCarriedStack(result.getRemainder());
                request.setFinished();
                bringBack = result.hasRemainder();
            } else {
                bringBack = true;
                request.cancel();
            }
            request = null;
        } else {
            easelPos = null;
        }
    }

    protected Optional<BlockPos> findNearestBlock() {
        BlockPos center = this.soldier.blockPosition();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int searchIndex = this.verticalSearchStart; searchIndex <= this.verticalSearchRange; searchIndex = searchIndex > 0 ? -searchIndex : 1 - searchIndex) {
            for (int rangeIndex = 0; rangeIndex < this.searchRange; rangeIndex++) {
                for (int i1 = 0; i1 <= rangeIndex; i1 = i1 > 0 ? -i1 : 1 - i1) {
                    for (int j1 = i1 < rangeIndex && i1 > -rangeIndex ? rangeIndex : 0; j1 <= rangeIndex; j1 = j1 > 0 ? -j1 : 1 - j1) {
                        blockpos$mutableblockpos.setWithOffset(center, i1, searchIndex - 1, j1);
                        if (this.soldier.isWithinHome(blockpos$mutableblockpos) && this.isValidTarget(this.soldier.level(), blockpos$mutableblockpos)) {
                            return Optional.of(blockpos$mutableblockpos);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    protected boolean moveMobToEasel() {
        if (easelPos == null) {
            return false;
        }
        if (easelPos.pos().closerToCenterThan(soldier.position(), 2f)) {
            this.soldier.getNavigation().stop();
            return true;
        }
        this.soldier.getNavigation().moveTo(this.easelPos.pos().getX() + 0.5, (this.easelPos.pos().getY() + 1), this.easelPos.pos().getZ() + 0.5, 1);
        return false;
    }

    protected boolean moveToRequest() {
        if (request.getPos().closerToCenterThan(soldier.position(), 2f)) {
            this.soldier.getNavigation().stop();
            return true;
        }
        this.soldier.getNavigation().moveTo(this.request.getPos().getX() + 0.5, (this.request.getPos().getY() + 1), this.request.getPos().getZ() + 0.5, 1);
        return false;
    }

    protected boolean isValidTarget(LevelReader pLevel, @Nullable BlockPos pPos) {
        return pPos != null && pLevel.getBlockState(pPos).is(ModBlocks.EASEL_BLOCK.get());
    }


    private IBlockCache<BlueprintRequestHandler> getBlueprintCache(BlockPos pos) {
        if (pos != null) {
            return ClaySoldiersCommon.CAPABILITY_MANGER.createBlueprintCache((ServerLevel) soldier.level(), pos);
        }
        return null;
    }


    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean workRequiresItemCarrying(ItemStack stack) {
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(BUILDING_LANG);
    }

    @Override
    public void stop() {
        if (request != null) {
            request.cancel();
        }
    }

    @Override
    public String asString() {
        return "BuildBlueprintGoal{%s: %s(%s)}".formatted(
                easelPos == null ? "Null" : "Cap",
                request,
                request == null ? (bringBack ? "bringBack" : "-") : canReach(request.getPos()) ? "Can Reach" : "Cannot Reach");
    }
}
