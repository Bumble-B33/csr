package net.bumblebee.claysoldiers.claysoldierchips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.ClayMobSitGoal;
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
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class ClaySoldierChip<T> {
    private static final StreamCodec<RegistryFriendlyByteBuf, ClaySoldierChip.Type<?>> TYPE_STREAM_CODEC = ByteBufCodecs.registry(ModRegistries.CLAY_SOLDIER_MODULES);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClaySoldierChip<?>> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ClaySoldierChip<?> decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            var type = TYPE_STREAM_CODEC.decode(registryFriendlyByteBuf);
            return type.decode(registryFriendlyByteBuf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, ClaySoldierChip<?> claySoldierChip) {
            TYPE_STREAM_CODEC.encode(registryFriendlyByteBuf, claySoldierChip.getType());
            claySoldierChip.encode(registryFriendlyByteBuf);
        }
    };
    private static final Codec<ClaySoldierChip.Type<?>> TYPE_CODEC = ModRegistries.CLAY_SOLDIER_MODULES_REGISTRY.byNameCodec();
    public static final Codec<ClaySoldierChip<?>> CODEC = TYPE_CODEC.dispatch(ClaySoldierChip::getType, Type::createMapCodec);
    protected static final String LANG_PREFIX = "clay_soldier.chip." + ClaySoldiersCommon.MOD_ID + ".";

    public static final String NO_MODI = LANG_PREFIX + ".data.no_additional_modi";

    @Nullable
    private AddonInfo addonInfo;
    protected final T data;
    protected final List<ClaySoldierChipAddon> addons;

    protected ClaySoldierChip(T data, List<ClaySoldierChipAddon> addons) {
        this.data = data;
        this.addons = addons;
    }

    public ClaySoldierChip<T> withSoldier(ProgrammableClaySoldierEntity soldier) {
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

    public abstract Type<T> getType();

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
        appender.accept(" Data: " + data);
    }

    public void readAdditional(ValueInput input) {

    }

    public void saveAdditional(ValueOutput output) {

    }

    protected void encode(RegistryFriendlyByteBuf byteBuf) {
        getType().streamCodec.encode(byteBuf, data);
        ClaySoldierChipAddon.LIST_STREAM_CODEC.encode(byteBuf, addons);
    }

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
    public @Nullable ClaySoldierChip<?> addAddons(List<ClaySoldierChipAddon> addons) {
        if (addons.isEmpty()) {
            return this;
        }

        if (!canApplyAddons(addons)) {
            return null;
        }
        if (this.addons.isEmpty()) {
            return getType().factory.create(data, List.copyOf(addons));
        }

        return getType().factory.create(data, Stream.concat(this.addons.stream(), addons.stream()).toList());
    }

    public boolean canApplyAddons(List<ClaySoldierChipAddon> addonsToApply) {
        if (addonsToApply.size() > getAllowedAddonsCount()) {
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

    public int addonCount(ClaySoldierChipAddon addon) {
        return getAddonCount(getAddons(), addon);
    }

    public static int getAddonCount(Collection<ClaySoldierChipAddon> addons, ClaySoldierChipAddon toFind) {
        int count = 0;
        for (var ad : addons) {
            if (ad.equals(toFind)) {
                count++;
            }
        }

        return count;
    }

    @NotNull
    private AddonInfo getAddonInfo() {
        if (addonInfo == null) {
            addonInfo = getType().addonInfo;
        }
        return addonInfo;
    }

    public boolean is(TagKey<ClaySoldierChip.Type<?>> tag) {
        return getType().is(tag);
    }
    
    @Override
    public String toString() {
        return "%s{%s, addons=%s}".formatted(this.getClass().getSimpleName(), data, getAddons());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ClaySoldierChip<?> that = (ClaySoldierChip<?>) o;
        if (getType() != that.getType()) {
            return false;
        }

        return Objects.equals(data, that.data) && Objects.equals(addons, that.addons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), data, addons);
    }

    public static class Type<T> {
        private final Factory<T> factory;
        private final AddonInfo addonInfo;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;
        private final MapCodec<ClaySoldierChip<T>> mapCodec;

        @Nullable
        private String descriptionId;
        @Nullable
        private Holder.Reference<Type<?>> holder;

        public Type(Factory<T> factory, AddonInfo addonInfo, MapCodec<T> dataCodec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
            this.factory = factory;
            this.addonInfo = addonInfo;
            this.streamCodec = streamCodec;
            this.mapCodec = RecordCodecBuilder.mapCodec(in -> in.group(
                    dataCodec.forGetter(c -> c.data),
                    ClaySoldierChipAddon.LIST_CODEC.optionalFieldOf("addons", List.of()).forGetter(c -> c.addons)
            ).apply(in, factory::create));
        }

        public static Type<Unit> create(Function<List<ClaySoldierChipAddon>, ? extends ClaySoldierChip<Unit>> instance, AddonInfo addonInfo) {
            return new Type<>(
                    (_, addons) -> instance.apply(addons), addonInfo, MapCodec.unit(Unit.INSTANCE), Unit.STREAM_CODEC.cast()
            );
        }

        public static Type<Unit> empty(Supplier<? extends ClaySoldierChip<Unit>> instance) {
            return new Type<>(
                    (_, _) -> instance.get(), AddonInfo.EMPTY, Unit.CODEC.optionalFieldOf("data", Unit.INSTANCE), Unit.STREAM_CODEC.cast()
            );
        }

        private MapCodec<ClaySoldierChip<T>> createMapCodec() {
            return mapCodec;
        }

        public String getDescriptionId() {
            if (descriptionId == null) {
                descriptionId = Util.makeDescriptionId("clay_soldier_module", getOrCreateReference().key().identifier());
            }
            return descriptionId;
        }

        public Component getDisplayName() {
            return Component.translatable(getDescriptionId());
        }

        private Holder.Reference<Type<?>> getOrCreateReference() {
            if (holder == null) {
                holder = ModRegistries.CLAY_SOLDIER_MODULES_REGISTRY.get(
                        ModRegistries.CLAY_SOLDIER_MODULES_REGISTRY.getKey(this)
                ).orElseThrow();
            }
            return holder;
        }

        public ClaySoldierChip<T> decode(RegistryFriendlyByteBuf buf) {
            return factory.create(streamCodec.decode(buf), ClaySoldierChipAddon.LIST_STREAM_CODEC.decode(buf));
        }

        public boolean is(TagKey<ClaySoldierChip.Type<?>> tag) {
            return getOrCreateReference().is(tag);
        }

        @Override
        public String toString() {
            return getDescriptionId();
        }
    }

    public interface Factory<T> {
        ClaySoldierChip<T> create(T data, List<ClaySoldierChipAddon> addons);
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
