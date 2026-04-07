package net.bumblebee.claysoldiers.entity.common.programmable;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.goal.workgoal.WorkSelectorGoal;
import net.bumblebee.claysoldiers.entity.common.programmable.chips.ClaySoldierChip;
import net.bumblebee.claysoldiers.entity.common.programmable.chips.CombatChip;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.soldier.status.SoldierStatusHolder;
import net.bumblebee.claysoldiers.networking.spawnpayloads.ProgrammableClaySoldierSpawnPayload;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class ProgrammableClaySoldierEntity extends AbstractClaySoldierEntity {
    private static final String CHIP_TAG_NAME = "chip_module";
    private static final SoldierStatusHolder EMPTY = () -> null;
    private static final Logger log = LoggerFactory.getLogger(ProgrammableClaySoldierEntity.class);
    @Nullable
    private ClaySoldierChip<?> brain = null;

    public ProgrammableClaySoldierEntity(EntityType<? extends AbstractClaySoldierEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel, AttackTypeProperty.ROBOT, (s) -> EMPTY);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (brain != null) {
            brain.save(output);
        }

    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        brain = ClaySoldierChip.load(this, input);
    }

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (pPlayer.getItemInHand(pHand).is(Items.REDSTONE)) {
            setupChip(new CombatChip(this, CombatChip.Data.monster()));

            return InteractionResult.SUCCESS;
        }
        if (pPlayer.getItemInHand(pHand).is(Items.GLOWSTONE)) {
            clearChip();
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(pPlayer, pHand);
    }

    public void clearChip() {
        brain = null;
        if (!level().isClientSide()) {
            targetSelector.removeAllGoals(p -> true);
            goalSelector.removeAllGoals(p -> true);
        }

    }

    public void setupChip(@NotNull ClaySoldierChip<?> chip) {
        clearChip();
        brain = chip;
        if (level() instanceof ServerLevel serverLevel) {
            chip.addGoals(serverLevel, goalSelector::addGoal, targetSelector::addGoal);
        }
    }

    @Nullable
    public ClaySoldierChip<?> getInstalledModule() {
        return brain;
    }

    @Override
    protected void registerGoals() {}

    @Override
    protected WorkSelectorGoal getOrCreateWorkSelectorGoal() {
        return new WorkSelectorGoal(this, List.of());
    }

    @Override
    public @Nullable UUID getClayTeamOwnerUUID() {
        return null;
    }

    @Override
    public boolean canBeKilledByDisruptor(ServerLevel level, ServerPlayer player) {
        return false;
    }

    @Override
    protected boolean isClayFood(ItemStack stack) {
        return false;
    }

    @Override
    public void getStatDisplay(List<Component> list, LivingEntity viewer) {
        super.getStatDisplay(list, viewer);
        if (brain != null) {
            list.add(CommonComponents.space().append(Component.translatable(MODULE, brain.info()).withStyle(ChatFormatting.GRAY)));
        }
    }

    @Override
    public List<String> getInfoState() {
        var info = super.getInfoState();
        if (brain != null) {
            info.add("Module: " + brain.asString());
        }

        return info;
    }

    @Override
    public void sendSpawnPayload(ServerPlayer tracking) {
        super.sendSpawnPayload(tracking);
        if (brain != null) {
            ClaySoldiersCommon.NETWORK_MANGER.sendToPlayer(tracking, new ProgrammableClaySoldierSpawnPayload(this, brain));
        }
    }
}
