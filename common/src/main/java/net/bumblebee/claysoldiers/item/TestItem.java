package net.bumblebee.claysoldiers.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntity;
import net.bumblebee.claysoldiers.entity.boss.ClaySoldierBossEquipment;
import net.bumblebee.claysoldiers.entity.goal.workgoal.dig.DigBreakManger;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.team.TeamLoyaltyManger;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TestItem extends Item {
    public static final Logger LOGGER = LoggerFactory.getLogger(ClaySoldiersCommon.MOD_NAME + " Debug");

    public TestItem(Properties pProperties) {
        super(pProperties.component(ModDataComponents.DEBUG_ITEM_MODE.get(), Mode.TEAM));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());

        if (context.getLevel() instanceof ServerLevel serverLevel) {
            log(serverLevel.getBlockState(context.getClickedPos()), List.of(
                    "PoiType: " + serverLevel.getPoiManager().getType(context.getClickedPos()),
                    "Occupants: " + (serverLevel.getPoiManager().getType(context.getClickedPos()).map(h -> h.value().maxTickets()).orElse(0) - serverLevel.getPoiManager().getFreeTickets(context.getClickedPos()))
                    )
            );
        }

        if (blockEntity instanceof HamsterWheelBlockEntity hamsterWheel) {
            var data = hamsterWheel.getSoldierData();
            log(hamsterWheel, List.of(data == null ? "HamsterWheelSoldierData(null)" : data.toString()));
        }

        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player pPlayer, InteractionHand pUsedHand) {

        var itemInHand = pPlayer.getItemInHand(pUsedHand);
        System.out.println((level.isClientSide ? "Client " : "Server ") + HamsterWheelBlockEntity.withSoldiers);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getPoiManager().getInSquare(h -> h.is(ModTags.PoiTypes.SOLDIER_CONTAINER), pPlayer.getOnPos(), 10, PoiManager.Occupancy.ANY);
        }

        if (pPlayer.isCrouching()) {
            var offHand = pPlayer.getItemInHand(InteractionHand.OFF_HAND);

            if (pUsedHand == InteractionHand.MAIN_HAND && !offHand.isEmpty()) {
                LOGGER.info("{}---: {}", level.isClientSide ? "Client" : "Server", offHand.getItem());
                LOGGER.info("Holdable: {}", ClaySoldiersCommon.DATA_MAP.getEffect(offHand));
                LOGGER.info("Wearable: {}", ClaySoldiersCommon.DATA_MAP.getArmor(offHand));
                LOGGER.info("Item Poi: {}", ClaySoldiersCommon.DATA_MAP.getItemPoi(offHand));
                LOGGER.info("Block Poi: {}", ClaySoldiersCommon.DATA_MAP.getBlockPoi(offHand));
                LOGGER.info("---");
            } else {
                cycleMode(itemInHand, pPlayer);
            }

        } else {
            var mode = pPlayer.getItemInHand(pUsedHand).get(ModDataComponents.DEBUG_ITEM_MODE.get());
            if (mode == null) {
                mode = Mode.TEAM;
            }
            List<String> info = new ArrayList<>();
            info.add((level.isClientSide() ? "Client---" : "Server---"));
            mode.getInfo(level, pPlayer, info);
            info.add("---");
            info.forEach(LOGGER::info);

        }
        return InteractionResultHolder.sidedSuccess(itemInHand, level.isClientSide());
    }

    private static void cycleMode(ItemStack itemInHand, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Mode newMode = switch (itemInHand.get(ModDataComponents.DEBUG_ITEM_MODE.get())) {
                case TEAM -> Mode.WORLD;
                case WORLD -> Mode.CONFIG;
                case CONFIG -> Mode.BLUEPRINT;
                case BLUEPRINT -> Mode.TAGS;
                case TAGS -> Mode.BOSS;
                case BOSS -> Mode.TEAM;
                case null -> Mode.TEAM;
            };
            itemInHand.set(ModDataComponents.DEBUG_ITEM_MODE.get(), newMode);
            serverPlayer.sendSystemMessage(newMode.getDisplayName());

        }
    }

    public static boolean isBlueprintEnabled(Level level) {
        return ClaySoldiersCommon.COMMON_HOOKS.isBlueprintEnabled(level.enabledFeatures());
    }

    public static long wheelSpeed() {
        return ClaySoldiersCommon.COMMON_HOOKS.getHamsterWheelSpeed();
    }

    interface InfoGenerator<L extends Level> {
        InfoGenerator<Level> EMPTY = (l, p, infoList) -> {};
        InfoGenerator<ServerLevel> EMPTY_SERVER = (l, p, infoList) -> {};

        void appendInfo(L level, Player player, List<String> infoList);
    }

    public enum Mode implements StringRepresentable {
        TEAM("team", ChatFormatting.DARK_GREEN,
                ((level, player, list) -> {
                    list.add("Custom Reg and Level: " + level.registryAccess().registry(ModRegistries.CLAY_MOB_TEAMS));
                    list.add("Teams: " + ClayMobTeamManger.getAllKeys(level.registryAccess()));
                    list.add("From Item: " + ClayMobTeamManger.getFromItemMap());
                }),
                (s, p, info) -> info.add("Loyalty: " + TeamLoyaltyManger.getTeamPlayerData(s)),
                (c, p, info) -> info.add("Loyalty: " + TeamLoyaltyManger.getClientTeamPlayerData())
        ),
        WORLD("world", ChatFormatting.BLUE, InfoGenerator.EMPTY, ((level, player, list) -> {
            list.add("BreakData: " + DigBreakManger.get());
            list.add("DataPack");
            var packRepository = level.getServer().getPackRepository();
            list.add("Selected: " + packRepository.getSelectedIds().stream().filter(id -> id.contains("csr") || id.contains("blueprint")).toList());
            list.add("Available: " + packRepository.getSelectedIds().stream().filter(id -> id.contains("csr") || id.contains("blueprint")).toList());
            packRepository.getAvailablePacks().stream().filter(p -> p.getId().contains("csr") || p.getId().contains("blueprint")).forEach(pack -> list.add("Pack(%s [%s], source:%s)".formatted(
                    pack.getDescription().getString(),
                    pack.getId(),
                    pack.getPackSource().decorate(Component.literal(ClaySoldiersCommon.MOD_ID)).getString()
            )));

        }), InfoGenerator.EMPTY),
        CONFIG("config", ChatFormatting.YELLOW, (l, p, infos) -> {
            infos.add("Config: MenuModify: %s | WheelSpeed: %s | Blueprint: %s".formatted(ClaySoldiersCommon.claySolderMenuModify ? "Allowed" : "Disabled", wheelSpeed(), isBlueprintEnabled(l) ? "Enabled" : "Disabled"));
            infos.add("Caps: " + ClaySoldiersCommon.CAPABILITY_MANGER.toString());
        }),
        BLUEPRINT("blueprint", ChatFormatting.AQUA, (l, p, info) -> {
            var reg = l.registryAccess().registryOrThrow(ModRegistries.BLUEPRINTS);
            info.add("Registry(%s): %s".formatted(reg.size(), reg.keySet()));
            info.add("Content: " + reg.holders().map(Holder::value).toList());
        }),
        TAGS("soldier_item_tags", ChatFormatting.DARK_PURPLE, (l, p, info) -> {
            var reg = l.registryAccess().registry(ModRegistries.SOLDIER_ITEM_TYPES);
            info.add("Type Reg: " + reg);
            info.add("Values: " + reg.map(r -> r.stream().toList()));
            info.add("Entries: " + reg.map(Registry::entrySet));

            info.add("Generators: " + ModRegistries.ITEM_GENERATORS_REGISTRY.stream().toList());
            info.add("Entries: " + ModRegistries.ITEM_GENERATORS_REGISTRY.entrySet());
        }),
        BOSS("boss", ChatFormatting.RED, InfoGenerator.EMPTY, (serverLevel, p, info) -> {
            var boss = ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get().create(serverLevel);
            if (boss == null) {
                info.add("Error creating Boss");
                return;
            }
            boss.moveTo(p.position());
            ClaySoldierBossEquipment.RANDOM.setUp(boss, 8, null, false);
            if (serverLevel.addFreshEntity(boss)) {
                info.add("Successfully spawned Boss");
            } else {
                info.add("Error spawning Boss");
            }

        }, InfoGenerator.EMPTY);

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);
        public static final StreamCodec<ByteBuf, Mode> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

        private final String serializedName;
        private final String capitalizedName;
        private final ChatFormatting chatFormatting;
        private final InfoGenerator<Level> toInfo;
        private final InfoGenerator<ServerLevel> server;
        private final InfoGenerator<Level> client;

        Mode(String serializedName, ChatFormatting chatFormatting, InfoGenerator<Level> toInfo) {
            this(serializedName, chatFormatting, toInfo, InfoGenerator.EMPTY_SERVER, InfoGenerator.EMPTY);
        }

        Mode(String serializedName, ChatFormatting chatFormatting, InfoGenerator<Level> toInfo, InfoGenerator<ServerLevel> server, InfoGenerator<Level> client) {
            this.serializedName = serializedName;
            this.chatFormatting = chatFormatting;
            this.toInfo = toInfo;
            this.server = server;
            this.client = client;
            StringBuilder builder = new StringBuilder();
            for (String i : serializedName.replace('_', ' ').split(" ")) {
                builder.append(i.replaceFirst(i.charAt(0) + "", (i.charAt(0) + "").toUpperCase()));
            }
            this.capitalizedName = builder.toString();
        }

        public void getInfo(Level level, Player player, List<String> info) {
            toInfo.appendInfo(level, player, info);
            if (level instanceof ServerLevel serverLevel) {
                server.appendInfo(serverLevel, player, info);
            } else {
                client.appendInfo(level, player, info);
            }
        }

        public Component getDisplayName() {
            return Component.literal(capitalizedName).withStyle(chatFormatting);
        }

        @Override
        public @NotNull String getSerializedName() {
            return serializedName;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Right-Click Logs Useful Information to the console").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("Sneak-Right-Click to change modes").withStyle(ChatFormatting.GOLD));
        var mode = stack.get(ModDataComponents.DEBUG_ITEM_MODE.get());
        if (mode != null) {
            tooltipComponents.add(Component.literal("Mode: ").withStyle(ChatFormatting.DARK_GRAY).append(mode.getDisplayName()));
        } else {
            tooltipComponents.add(Component.literal("Mode: Unselected").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static <T> void log(T entity, List<String> infos) {
        LOGGER.info("{}---", entity);
        infos.forEach(LOGGER::info);
        LOGGER.info("---");
    }
}
