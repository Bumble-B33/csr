package net.bumblebee.claysoldiers.item.claymobspawn;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.soldier.ClaySoldierEntity;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.item.BrickedItemHolder;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.team.loyalty.TeamLoyaltyManger;
import net.bumblebee.claysoldiers.team.loyalty.TeamPlayerData;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ClaySoldierSpawnItem extends MultiSpawnItem<ClaySoldierEntity> implements BrickedItemHolder {
    public static String DESCRIPTION_LANG = "item.csr.tooltip.curios.clay_soldier";
    public static final String DESCRIPTION_ID_PREFIX = ".with_prefix";
    public static final String PLAYER_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".clay_soldier.player";

    public ClaySoldierSpawnItem(Properties props) {
        super(props);
    }

    @Override
    public @NonNull EntityType<ClaySoldierEntity> getType() {
        return ModEntityTypes.CLAY_SOLDIER_ENTITY.get();
    }

    @Override
    public ItemStack getBrickedItem(ItemStack original) {
        return original.transmuteCopy(ModItems.BRICKED_CLAY_SOLDIER.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        var teamKey = stack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());

        if (flag.isAdvanced()) {
            if (teamKey != null) {
                tooltipAdder.accept(CommonComponents.space().append(
                        teamKey.toString()
                ).withStyle(ChatFormatting.DARK_GRAY));
            }
            var type = stack.get(ModDataComponents.CLAY_SOLDIER_ADDITIONAL_DATA.get());
            if (type != null) {
                tooltipAdder.accept(CommonComponents.space().append(
                        type.displayName()
                ).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        if (teamKey == null) {
            return;
        }

        var reg = context.registries();
        if (reg != null) {
            ClayMobTeamManger.get(teamKey, reg).ifPresent(h -> {
                var list = h.value().getPlayerNames();
                if (!list.isEmpty()) {
                    if (list.size() == 1) {
                        tooltipAdder.accept(CommonComponents.space().append(Component.translatable(PLAYER_LANG, list.getFirst())).withStyle(ChatFormatting.DARK_GRAY));
                    } else {
                        tooltipAdder.accept(CommonComponents.space().append(Component.translatable(PLAYER_LANG, list.toString())).withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
            });
        }
    }

    @Override
    public Consumer<ClaySoldierEntity> modifyBeforeSpawn(ItemStack stack, @Nullable Player player) {
        return claySoldier -> {
            claySoldier.setClayTeamType(stack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get()));
            claySoldier.setSpawnedFrom(stack, true);
            var additionalSoldierData = stack.get(ModDataComponents.CLAY_SOLDIER_ADDITIONAL_DATA.get());
            if (additionalSoldierData != null) {
                additionalSoldierData.convert(claySoldier, player);
            }
        };
    }

    public static void setClayMobTeam(ItemStack stack, Holder.Reference<ClayMobTeam> team) {
        stack.set(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), team.key());
        if (!ClayMobTeamManger.DEFAULT_KEY.equals(team.key())) {
            stack.set(DataComponents.ITEM_NAME,
                    Component.translatable(ModItems.CLAY_SOLDIER.get().getDescriptionId() + DESCRIPTION_ID_PREFIX, team.value().getDisplayName())
            );
        }
    }

    public static ItemStack createStack(@Nullable ResourceKey<ClayMobTeam> team, HolderLookup.Provider registries) {
        return createStack(ClayMobTeamManger.getOrDefault(team, registries));
    }

    public static ItemStack createStack(Holder.Reference<ClayMobTeam> team) {
        return createTemplateClayMobTeam(team).create();
    }

    public static ItemStack createStackUnchecked(ResourceKey<ClayMobTeam> team, int count) {
        var stack = new ItemStack(ModItems.CLAY_SOLDIER.get(), count);
        stack.set(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), team);
        return stack;
    }

    public static ItemStackTemplate createTemplateClayMobTeam(Holder.Reference<ClayMobTeam> teamId) {
        var builder = DataComponentPatch.builder();
        builder.set(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), teamId.key());
        if (!ClayMobTeamManger.DEFAULT_KEY.equals(teamId.key())) {
            builder.set(DataComponents.ITEM_NAME,
                    Component.translatable(ModItems.CLAY_SOLDIER.get().getDescriptionId() + DESCRIPTION_ID_PREFIX, teamId.value().getDisplayName())
            );
        }
        return new ItemStackTemplate(ModItems.CLAY_SOLDIER.get(), builder.build());
    }

    @Override
    public void onCraftedPostProcess(ItemStack stack, Level level) {
        var teamId = stack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());
        var team = ClayMobTeamManger.getFromKeyOrError(teamId, level.registryAccess());
        if (!ClayMobTeamManger.DEFAULT_KEY.equals(teamId)) {
            stack.set(DataComponents.ITEM_NAME,
                    Component.translatable(ModItems.CLAY_SOLDIER.get().getDescriptionId() + DESCRIPTION_ID_PREFIX, team.getDisplayName())
            );
        }
    }

    @Override
    protected boolean isValid(ItemStack stack, Level level, @Nullable Player player) {
        var key = getTeamFromStack(stack);
        if (key == null) {
            return false;
        }
        var team = ClayMobTeamManger.get(key, level.registryAccess());
        if (team.isEmpty()) {
            return false;
        }
        return team.orElseThrow().value().canBeUsed(player);
    }

    @Override
    public int getPouchColor(DataComponentMap stack, LivingEntity viewing) {
        return getColorFromTeam(stack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get()), viewing);
    }

    @Override
    public DataComponentMap requiredForPouch(ItemStack stack) {
        var team = stack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());
        var builder = DataComponentMap.builder()
                .set(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), team);
        return builder.build();
    }

    @Override
    public ItemStack recreateStackFromPouch(DataComponentMap data, HolderLookup.Provider registries) {
        return ClaySoldierSpawnItem.createStack(data.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get()), registries);
    }

    public static int getColorFromTeam(@Nullable ResourceKey<ClayMobTeam> team, LivingEntity player) {
        if (team == null) {
            return ClayMobTeamManger.ERROR.getColor(player, 0);
        }
        ColorHelper color = ClayMobTeamManger.getFromKeyOrError(team, player.registryAccess()).getColor();
        if (color.isEmpty()) {
            return ColorHelper.DEFAULT_CLAY_COLOR;
        }
        return color.getColor(player, 0);
    }

    @Nullable
    public static ResourceKey<ClayMobTeam> getTeamFromStack(ItemStack stack) {
        return stack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());
    }

    public static boolean canEquipClaySoldier(Player player, ResourceKey<ClayMobTeam> team) {
        var optTeam = ClayMobTeamManger.get(team, player.registryAccess());
        if (optTeam.isEmpty()) {
            return false;
        }
        TeamPlayerData data;
        if (player.level() instanceof ServerLevel serverLevel) {
            data = TeamLoyaltyManger.getTeamPlayerData(serverLevel);
        } else {
            data = TeamLoyaltyManger.getClientTeamPlayerData();
        }
        var playerData = data.getPlayerForTeam(team);

        return playerData != null && playerData.is(player);
    }

    private static boolean hasClaySoldierOnHead(Player player) {
        for (Predicate<Player> predicate : ClaySoldiersCommon.IS_WEARING_CLAY_SOLDIER) {
            if (predicate.test(player)) {
                return true;
            }
        }
        return false;
    }

    public static void onPlayerHurt(Player player, LivingEntity attacker) {
        if (!hasClaySoldierOnHead(player)) {
            return;
        }
        var stack = getProjectileItem(player);
        if (stack != null) {
            stack.getThrowableCap().performRangedAttack(player, player.level(), attacker, stack, 1f);
        }
    }

    public static ItemStackWithEffect getProjectileItem(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack ammoSlot = player.getInventory().getItem(i);
            if (ammoSlot.is(ModTags.Items.SOLDIER_THROWABLE_HARMFUL)) {
                var effect = new ItemStackWithEffect(ammoSlot);
                if (effect.isThrowable()) {
                    if (!player.hasInfiniteMaterials()) {
                        player.getInventory().removeItem(i, 1);
                    }
                    return effect;
                }
            }
        }
        return null;
    }

    public static void setRandomTeam(ItemStack stack, RegistryAccess registryAccess, RandomSource random) {
        stack.remove(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());
        registryAccess.lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS).getRandom(random).ifPresentOrElse(
                team -> setClayMobTeam(stack, team),
                () -> ClaySoldiersCommon.ERROR_HANDLER.error("Failed to set a Random Team Component for " + stack)
        );
    }
}