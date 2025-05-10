package net.bumblebee.claysoldiers.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayMobTeamOwnerEntity;
import net.bumblebee.claysoldiers.entity.boss.ClaySoldierBossEquipment;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.soldieritemtypes.SoldierItemType;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.team.TeamLoyaltyManger;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ClaySoldierCommands {
    public static final String COMMAND_TEAM_SINGLE_SUCCESS = "commands." + ClaySoldiersCommon.MOD_ID + ".change_team.success.single";
    public static final String COMMAND_TEAM_MULTIPLE_SUCCESS = "commands." + ClaySoldiersCommon.MOD_ID + ".change_team.success.multiple";
    public static final String COMMAND_TEAM_FAILURE = "commands." + ClaySoldiersCommon.MOD_ID + ".change_team.failure";
    public static final String COMMAND_TEAM_ITEM_SUCCESS = "commands." + ClaySoldiersCommon.MOD_ID + ".change_team.item.success";
    public static final String COMMAND_EXECUTED_BY_PLAYER = "commands." + ClaySoldiersCommon.MOD_ID + ".change_team.item.failure";

    public static final String COMMAND_LOADED_TEAMS_SUCCESS = "commands." + ClaySoldiersCommon.MOD_ID + ".loaded_teams";
    public static final String COMMAND_SHOW_TEAM_ALLEGIANCE_SUCCESS = "commands." + ClaySoldiersCommon.MOD_ID + ".team_allegiance";
    public static final String COMMAND_SHOW_TEAM_ALLEGIANCE_EMPTY = "commands." + ClaySoldiersCommon.MOD_ID + ".team_allegiance.empty";

    public static final String COMMAND_ITEM_SET_ERROR = "commands." + ClaySoldiersCommon.MOD_ID + ".spawn_item_set.error";
    public static final String COMMAND_ITEM_SET_FAILURE = "commands." + ClaySoldiersCommon.MOD_ID + ".spawn_item_set.failure";

    public static final String COMMAND_TEAM_LOYALTY_FAILURE = "commands." + ClaySoldiersCommon.MOD_ID + ".team_loyalty.set.failure";
    public static final String COMMAND_TEAM_LOYALTY_REMOVE_FAILURE = "commands." + ClaySoldiersCommon.MOD_ID + ".team_loyalty.remove.failure";
    public static final String COMMAND_TEAM_LOYALTY_REMOVE = "commands." + ClaySoldiersCommon.MOD_ID + ".team_loyalty.remove.success";
    public static final String COMMAND_TEAM_LOYALTY_SET = "commands." + ClaySoldiersCommon.MOD_ID + ".team_loyalty.set.success";


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> csrCommand = Commands.literal(ClaySoldiersCommon.MOD_ID);

        csrCommand.then(Commands.literal("items")
                .requires(c -> c.hasPermission(2))
                .then(Commands.argument("set", DefaultedResourceLocationArgument.itemType(context))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(c -> spawnItems(c, true))
                                .then(Commands.literal("uniform")
                                        .executes(c -> spawnItems(c, false))
                                ))
                )
        );


        csrCommand.then(Commands.literal("team")
                .then(Commands.literal("set")
                        .requires(c -> c.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(Commands.argument("team", DefaultedResourceLocationArgument.all(context))
                                        .executes(ClaySoldierCommands::setTeamCommand)
                                )
                        )
                        .then(Commands.literal("item")
                                .then(Commands.argument("team", DefaultedResourceLocationArgument.all(context))
                                        .executes(ClaySoldierCommands::setTeamHandCommand)
                                )
                        )
                )
                .then(Commands.literal("loyalty")
                        .requires(c -> c.hasPermission(0))
                        .executes(ClaySoldierCommands::showTeamAllegiance)
                        .then(Commands.literal("set")
                                .requires(c -> c.hasPermission(4))
                                .then(Commands.argument("team", DefaultedResourceLocationArgument.all(context))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(c -> setTeamLoyalty(c, EntityArgument.getPlayer(c, "player")))
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .requires(c -> c.hasPermission(4))
                                .then(Commands.argument("team", DefaultedResourceLocationArgument.all(context))
                                        .executes(c -> setTeamLoyalty(c, null))

                                )
                        )
                )
        );

        csrCommand.then(Commands.literal("boss")
                .requires(c -> c.hasPermission(2))
                .then(Commands.literal("normal")
                        .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.NORMAL, 0, null, false))
                        .then(Commands.argument("team", DefaultedResourceLocationArgument.all(context))
                                .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.NORMAL, 0, DefaultedResourceLocationArgument.key("team", c), false))
                                .then(Commands.literal("waxed")
                                        .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.NORMAL, 0, DefaultedResourceLocationArgument.key("team", c), true))
                                )
                        )
                        .then(Commands.literal("waxed")
                                .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.NORMAL, 0, null, true))
                        )
                )
                .then(Commands.literal("vampire")
                        .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.VAMPIRE, 0, null, false))
                        .then(Commands.argument("team", DefaultedResourceLocationArgument.all(context))
                                .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.VAMPIRE, 0, DefaultedResourceLocationArgument.key("team", c), false))
                                .then(Commands.literal("waxed")
                                        .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.VAMPIRE, 0, DefaultedResourceLocationArgument.key("team", c), true))
                                )
                        )
                        .then(Commands.literal("waxed")
                                .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.VAMPIRE, 0, null, true))
                        )
                )
                .then(Commands.literal("random")
                        .then(Commands.argument("weight", IntegerArgumentType.integer(0, 25))
                                .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.RANDOM, IntegerArgumentType.getInteger(c, "weight"), null, false))
                        )
                )
        );


        dispatcher.register(csrCommand);
    }

    private static int spawnItems(CommandContext<CommandSourceStack> command, boolean random) {
        CommandSourceStack source = command.getSource();

        SoldierItemType type = command.getSource().registryAccess().registryOrThrow(ModRegistries.SOLDIER_ITEM_TYPES).get(command.getArgument("set", ResourceLocation.class));
        if (type == null) {
            source.sendFailure(Component.translatable(COMMAND_ITEM_SET_ERROR));
            return -1;
        }

        int count = IntegerArgumentType.getInteger(command, "amount");

        Vec3 pos = source.getPosition();
        RandomSource randomSource = random ? source.getLevel().getRandom() : RandomSource.create(42);
        var list = type.getItems(randomSource, count);
        if (list.isEmpty()) {
            source.sendFailure(Component.translatable(COMMAND_ITEM_SET_FAILURE));
            return -1;
        }

        Containers.dropContents(source.getLevel(), new BlockPos((int) pos.x, (int) pos.y, (int) pos.z), list);
        source.sendSuccess(type::getDisplayName, true);
        return 1;
    }

    private static int setTeamCommand(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        List<ClayMobTeamOwnerEntity> entities = EntityArgument.getEntities(command, "targets")
                .stream().filter(ClayMobTeamOwnerEntity.class::isInstance).map(ClayMobTeamOwnerEntity.class::cast).toList();
        RegistryAccess registries = command.getSource().registryAccess();
        ResourceLocation team = DefaultedResourceLocationArgument.key("team", command);
        for (var entity : entities) {
            entity.setClayTeamType(team);
        }

        if (entities.size() == 1) {
            command.getSource().sendSuccess(() -> Component.translatable(COMMAND_TEAM_SINGLE_SUCCESS, ClayMobTeamManger.getFromKeyAssumeValid(team, registries).getDisplayNameWithColor(ColorHelper::getColorStatic)), true);
        } else if (entities.size() > 1) {
            command.getSource().sendSuccess(() -> Component.translatable(COMMAND_TEAM_MULTIPLE_SUCCESS, entities.size(), ClayMobTeamManger.getFromKeyAssumeValid(team, registries).getDisplayNameWithColor(ColorHelper::getColorStatic)), true);
        } else {
            command.getSource().sendFailure(Component.translatable(COMMAND_TEAM_FAILURE));
        }

        return entities.size();
    }

    private static int setTeamLoyalty(CommandContext<CommandSourceStack> command, @Nullable Player player) {
        ResourceLocation teamId = DefaultedResourceLocationArgument.key("team", command);
        Component team = ClayMobTeamManger.getFromKeyOrError(teamId, command.getSource().registryAccess()).getDisplayNameWithColor(ColorHelper::getColorStatic);

        if (TeamLoyaltyManger.setTeamPlayer(command.getSource().getLevel(), teamId, player)) {
            if (player == null) {
                command.getSource().sendSuccess(() -> Component.translatable(COMMAND_TEAM_LOYALTY_REMOVE, team), true);
            } else {
                command.getSource().sendSuccess(() -> Component.translatable(COMMAND_TEAM_LOYALTY_SET, team, player.getDisplayName()), true);
            }
            return 1;
        }

        if (player == null) {
            command.getSource().sendFailure(Component.translatable(COMMAND_TEAM_LOYALTY_REMOVE_FAILURE, team));
        } else {
            command.getSource().sendFailure(Component.translatable(COMMAND_TEAM_LOYALTY_FAILURE, team, player.getDisplayName()));

        }
        return -1;
    }

    private static int setTeamHandCommand(CommandContext<CommandSourceStack> command) {
        ResourceLocation team = DefaultedResourceLocationArgument.key("team", command);
        var player = command.getSource().getPlayer();
        if (player == null) {
            command.getSource().sendFailure(Component.translatable(COMMAND_EXECUTED_BY_PLAYER));
            return -1;
        }
        int set = -1;
        var mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        var offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (itemIsClayDoll(mainHand)) {
            ClaySoldierSpawnItem.setClayMobTeam(mainHand, team, command.getSource().registryAccess());
            set = 1;
        }
        if (itemIsClayDoll(offHand)) {
            ClaySoldierSpawnItem.setClayMobTeam(offHand, team, command.getSource().registryAccess());
            set = set == -1 ? 1 : 2;
        }

        command.getSource().sendSuccess(() -> Component.translatable(COMMAND_TEAM_ITEM_SUCCESS, ClayMobTeamManger.getFromKeyAssumeValid(team, command.getSource().registryAccess()).getDisplayNameWithColor(ColorHelper::getColorStatic)), true);

        return set;
    }

    private static boolean itemIsClayDoll(ItemStack stack) {
        return stack.is(ModItems.CLAY_SOLDIER.asItem());
    }

    private static int summonBossClaySoldier(CommandContext<CommandSourceStack> command, ClaySoldierBossEquipment equipment, int weight, @Nullable ResourceLocation team, boolean waxed) {
        var boss = ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get().create(command.getSource().getLevel());
        if (boss == null) {
            return -1;
        }
        boss.moveTo(command.getSource().getPosition());
        equipment.setUp(boss, weight, team, waxed);
        command.getSource().getLevel().addFreshEntity(boss);
        return 1;
    }

    private static int showTeamAllegiance(CommandContext<CommandSourceStack> command) {
        ServerLevel level = command.getSource().getLevel();
        Counter counter = new Counter();
        var teamDataMap = TeamLoyaltyManger.getTeamPlayerData(level);

        teamDataMap.forEach((team, playerData) -> {
            Player player = level.getPlayerByUUID(playerData.getUUID());
            var teamName = ClayMobTeamManger.getFromKeyAssumeValid(team, command.getSource().registryAccess()).getDisplayNameWithColor(ColorHelper::getColorStatic);
            counter.add();
            if (player != null) {
                teamDataMap.updatePlayerName(team, player);
                command.getSource().sendSuccess(() -> Component.translatable(COMMAND_SHOW_TEAM_ALLEGIANCE_SUCCESS, teamName, player.getDisplayName()), false);
            } else {
                command.getSource().sendSuccess(() -> Component.translatable(COMMAND_SHOW_TEAM_ALLEGIANCE_SUCCESS, teamName, playerData.getLastDisplayName()), false);
            }
        });
        if (counter.count == 0) {
            command.getSource().sendSuccess(() -> Component.translatable(COMMAND_SHOW_TEAM_ALLEGIANCE_EMPTY), false);
        }

        return counter.count - 1;
    }

    private static class Counter {
        int count = 0;

        private void add() {
            count++;
        }
    }
}