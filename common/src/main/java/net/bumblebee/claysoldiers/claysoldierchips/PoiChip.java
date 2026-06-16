package net.bumblebee.claysoldiers.claysoldierchips;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.UseAssignedPoiGoal;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;

public class PoiChip extends ClaySoldierChip<Unit> {
    public static final PoiChip INSTANCE = new PoiChip();
    private static final Identifier ASSET_ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "poi");
    public static final String POI_NO_DATA_LANG = LANG_PREFIX + ".data.poi.unset";
    public static final String ACTIVE_POI_DATA_LANG = LANG_PREFIX + ".data.poi.active";

    private PoiChip() {
        super(Unit.INSTANCE, List.of());
    }

    public static PoiChip create() {
        return INSTANCE;
    }

    @Override
    public void addSpecialGoals(ServerLevel level, ProgrammableClaySoldierEntity soldier, BiConsumer<Integer, Goal> goalAdder, BiConsumer<Integer, Goal> targetAdder) {
        goalAdder.accept(1, new UseAssignedPoiGoal(soldier, 1.2f));
    }

    @Override
    public Type<Unit> getType() {
        return ClaySoldierChips.USE_POI.get();
    }

    @Override
    public ClaySoldierChip<Unit> withSoldier(ProgrammableClaySoldierEntity soldier) {
        return this;
    }

    @Override
    public Component getWorkStatusDisplayName(@NotNull ClayMobEntity soldier) {
        if (!soldier.hasPoiPos()) {
            return Component.translatable(POI_NO_DATA_LANG);
        }

        return Component.translatable(ACTIVE_POI_DATA_LANG);
    }

    @Override
    public @Nullable Identifier assetId() {
        return ASSET_ID;
    }

    @Override
    public OptionalInt getItemColorForLayer(ItemLayer layer) {
        return switch (layer) {
            case BASE -> OptionalInt.of(0x0707FF);
            case CONNECTION -> OptionalInt.of(0xFFA61B);
            default -> OptionalInt.empty();
        };
    }
}
