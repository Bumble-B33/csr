package net.bumblebee.claysoldiers.blueprint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.plan.BlueprintPlan;
import net.bumblebee.claysoldiers.blueprint.plan.ServerBlueprintPlan;
import net.bumblebee.claysoldiers.blueprint.templates.BaseImmutableTemplate;
import net.bumblebee.claysoldiers.blueprint.templates.ImmutableTemplate;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class BlueprintData {
    private static final String BLUEPRINT_DATA_TAG = "BlueprintData";
    public static final Codec<BlueprintData> JSON_CODEC = RecordCodecBuilder.create(in -> in.group(
            Identifier.CODEC.fieldOf("location").forGetter(BlueprintData::structureLocation),
            Codec.STRING.fieldOf("name").forGetter(BlueprintData::name),
            Codec.FLOAT.optionalFieldOf("marking", 0f).forGetter(BlueprintData::marking)
            ).apply(in, BlueprintData::new)
    );
    public static final Codec<ResourceKey<BlueprintData>> KEY_CODEC = ResourceKey.codec(ModRegistries.BLUEPRINTS);


    private final Identifier structureLocation;
    private final String name;
    private final float marking;
    private BaseImmutableTemplate template;
    @Nullable
    private VoxelShape voxelShape;
    private boolean valid = false;
    private Holder.Reference<BlueprintData> reference;

    public BlueprintData(Identifier structureLocation, String name, float marking) {
        this.structureLocation = structureLocation;
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
        if (this.template instanceof ImmutableTemplate) {
            ClaySoldiersCommon.ERROR_HANDLER.debug("Trying to override. Template");
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

    public Optional<ServerBlueprintPlan> createServerPlan(ServerLevel level) {
        return valid ? template.createServer(level) : Optional.empty();
    }

    public Optional<BlueprintPlan> createClientPlan() {
        return valid ? template.createClient() : Optional.empty();
    }

    public void save(ValueOutput tag, HolderLookup.Provider registries) {
        registries.lookupOrThrow(ModRegistries.BLUEPRINTS).listElements().filter(h -> h.value().equals(this)).findAny()
                .ifPresentOrElse(
                        holder -> tag.store(BLUEPRINT_DATA_TAG, KEY_CODEC, holder.key()),
                        () -> ClaySoldiersCommon.LOGGER.error("Error Loading Blueprint Data from Tag"));
    }

    @Nullable
    public static BlueprintData load(ValueInput tag, HolderLookup.Provider pRegistries) {
        Optional<ResourceKey<BlueprintData>> key = tag.read(BLUEPRINT_DATA_TAG, KEY_CODEC);
        if (key.isEmpty()) {
            return null;
        }
        Optional<Holder.Reference<BlueprintData>> holder = pRegistries.lookupOrThrow(ModRegistries.BLUEPRINTS).get(key.orElseThrow());
        if (holder.isEmpty()) {
            ClaySoldiersCommon.LOGGER.error("Tried Loading Blueprint Data that does not exist {}", key);
        }
        return holder.map(Holder::value).orElse(null);
    }

    public Identifier structureLocation() {
        return structureLocation;
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
        return Objects.equals(this.structureLocation, that.structureLocation) &&
                Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structureLocation, name);
    }

    @Override
    public String toString() {
        return "BlueprintData[%s, %s, %s, %s, %s]".formatted(name, structureLocation.getPath(),
                (valid ? "Valid" : "Invalid"),
                (template == null ? "NoTemplate" : template.toShortString()),
                (voxelShape == null ? "NoShape" : "WithShape")
        );
    }

}
