package net.bumblebee.claysoldiers.claysoldierchips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.ClayMobSitGoal;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Stream;

public abstract class ClaySoldierChip {
    private static final StreamCodec<RegistryFriendlyByteBuf, ClaySoldierChip.Type> TYPE_STREAM_CODEC = ByteBufCodecs.registry(ModRegistries.CLAY_SOLDIER_MODULES);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaySoldierChip> STREAM_CODEC = StreamCodec.composite(
            TYPE_STREAM_CODEC, ClaySoldierChip::getType,
            ClaySoldierChipAddon.LIST_STREAM_CODEC, ClaySoldierChip::getAddons,
            Type::create
    );
    private static final Codec<ClaySoldierChip.Type> TYPE_CODEC = ModRegistries.CLAY_SOLDIER_MODULES_REGISTRY.byNameCodec();
    public static final Codec<ClaySoldierChip> CODEC = RecordCodecBuilder.create(in -> in.group(
            TYPE_CODEC.fieldOf("type").forGetter(ClaySoldierChip::getType),
            ClaySoldierChipAddon.LIST_CODEC.optionalFieldOf("addons", List.of()).forGetter(ClaySoldierChip::getAddons)
    ).apply(in, Type::create));

    protected static final String LANG_PREFIX = "clay_soldier_chip." + ClaySoldiersCommon.MOD_ID + ".";
    public static final Map<Supplier<ClaySoldierChip.Type>, Item> BY_ITEM = new ConcurrentHashMap<>();

    @Nullable
    private AddonInfo addonInfo;
    protected final List<ClaySoldierChipAddon> addons;
    private final int acceleration;

    protected ClaySoldierChip(List<ClaySoldierChipAddon> addons) {
        this.addons = addons;
        this.acceleration = addons.stream().reduce(0, (count, addon) -> count + addon.getOrDefault(ModDataComponents.CLAY_SOLDIER_CHIP_ADDON_ACCELERATION.get(), 0), Integer::sum);
    }

    public ClaySoldierChip withSoldier(ProgrammableClaySoldierEntity soldier) {
        return this;
    }

    /**
     * {@link #withSoldier(ProgrammableClaySoldierEntity)} will have been called.
     */
    public abstract void addSpecialGoals(ServerLevel level, ProgrammableClaySoldierEntity soldier, BiConsumer<Integer, Goal> goalAdder, BiConsumer<Integer, Goal> targetAdder);

    public void addGoals(ServerLevel level, ProgrammableClaySoldierEntity soldier, BiConsumer<Integer, Goal> goalAdder, BiConsumer<Integer, Goal> targetAdder) {
        addSpecialGoals(level, soldier, goalAdder, targetAdder);
        addDefaultGoals(level, soldier, goalAdder);
    }

    protected void addDefaultGoals(ServerLevel level, ProgrammableClaySoldierEntity soldier, BiConsumer<Integer, Goal> goalAdder) {
        goalAdder.accept(0, new ClayMobSitGoal(soldier));
    }

    public abstract Type getType();

    public boolean requiresItemPickUp(ItemStack stack) {
        return false;
    }

    public boolean requiresItemCarrying(ItemStack stack) {
        return false;
    }

    /**
     * Called when the Soldier dies or is unloaded serverside.
     */
    public void reset() {
    }

    public boolean shouldStayAtWork() {
        return false;
    }

    /**
     *
     * @param soldier the soldier to whom the tooltip belongs or the player viewer.
     * @return the info for the Stat Display and Chip Item
     */
    public @NotNull Component info(@Nullable LivingEntity soldier) {
        return getType().getDisplayName();
    }

    /**
     * Also shown in the stat display
     *
     * @param soldier The Soldier to whom this chip belongs
     */
    public Component getWorkStatusDisplayName(@NotNull ClayMobEntity soldier) {
        return info(soldier);
    }

    public @Nullable Identifier assetId() {
        return null;
    }

    public OptionalInt getItemColorForLayer(ItemLayer layer) {
        return OptionalInt.empty();
    }

    public int getItemAddonColor(int addonIndex) {
        int installedCount = getAddons().size();

        if (addonIndex < installedCount) {
            return 0xEFBF04;
        }

        return addonIndex < getAllowedAddonsCount() ? 0xFFFFFF : getItemColorForLayer(ItemLayer.BASE).orElse(ItemLayer.BASE.getDefaultColor());
    }

    public void appendDebugInfo(Consumer<String> appender) {
        appender.accept("Chip: " + this.getClass().getSimpleName());
        appender.accept(" Addons: " + this.getAddons());
    }

    public void appendItemHoverText(ItemStack stack,  Consumer<Component> tooltipAdder) {}

    // Addons
    public List<ClaySoldierChipAddon> getAddons() {
        return addons;
    }

    /**
     * Applies the given addons. Returns the new {@code ClaySoldierChip} with the Addosn applied. Returns {@code null} if the addons cannot be applied
     *
     * @param addons to apply
     * @return the new {@code ClaySoldierChip}
     */
    public @Nullable ClaySoldierChip addAddons(List<ClaySoldierChipAddon> addons) {
        if (addons.isEmpty()) {
            return this;
        }

        if (!canApplyAddons(addons)) {
            return null;
        }
        if (this.addons.isEmpty()) {
            return getType().create(List.copyOf(addons));
        }

        return getType().create(Stream.concat(this.addons.stream(), addons.stream()).toList());
    }

    public boolean canApplyAddons(List<ClaySoldierChipAddon> addonsToApply) {
        if (addonsToApply.size() > getAllowedAddonsCount() - addons.size()) {
            return false;
        }
        List<ClaySoldierChipAddon> appliedAddons = new ArrayList<>(getAddons());
        List<ClaySoldierChipAddon> remainingAddons = new ArrayList<>(addonsToApply);

        boolean changed;
        do {
            changed = false;

            Iterator<ClaySoldierChipAddon> iterator = remainingAddons.iterator();
            while (iterator.hasNext()) {
                ClaySoldierChipAddon addon = iterator.next();

                if (getAddonInfo().canBeApplied(appliedAddons, addon)) {
                    appliedAddons.add(addon);
                    iterator.remove();
                    changed = true;
                }
            }

        } while (changed);

        return remainingAddons.isEmpty();
    }

    public int getAllowedAddonsCount() {
        return getAddonInfo().getAllowedAddonsCount();
    }

    public boolean hasAddon(ClaySoldierChipAddon addon) {
        return getAddons().contains(addon);
    }

    public int getAccelerationAddonCount() {
        return acceleration;
    }

    @NotNull
    private AddonInfo getAddonInfo() {
        if (addonInfo == null) {
            addonInfo = getType().addonInfo;
        }
        return addonInfo;
    }

    public boolean is(TagKey<ClaySoldierChip.Type> tag) {
        return getType().is(tag);
    }
    
    @Override
    public String toString() {
        return "%s{addons=%s}".formatted(this.getClass().getSimpleName(), getAddons());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ClaySoldierChip that = (ClaySoldierChip) o;
        if (getType() != that.getType()) {
            return false;
        }

        return Objects.equals(addons, that.addons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), addons);
    }

    public static class Type implements ItemLike {
        private final Factory factory;
        private final AddonInfo addonInfo;

        @Nullable
        private String descriptionId;
        @Nullable
        private Holder.Reference<Type> holder;
        private @Nullable Item item;

        public Type(Factory factory, AddonInfo addonInfo) {
            this.factory = factory;
            this.addonInfo = addonInfo;
        }

        private ClaySoldierChip create(List<ClaySoldierChipAddon> addons) {
            return factory.create(addons);
        }

        public static Type empty(Supplier<? extends ClaySoldierChip> instance) {
            return new Type(
                    (_) -> instance.get(), AddonInfo.EMPTY
            );
        }

        public String getDescriptionId() {
            if (descriptionId == null) {
                descriptionId = Util.makeDescriptionId("clay_soldier_chip", getOrCreateReference().key().identifier());
            }
            return descriptionId;
        }

        public Component getDisplayName() {
            return Component.translatable(getDescriptionId());
        }

        private Holder.Reference<Type> getOrCreateReference() {
            if (holder == null) {
                holder = ModRegistries.CLAY_SOLDIER_MODULES_REGISTRY.get(
                        ModRegistries.CLAY_SOLDIER_MODULES_REGISTRY.getKey(this)
                ).orElseThrow();
            }
            return holder;
        }

        public boolean is(TagKey<ClaySoldierChip.Type> tag) {
            return getOrCreateReference().is(tag);
        }

        @Override
        public String toString() {
            return getDescriptionId();
        }

        @Override
        public @NonNull Item asItem() {
            if (item == null) {
                var it = BY_ITEM.entrySet().iterator();
                while (it.hasNext()) {
                    var k = it.next();
                    if (k.getKey().get() == this) {
                        item = k.getValue();
                        it.remove();
                        break;
                    }
                }
            }
            if (item == null) {
                throw new IllegalArgumentException("Item cannot be null");
            }

            return item;
        }
    }

    @FunctionalInterface
    public interface Factory {
        ClaySoldierChip create(List<ClaySoldierChipAddon> addons);
    }

    public enum ItemLayer implements StringRepresentable {
        BASE("base", 0x121212),
        CONNECTION("connection", 0xF9F9F9);

        public static final int MAX_ADDON_LAYERS = 4;

        public static final Codec<ItemLayer> CODEC = StringRepresentable.fromEnum(ItemLayer::values);

        private final String serializedName;
        private final int defaultColor;

        ItemLayer(String serializedName, int defaultColor) {
            this.serializedName = serializedName;
            this.defaultColor = defaultColor;
        }

        public int getDefaultColor() {
            return defaultColor;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
