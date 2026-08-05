package net.bumblebee.claysoldiers.claysoldierchips.work;

import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierWorkAccess;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddons;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.workgoal.IWorkGoal;
import net.bumblebee.claysoldiers.entity.goal.workgoal.SearchRange;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class WorkGoalChip<G extends Goal & IWorkGoal> extends ClaySoldierChip {

    private ClayMobWorkAccess workAccess;
    private ProgrammableClaySoldierEntity soldier;
    private G goal;
    private final Identifier assetId;
    private final int baseColor;
    private final int connectionColor;

    protected WorkGoalChip(List<ClaySoldierChipAddon> addons, Identifier assetId, int baseColor, int connectionColor) {
        super(addons);
        this.assetId = assetId;
        this.baseColor = baseColor;
        this.connectionColor = connectionColor;
    }

    protected abstract G createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess);

    @Override
    public void addSpecialGoals(ServerLevel level, ProgrammableClaySoldierEntity soldier, BiConsumer<Integer, Goal> goalAdder, BiConsumer<Integer, Goal> targetAdder) {
        goalAdder.accept(1, goal);
    }

    @Override
    public WorkGoalChip<G> withSoldier(ProgrammableClaySoldierEntity soldier) {
        if (soldier.level().isClientSide() && this.soldier != null) {
            return this;
        }
        this.soldier = soldier;
        this.workAccess = new ClaySoldierWorkAccess(soldier);
        this.goal = createGoal(soldier, workAccess);
        return this;
    }

    @Override
    public boolean requiresItemPickUp(ItemStack stack) {
        return goal.workRequiresItemPickUp(stack);
    }

    @Override
    public boolean requiresItemCarrying(ItemStack stack) {
        return goal.workRequiresItemCarrying(stack);
    }

    @Override
    public void reset() {
        goal.stop();
    }

    @Override
    public boolean shouldStayAtWork() {
        return !soldier.getPoiInfo().isEmpty();
    }

    @Override
    public Component getWorkStatusDisplayName(@NotNull ClayMobEntity soldier) {
        return goal.getWorkStatus();
    }

    @Override
    public void appendDebugInfo(Consumer<String> appender) {
        super.appendDebugInfo(appender);
        if (goal != null) {
            appender.accept(goal.asString());
        } else {
            appender.accept("Goal not yet created");
        }

    }

    @Override
    public Identifier assetId() {
        return assetId;
    }

    @Override
    public OptionalInt getItemColorForLayer(ItemLayer layer) {
        return switch (layer) {
            case BASE -> OptionalInt.of(baseColor);
            case CONNECTION -> OptionalInt.of(connectionColor);
        };
    }

    public SearchRange scaleRange(SearchRange searchRange) {
        if (hasAddon(ClaySoldierChipAddons.RANGE_ADDON)) {
            return new SearchRange((int) (searchRange.horizontalRange() * 1.5f), (int) (searchRange.verticalRange() * 1.5f));
        }
        return searchRange;
    }
}
