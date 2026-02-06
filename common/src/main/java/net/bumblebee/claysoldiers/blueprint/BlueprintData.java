package net.bumblebee.claysoldiers.blueprint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.templates.BaseImmutableTemplate;
import net.bumblebee.claysoldiers.blueprint.templates.BlueprintPlan;
import net.bumblebee.claysoldiers.blueprint.templates.ServerBlueprintPlan;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class BlueprintData {
    private static final String BLUEPRINT_DATA_TAG = "BlueprintData";
    public static final Codec<BlueprintData> JSON_CODEC = RecordCodecBuilder.create(in -> in.group(
            ResourceLocation.CODEC.fieldOf("location").forGetter(BlueprintData::location),
            Codec.STRING.fieldOf("name").forGetter(BlueprintData::name),
            Codec.FLOAT.optionalFieldOf("marking", 0f).forGetter(BlueprintData::marking)
            ).apply(in, BlueprintData::new)
    );

    private final ResourceLocation location;
    private final String name;
    private final float marking;
    private BaseImmutableTemplate template;
    @Nullable
    private VoxelShape voxelShape;
    private boolean valid = false;
    private Holder.Reference<BlueprintData> reference;

    public BlueprintData(ResourceLocation location, String name, float marking) {
        this.location = location;
        this.name = name;
        this.marking = marking;
    }

    public Component getDisplayName() {
        return Component.translatable(name());
    }



    public void bindStructure(BaseImmutableTemplate template) {
        if (template == null) {
            return;
        }

        this.template = template;
        this.voxelShape = template.getShape();
        this.valid = true;
    }

    /**
     * @return whether this is bound.
     */
    public boolean isValid() {
        return valid;
    }

    public Optional<ServerBlueprintPlan> createServerPlan() {
        return valid ? template.createServer() : Optional.empty();
    }

    public Optional<BlueprintPlan> createClientPlan() {
        return valid ? template.createClient() : Optional.empty();
    }

    public void save(ValueOutput tag, HolderLookup.Provider registries) {
        registries.lookupOrThrow(ModRegistries.BLUEPRINTS).listElements().filter(h -> h.value().equals(this)).findAny()
                .ifPresentOrElse(
                        holder -> tag.store(BLUEPRINT_DATA_TAG, ResourceLocation.CODEC, holder.key().location()),
                        () -> ClaySoldiersCommon.LOGGER.error("Error Loading Blueprint Data from Tag"));
    }

    @Nullable
    public static BlueprintData load(ValueInput tag, HolderLookup.Provider pRegistries) {
        var key = tag.read(BLUEPRINT_DATA_TAG, ResourceLocation.CODEC);
        if (key.isEmpty()) {
            return null;
        }
        var holder = pRegistries.lookupOrThrow(ModRegistries.BLUEPRINTS).get(ResourceKey.create(ModRegistries.BLUEPRINTS, key.orElseThrow()));
        if (holder.isEmpty()) {
            ClaySoldiersCommon.LOGGER.error("Tried Loading Blueprint Data that does not exist {}", key);
        }
        return holder.map(Holder.Reference::value).orElse(null);
    }

    public ResourceLocation location() {
        return location;
    }

    public String name() {
        return name;
    }

    public float marking() {
        return marking;
    }

    public BaseImmutableTemplate getTemplate() {
        if (!valid) {
            throw new IllegalStateException("Tried getting the Template, but data is invalid " + this);
        }
        return template;
    }

    public VoxelShape getShape() {
        if (!valid) {
            throw new IllegalStateException("Tried getting the Shape, but data is invalid " + this);
        }
        return voxelShape;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BlueprintData) obj;
        return Objects.equals(this.location, that.location) &&
                Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, name);
    }

    @Override
    public String toString() {
        return "BlueprintData[%s, %s, %s, %s, %s]".formatted(name, location.getPath(),
                (valid ? "Valid" : "Invalid"),
                (template == null ? "NoTemplate" : template.toShortString()),
                (voxelShape == null ? "NoShape" : "WithShape")
        );
    }

}
