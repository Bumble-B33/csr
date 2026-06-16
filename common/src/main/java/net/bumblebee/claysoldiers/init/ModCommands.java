package net.bumblebee.claysoldiers.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.ClayMobTeamOwnerEntity;
import net.bumblebee.claysoldiers.entity.common.boss.ClaySoldierBossEquipment;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.soldieritemtypes.SoldierItemType;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.team.loyalty.TeamLoyaltyManger;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ModCommands {
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
    public static final String COMMAND_TEAM_LOYALTY_DISABLE_FAILURE = "commands." + ClaySoldiersCommon.MOD_ID + ".team_loyalty.disabled.failure";

    public static final String COMMAND_TEAM_LOYALTY_REMOVE_FAILURE = "commands." + ClaySoldiersCommon.MOD_ID + ".team_loyalty.remove.failure";
    public static final String COMMAND_TEAM_LOYALTY_REMOVE = "commands." + ClaySoldiersCommon.MOD_ID + ".team_loyalty.remove.success";
    public static final String COMMAND_TEAM_LOYALTY_SET = "commands." + ClaySoldiersCommon.MOD_ID + ".team_loyalty.set.success";

    public static final String ENABLING_DATAPACK = "commands." + ClaySoldiersCommon.MOD_ID + ".datapack.enabling";
    public static final String DISABLING_DATAPACK = "commands." + ClaySoldiersCommon.MOD_ID + ".datapack.disabing";


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> csrCommand = Commands.literal(ClaySoldiersCommon.MOD_ID);

        csrCommand.then(Commands.literal("items")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("set", createItemTypeArgument(context))
                        .then(Commands.argument("amountRequired", IntegerArgumentType.integer(1))
                                .executes(c -> spawnItems(c, true))
                                .then(Commands.literal("uniform")
                                        .executes(c -> spawnItems(c, false))
                                ))
                )
        );


        csrCommand.then(Commands.literal("team")
                .then(Commands.literal("set")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(Commands.argument("team", createTeamArgument(context))
                                        .executes(ModCommands::setTeamCommand)
                                )
                        )
                        .then(Commands.literal("item")
                                .then(Commands.argument("team", createTeamArgument(context))
                                        .executes(ModCommands::setTeamHandCommand)
                                )
                        )
                )
                .then(Commands.literal("loyalty")
                        .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                        .executes(ModCommands::showTeamAllegiance)
                        .then(Commands.literal("set")
                                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                                .then(Commands.argument("team", createTeamArgument(context))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(c -> setTeamLoyalty(c, EntityArgument.getPlayer(c, "player")))
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                                .then(Commands.argument("team", createTeamArgument(context))
                                        .executes(c -> setTeamLoyalty(c, null))

                                )
                        )
                )
        );

        csrCommand.then(Commands.literal("boss")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("normal")
                        .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.NORMAL, 0, null, false))
                        .then(Commands.argument("team", createTeamArgument(context))
                                .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.NORMAL, 0, getTeam(c, "team"), false))
                                .then(Commands.literal("waxed")
                                        .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.NORMAL, 0, getTeam(c, "team"), true))
                                )
                        )
                        .then(Commands.literal("waxed")
                                .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.NORMAL, 0, null, true))
                        )
                )
                .then(Commands.literal("vampire")
                        .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.VAMPIRE, 0, null, false))
                        .then(Commands.argument("team", createTeamArgument(context))
                                .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.VAMPIRE, 0, getTeam(c, "team"), false))
                                .then(Commands.literal("waxed")
                                        .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.VAMPIRE, 0, getTeam(c, "team"), true))
                                )
                        )
                        .then(Commands.literal("waxed")
                                .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.VAMPIRE, 0, null, true))
                        )
                )
                .then(Commands.literal("zombie")
                        .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.ZOMBIE, 0, null, false))
                        .then(Commands.argument("team", createTeamArgument(context))
                                .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.ZOMBIE, 0, getTeam(c, "team"), false))
                                .then(Commands.literal("waxed")
                                        .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.ZOMBIE, 0, getTeam(c, "team"), true))
                                )
                        )
                        .then(Commands.literal("waxed")
                                .executes(c -> summonBossClaySoldier(c, ClaySoldierBossEquipment.ZOMBIE, 0, null, true))
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
        SoldierItemType type;
        try {
            type = getSoldierItemType(command, "set").value();
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.translatable(COMMAND_ITEM_SET_ERROR));
            return -1;
        }

        int count = IntegerArgumentType.getInteger(command, "amountRequired");

        Vec3 pos = source.getPosition();
        RandomSource randomSource = random ? source.getLevel().getRandom() : RandomSource.create(42);
        NonNullList<ItemStack> list = type.getItems(randomSource, count);
        if (list.isEmpty()) {
            source.sendFailure(Component.translatable(COMMAND_ITEM_SET_FAILURE));
            return -1;
        }
        list.forEach(itemStack -> dropDelayedItemStack(source.getLevel(), pos.x, pos.y, pos.z, itemStack));
        source.sendSuccess(type::getDisplayName, true);
        return 1;
    }

    private static void dropDelayedItemStack(Level level, double x, double y, double z, ItemStack stack) {
        double width = EntityType.ITEM.getWidth();
        double d1 = 1.0 - width;
        double d2 = width / 2.0;
        double d3 = Math.floor(x) + level.getRandom().nextDouble() * d1 + d2;
        double d4 = Math.floor(y) + level.getRandom().nextDouble() * d1;
        double d5 = Math.floor(z) + level.getRandom().nextDouble() * d1 + d2;

        while (!stack.isEmpty()) {
            ItemEntity itementity = new ItemEntity(level, d3, d4, d5, stack.split(level.getRandom().nextInt(21) + 10));
            itementity.setDeltaMovement(
                    level.getRandom().triangle(0.0, 0.11485000171139836),
                    level.getRandom().triangle(0.2, 0.11485000171139836),
                    level.getRandom().triangle(0.0, 0.11485000171139836)
            );
            itementity.setPickUpDelay(60);
            level.addFreshEntity(itementity);
        }
    }

    private static int setTeamCommand(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        List<ClayMobTeamOwnerEntity> entities = EntityArgument.getEntities(command, "targets")
                .stream().filter(ClayMobTeamOwnerEntity.class::isInstance).map(ClayMobTeamOwnerEntity.class::cast).toList();
        Holder.Reference<ClayMobTeam> team = getTeam(command, "team");
        for (var entity : entities) {
            entity.setClayTeamType(team);
        }

        if (entities.size() == 1) {
            command.getSource().sendSuccess(() -> Component.translatable(COMMAND_TEAM_SINGLE_SUCCESS, team.value().getDisplayNameWithColor(ColorHelper::getColorStatic)), true);
        } else if (entities.size() > 1) {
            command.getSource().sendSuccess(() -> Component.translatable(COMMAND_TEAM_MULTIPLE_SUCCESS, entities.size(), team.value().getDisplayNameWithColor(ColorHelper::getColorStatic)), true);
        } else {
            command.getSource().sendFailure(Component.translatable(COMMAND_TEAM_FAILURE));
        }

        return entities.size();
    }

    private static int setTeamLoyalty(CommandContext<CommandSourceStack> command, @Nullable Player player) throws CommandSyntaxException {
        Holder.Reference<ClayMobTeam> team = getTeam(command, "team");
        Component teamName = team.value().getDisplayNameWithColor(ColorHelper::getColorStatic);

        if (!team.value().canBeTamed()) {
            command.getSource().sendFailure(Component.translatable(COMMAND_TEAM_LOYALTY_DISABLE_FAILURE, teamName));
            return -1;
        }


        if (TeamLoyaltyManger.setTeamPlayer(command.getSource().getLevel(), team, player)) {
            if (player == null) {
                command.getSource().sendSuccess(() -> Component.translatable(COMMAND_TEAM_LOYALTY_REMOVE, teamName), true);
            } else {
                command.getSource().sendSuccess(() -> Component.translatable(COMMAND_TEAM_LOYALTY_SET, teamName, player.getDisplayName()), true);
            }
            return 1;
        }

        if (player == null) {
            command.getSource().sendFailure(Component.translatable(COMMAND_TEAM_LOYALTY_REMOVE_FAILURE, teamName));
        } else {
            command.getSource().sendFailure(Component.translatable(COMMAND_TEAM_LOYALTY_FAILURE, teamName, player.getDisplayName()));

        }
        return -1;
    }

    private static int setTeamHandCommand(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        var team = getTeam(command, "team");
        var player = command.getSource().getPlayer();
        if (player == null) {
            command.getSource().sendFailure(Component.translatable(COMMAND_EXECUTED_BY_PLAYER));
            return -1;
        }
        int set = -1;
        var mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        var offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (itemIsClayDoll(mainHand)) {
            ClaySoldierSpawnItem.setClayMobTeam(mainHand, team);
            set = 1;
        }
        if (itemIsClayDoll(offHand)) {
            ClaySoldierSpawnItem.setClayMobTeam(offHand, team);
            set = set == -1 ? 1 : 2;
        }

        command.getSource().sendSuccess(() -> Component.translatable(COMMAND_TEAM_ITEM_SUCCESS, team.value().getDisplayNameWithColor(ColorHelper::getColorStatic)), true);

        return set;
    }

    private static boolean itemIsClayDoll(ItemStack stack) {
        return stack.is(ModItems.CLAY_SOLDIER.asItem());
    }

    private static int summonBossClaySoldier(CommandContext<CommandSourceStack> command, ClaySoldierBossEquipment equipment, int weight, @Nullable Holder.Reference<ClayMobTeam> team, boolean waxed) {
        var boss = ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get().create(command.getSource().getLevel(), EntitySpawnReason.COMMAND);
        if (boss == null) {
            return -1;
        }
        boss.snapTo(command.getSource().getPosition());
        var teamId = team == null ? null : team.key();
        equipment.setUp(boss, weight, teamId, waxed);
        command.getSource().getLevel().addFreshEntity(boss);
        return 1;
    }

    private static int showTeamAllegiance(CommandContext<CommandSourceStack> command) {
        ServerLevel level = command.getSource().getLevel();
        Counter counter = new Counter();
        var teamDataMap = TeamLoyaltyManger.getTeamPlayerData(level);

        teamDataMap.forEach((team, playerData) -> {
            Player player = level.getPlayerByUUID(playerData.getUUID());
            var teamName = ClayMobTeamManger.get(team, command.getSource().registryAccess()).orElseThrow().value().getDisplayNameWithColor(ColorHelper::getColorStatic);
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

    private static ResourceArgument<ClayMobTeam> createTeamArgument(CommandBuildContext context) {
        return new ResourceArgument<>(context, ModRegistries.CLAY_MOB_TEAMS);
    }


    private static Holder.Reference<ClayMobTeam> getTeam(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return ResourceArgument.getResource(context, name, ModRegistries.CLAY_MOB_TEAMS);
    }


    private static ResourceArgument<SoldierItemType> createItemTypeArgument(CommandBuildContext context) {
        return new ResourceArgument<>(context, ModRegistries.SOLDIER_ITEM_TYPES);
    }

    private static Holder.Reference<SoldierItemType> getSoldierItemType(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return ResourceArgument.getResource(context, name, ModRegistries.SOLDIER_ITEM_TYPES);
    }
}