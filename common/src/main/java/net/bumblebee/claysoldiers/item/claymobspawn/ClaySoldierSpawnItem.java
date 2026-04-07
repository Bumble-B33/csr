package net.bumblebee.claysoldiers.item.claymobspawn;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.soldier.ClaySoldierEntity;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.item.BrickedItemHolder;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.team.TeamLoyaltyManger;
import net.bumblebee.claysoldiers.team.TeamPlayerData;
import net.bumblebee.claysoldiers.util.ErrorHandler;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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
    public EntityType<ClaySoldierEntity> getType() {
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

        ClayMobTeam team = ClayMobTeamManger.getFromKeyOrError(teamKey, context.registries());
        var list = team.getPlayerNames();
        if (!list.isEmpty()) {

            if (list.size() == 1) {
                tooltipAdder.accept(CommonComponents.space().append(Component.translatable(PLAYER_LANG, list.getFirst())).withStyle(ChatFormatting.DARK_GRAY));
            } else {
                tooltipAdder.accept(CommonComponents.space().append(Component.translatable(PLAYER_LANG, list.toString())).withStyle(ChatFormatting.DARK_GRAY));
            }
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

    public static void setClayMobTeam(ItemStack stack, @Nullable ResourceLocation teamId, HolderLookup.Provider registries) {
        var team = ClayMobTeamManger.getFromKeyOrError(teamId, registries);
        stack.set(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), teamId);
        if (!ClayMobTeamManger.DEFAULT_TYPE.equals(teamId)) {
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
        var team = ClayMobTeamManger.getFromKey(key, level.registryAccess());
        if (team == null) {
            return false;
        }
        return team.canBeUsed(player);
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
        ItemStack stack = new ItemStack(this);
        setClayMobTeam(stack, data.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get()), registries);
        return stack;
    }

    public static int getColorFromTeam(@Nullable ResourceLocation team, LivingEntity player) {
        if (team == null) {
            return ClayMobTeamManger.ERROR.getColor(0, 0);
        }
        int color = ClayMobTeamManger.getFromKeyOrError(team, player.registryAccess()).getColor(player, 0);
        if (color == -1) {
            return ColorHelper.DEFAULT_CLAY_COLOR;
        }
        return ARGB.opaque(color);
    }

    @Nullable
    public static ResourceLocation getTeamFromStack(ItemStack stack) {
        return stack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());
    }


    public static boolean canEquipClaySoldier(Player player, ResourceLocation team) {
        var optTeam = ClayMobTeamManger.getOptional(team, player.registryAccess());
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
                team -> {
                    stack.set(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), team.key().location());
                },
                () -> ErrorHandler.INSTANCE.error("Failed to set a Random Team Component for " + stack)
        );
    }
}