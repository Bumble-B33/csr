package net.bumblebee.claysoldiers.team;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ClayMobTeamManger {
    private static final Identifier DEFAULT_TYPE = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "normal");
    private static final Identifier NO_TEAM_TYPE = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "no_team");

    public static final ResourceKey<ClayMobTeam> DEFAULT_KEY = ResourceKey.create(ModRegistries.CLAY_MOB_TEAMS, DEFAULT_TYPE);
    public static final ResourceKey<ClayMobTeam> NO_TEAM_KEY = ResourceKey.create(ModRegistries.CLAY_MOB_TEAMS, NO_TEAM_TYPE);

    private static final ClayMobTeam DEFAULT = ClayMobTeam.of("Normal", ColorHelper.CLAY_COLOR).build();
    private static final ClayMobTeam NO_TEAM = ClayMobTeam.of("NoTeam", ColorHelper.CLAY_COLOR).allowFriendlyFire().disableTaming().build();

    private static final Map<Item, ResourceKey<ClayMobTeam>> FROM_ITEM_MAP = new HashMap<>();

    public static final Logger LOGGER = LoggerFactory.getLogger("Clay Soldiers Team Manger");

    public static final ClayMobTeam ERROR = new ErrorClayMobTeam();

    public static Holder.Reference<ClayMobTeam> getDefault(HolderLookup.Provider access) {
        return access.get(DEFAULT_KEY).orElseThrow();
    }

    public static Stream<Holder.Reference<ClayMobTeam>> getAll(HolderLookup.Provider registryAccess) {
        return registryAccess.lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS).listElements();
    }

    public static Optional<Holder.Reference<ClayMobTeam>> get(ResourceKey<ClayMobTeam> key, HolderLookup.Provider registryAccess) {
        return registryAccess.lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS).get(key);
    }

    public static Holder.Reference<ClayMobTeam> getOrDefault(@Nullable ResourceKey<ClayMobTeam> key, HolderLookup.Provider registryAccess) {
        if (key == null) {
            return getDefault(registryAccess);
        }
        return registryAccess.lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS).get(key).orElse(getDefault(registryAccess));
    }

    /**
     * Returns whether this key is for a valid team.
     */
    public static boolean isValidTeam(@NotNull ResourceKey<ClayMobTeam> key, RegistryAccess access) {
        return access.lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS).containsKey(key);
    }

    @NotNull
    public static ClayMobTeam getFromKeyOrError(@Nullable ResourceKey<ClayMobTeam> key, @Nullable HolderLookup.Provider access) {
        if (access == null || key == null) {
            return ERROR;
        }
        return access.lookup(ModRegistries.CLAY_MOB_TEAMS)
                .map(r -> r.get(key).map(Holder::value).orElse(ERROR)).orElse(ERROR);
    }

    /**
     * Returns a team id associated with the given item.
     */
    @Nullable
    public static ResourceKey<ClayMobTeam> getFromItem(Item item) {
        return FROM_ITEM_MAP.get(item);
    }

    public static void appendFromItemMap(@Nullable Item item, Identifier location) {
        if (item == null) {
            return;
        }

        FROM_ITEM_MAP.put(item, create(location));
    }

    @UnmodifiableView
    public static Map<Item, ResourceKey<ClayMobTeam>> getFromItemMap() {
        return Map.copyOf(FROM_ITEM_MAP);
    }

    private static ResourceKey<ClayMobTeam> create(Identifier location) {
        return ResourceKey.create(ModRegistries.CLAY_MOB_TEAMS, location);
    }

    private static class ErrorClayMobTeam extends ClayMobTeam {
        protected ErrorClayMobTeam() {
            super("Error", ColorHelper.color(0xFF5555), true, true, Items.AIR);
        }

        @Override
        public boolean canBeUsed(Player player) {
            return false;
        }
    }

    public static void registerDefault(Registry<ClayMobTeam> registry) {
        boolean defaultType = false;
        boolean noTeamType = false;

        //Todo test

        if (registry.get(DEFAULT_KEY).isEmpty()) {
            Registry.register(registry, DEFAULT_KEY, DEFAULT);
            defaultType = true;
        }
        var noTeam = registry.get(NO_TEAM_KEY);
        if (noTeam.isEmpty()) {
            Registry.register(registry, NO_TEAM_KEY, NO_TEAM);
            noTeamType = true;
        } else {
            if (!noTeam.orElseThrow().value().isFriendlyFireAllowed()) {
                LOGGER.warn("Created {} without friendly-fire enabled, however it probably should be", NO_TEAM);
            }
            if (noTeam.orElseThrow().value().canBeTamed()) {
                LOGGER.warn("Created {} without taming disabled, however it probably should be", NO_TEAM);
            }
        }

        if (noTeamType && defaultType) {
            LOGGER.info("Registered {} and {} as they were not present", DEFAULT_KEY.identifier(), NO_TEAM_KEY.identifier());
        } else if (noTeamType || defaultType) {
            LOGGER.info("Registered {} as it was not present", noTeamType ? NO_TEAM_KEY.identifier() : DEFAULT_KEY.identifier());
        }
    }
}
