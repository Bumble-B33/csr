package net.bumblebee.claysoldiers.item.claymobspawn;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.soldier.ClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.item.BrickedItemHolder;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public class ClaySoldierSpawnItem extends MultiSpawnItem<ClaySoldierEntity> implements BrickedItemHolder {
    public static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "item/clay_soldier");
    public static final String DESCRIPTION_ID_PREFIX = ".with_prefix";
    public static final String PLAYER_LANG = "item." + ClaySoldiersCommon.MOD_ID + ".clay_soldier.player";

    public ClaySoldierSpawnItem(Properties props) {
        super(props.component(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), ClayMobTeamManger.DEFAULT_TYPE));
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
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        var teamKey = pStack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());

        if (pTooltipFlag.isAdvanced()) {
            if (teamKey != null) {
                pTooltipComponents.add(CommonComponents.space().append(
                        teamKey.toString()
                ).withStyle(ChatFormatting.DARK_GRAY));
            }
            var type = pStack.get(ModDataComponents.CLAY_SOLDIER_ADDITIONAL_DATA.get());
            if (type != null) {
                pTooltipComponents.add(CommonComponents.space().append(
                        type.displayName()
                ).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        ClayMobTeam team = ClayMobTeamManger.getFromKeyOrError(teamKey, pContext.registries());
        var list = team.getPlayerNames();
        if (!list.isEmpty()) {

            if (list.size() == 1) {
                pTooltipComponents.add(CommonComponents.space().append(Component.translatable(PLAYER_LANG, list.getFirst())).withStyle(ChatFormatting.DARK_GRAY));
            } else {
                pTooltipComponents.add(CommonComponents.space().append(Component.translatable(PLAYER_LANG, list.toString())).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

    }

    @Override
    public Consumer<ClaySoldierEntity> modifyBeforeSpawn(ItemStack stack) {
        return claySoldier -> {
            claySoldier.setClayTeamType(stack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get()));
            claySoldier.setSpawnedFrom(stack, true);
            var additionalSoldierData = stack.get(ModDataComponents.CLAY_SOLDIER_ADDITIONAL_DATA.get());
            if (additionalSoldierData != null) {
                additionalSoldierData.convert(claySoldier);
            }
        };
    }

    public static void setClayMobTeam(ItemStack stack, ResourceLocation teamId, HolderLookup.Provider registries) {
        var team = ClayMobTeamManger.getFromKeyOrError(teamId, registries);
        stack.set(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get(), teamId);
        if (!ClayMobTeamManger.DEFAULT_TYPE.equals(teamId)) {
            stack.set(DataComponents.ITEM_NAME,
                    Component.translatable(ModItems.CLAY_SOLDIER.get().getDescriptionId() + DESCRIPTION_ID_PREFIX, team.getDisplayName())
            );
        }

    }

    @Override
    protected boolean isValid(ItemStack stack, Level level, Player player) {
        var key = stack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());
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

    public static int getColorFromTeam(ResourceLocation team, LivingEntity player) {
        int color = ClayMobTeamManger.getFromKeyOrError(team, player.registryAccess()).getColor(player, 0);
        if (color == -1) {
            return ColorHelper.DEFAULT_CLAY_COLOR;
        }
        return FastColor.ARGB32.opaque(color);
    }
}