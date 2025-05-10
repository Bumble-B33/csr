package net.bumblebee.claysoldiers.team;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ClayMobTeamManger {
    public static final ResourceLocation DEFAULT_TYPE = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "normal");
    public static final ResourceLocation NO_TEAM_TYPE = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "no_team");
    private static final ClayMobTeam DEFAULT = ClayMobTeam.of("Normal", ColorHelper.EMPTY).build();
    private static final ClayMobTeam NO_TEAM = ClayMobTeam.of("NoTeam", ColorHelper.EMPTY).allowFriendlyFire().build();

    private static final Map<Item, ResourceLocation> FROM_ITEM_MAP = new HashMap<>();

    public static final Logger LOGGER = LoggerFactory.getLogger("Clay Soldiers Team Manger");

    public static final ClayMobTeam ERROR = new ErrorClayMobTeam();

    /**
     * Creates a new {@code ClayMobTeamReference}.
     * @param key the key of the team
     * @param ifInValid executed when the given does not belong to a valid team.
     * @return a {@code ClayMobTeamReference} with the given key
     */
    public static IClayMobTeamReference getReferenceOrDefault(ResourceLocation key, RegistryAccess access, Runnable ifInValid) {
        var holder = access.registryOrThrow(ModRegistries.CLAY_MOB_TEAMS).getHolder(key);
        if (holder.isPresent()) {
            return new ClayMobTeamReference(holder.orElseThrow());
        }
        ifInValid.run();
        return new ClayMobTeamReference(getDefault(access));
    }

    private record ClayMobTeamReference(Holder.Reference<ClayMobTeam> base) implements IClayMobTeamReference {
        @Override
        public @NotNull ClayMobTeam value() {
            return base.value();
        }

        @Override
        public @NotNull ResourceLocation key() {
            return base.key().location();
        }
    }

    public static Holder.Reference<ClayMobTeam> getDefault(HolderLookup.Provider access) {
        return access.lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS).get(ResourceKey.create(ModRegistries.CLAY_MOB_TEAMS, DEFAULT_TYPE)).orElseThrow();
    }

    public static Stream<ResourceLocation> getAllKeys(HolderLookup.Provider registryAccess) {
        return registryAccess.lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS).listElementIds().map(ResourceKey::location);
    }

    @NotNull
    public static ClayMobTeam getFromKeyAssumeValid(ResourceLocation key, RegistryAccess access) {
        return Objects.requireNonNull(access.registryOrThrow(ModRegistries.CLAY_MOB_TEAMS).get(key), "Tried accessing in valid team");
    }

    /**
     * Returns the {@code ClayMobTeam} associated with the given key. Returns {@code null} if the key is not associated with any team.
     * @param key the key of the team.
     * @return the {@code ClayMobTeam} associated with the given key
     */
    @Nullable
    public static ClayMobTeam getFromKey(ResourceLocation key, RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(ModRegistries.CLAY_MOB_TEAMS).get(key);
    }

    public static Optional<ClayMobTeam> getOptional(ResourceLocation key, HolderLookup.Provider registryAccess) {
        return registryAccess.lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS).get(create(key)).map(Holder::value);
    }

    public static Optional<Holder.Reference<ClayMobTeam>> getHolder(ResourceLocation key, HolderLookup.Provider registryAccess) {
        return registryAccess.lookupOrThrow(ModRegistries.CLAY_MOB_TEAMS).get(ResourceKey.create(ModRegistries.CLAY_MOB_TEAMS, key));
    }


    /**
     * Returns whether this key is for a valid team.
     */
    public static boolean isValidTeam(ResourceLocation key, RegistryAccess access) {
        return access.registryOrThrow(ModRegistries.CLAY_MOB_TEAMS).containsKey(key);
    }

    @NotNull
    public static ClayMobTeam getFromKeyOrError(@NotNull ResourceLocation key, HolderLookup.Provider access) {
        if (access == null || key == null) {
            return ERROR;
        }
        return access.lookup(ModRegistries.CLAY_MOB_TEAMS)
                .map(r -> r.get(ResourceKey.create(ModRegistries.CLAY_MOB_TEAMS, key)).map(Holder::value).orElse(ERROR)).orElse(ERROR);
    }

    /**
     * Creates a new {@link ModItems#CLAY_SOLDIER Clay Soldier Puppet} with the give {@code ClayMobTeam}.
     *
     * @param id the id of the team
     * @return a new {@link ModItems#CLAY_SOLDIER Clay Soldier Puppet}
     */
    public static ItemStack createStackForTeam(ResourceLocation id, HolderLookup.Provider registries) {
        ItemStack stack = ModItems.CLAY_SOLDIER.get().getDefaultInstance();
        ClaySoldierSpawnItem.setClayMobTeam(stack, id, registries);
        return stack;
    }

    /**
     * Returns a team id associated with the given item.
     */
    @Nullable
    public static ResourceLocation getFromItem(Item item) {
        return FROM_ITEM_MAP.get(item);
    }

    public static void appendFromItemMap(@Nullable Item item, ResourceLocation location) {
        if (item == null) {
            return;
        }

        FROM_ITEM_MAP.put(item, location);
    }

    @UnmodifiableView
    public static Map<Item, ResourceLocation> getFromItemMap() {
        return Map.copyOf(FROM_ITEM_MAP);
    }

    private static ResourceKey<ClayMobTeam> create(ResourceLocation location) {
        return ResourceKey.create(ModRegistries.CLAY_MOB_TEAMS, location);
    }

    private static class ErrorClayMobTeam extends ClayMobTeam {
        protected ErrorClayMobTeam() {
            super("Error", ColorHelper.color(0xFF5555), true, Items.AIR);
        }

        @Override
        public boolean canBeUsed(Player player) {
            return false;
        }
    }

    public static void registerDefault(Registry<ClayMobTeam> registry) {
        boolean defaultType = false;
        boolean noTeamType = false;

        if (registry.get(DEFAULT_TYPE) == null) {
            Registry.register(registry, DEFAULT_TYPE, DEFAULT);
            defaultType = true;
        }
        var noTeam = registry.get(NO_TEAM_TYPE);
        if (noTeam == null) {
            Registry.register(registry, NO_TEAM_TYPE, NO_TEAM);
            noTeamType = true;
        } else if (!noTeam.isFriendlyFireAllowed()) {
            LOGGER.warn("Created {} without friendly-fire enabled, however it probably should be", NO_TEAM);
        }

        if (noTeamType && defaultType) {
            LOGGER.info("Registered {} and {} as they were not present", DEFAULT_TYPE, NO_TEAM_TYPE);
        } else if (noTeamType || defaultType) {
            LOGGER.info("Registered {} as it was not present", noTeamType ? NO_TEAM_TYPE : DEFAULT_TYPE);
        }
    }
}
