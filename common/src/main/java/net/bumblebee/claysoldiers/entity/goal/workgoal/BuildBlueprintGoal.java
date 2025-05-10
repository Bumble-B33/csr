package net.bumblebee.claysoldiers.entity.goal.workgoal;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintRequest;
import net.bumblebee.claysoldiers.capability.BlueprintRequestHandler;
import net.bumblebee.claysoldiers.capability.IBlockCache;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

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

    public BuildBlueprintGoal(AbstractClaySoldierEntity soldier, Supplier<WorkSelectorGoal> workSelector, int searchRange) {
        super(soldier, workSelector);
        this.searchRange = searchRange;
        this.verticalSearchRange = 2;
    }

    @Override
    public boolean canUse() {
        if (isOnBreak()) {
            return false;
        }
        return getPoiPos() != null && getCapCacheResetIfInvalid() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse();
    }


    @Override
    public void tick() {
        if (easelPos == null) {
            setStatus(REQUIRES_POI_ID);
            easelPos = findNearestBlock().map(this::getBlueprintCache).orElse(null);
            return;
        }

        if (request == null) {
            if (bringBack) {
                setStatus(RETURNING_ID);
                if (returnCarried()) {
                    bringBack = false;
                }
            } else {
                getRequestJob();
                setStatus(SEARCHING_ID);
            }
        } else if (request.isCancelled() || request.isFinished()) {
            bringBack = !soldier.getCarriedStack().isEmpty();
            request = null;
        } else if (request.hasStarted()) {
            getRequestFromStorage();
        } else if (request.isPlacing()) {
            setStatus(CARRYING_ID);
            if (moveToRequest()) {
                placeBlock();
            }
        }
    }

    private void getRequestFromStorage() {
        if (moveToPoi()) {
            assert getCapCache() != null;
            var storage = getCapCache().getCapability();
            if (storage != null) {
                if (!soldier.getCarriedStack().isEmpty()) {
                    var returned = storage.tryInserting(soldier.getCarriedStack());
                    soldier.dropItemStack(returned);
                }
                soldier.setCarriedStack(storage.tryExtracting(stack -> stack.is(request.getItem()), 1));
                request.setPlacing();
            } else {
                easelPos = null;
                request = null;
            }
        }
    }

    private boolean returnCarried() {
        if (moveToPoi()) {
            assert getCapCache() != null;
            var storage = getCapCache().getCapability();
            if (storage != null) {
                soldier.spawnAtLocation(storage.tryInserting(soldier.getCarriedStack()));
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
            if (requestHandler.doRequest(request)) {
                soldier.setCarriedStack(ItemStack.EMPTY);
                request.setFinished();
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
                        if (this.soldier.isWithinRestriction(blockpos$mutableblockpos) && this.isValidTarget(this.soldier.level(), blockpos$mutableblockpos)) {
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
            return ClaySoldiersCommon.CAPABILITY_MANGER.createBlueprint((ServerLevel) soldier.level(), pos);
        }
        return null;
    }


    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean workRequiresItemCarrying() {
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
