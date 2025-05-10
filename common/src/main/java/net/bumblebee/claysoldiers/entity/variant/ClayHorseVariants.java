package net.bumblebee.claysoldiers.entity.variant;

import com.google.common.collect.Maps;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.item.claymobspawn.MultiSpawnItem;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.function.Supplier;

public enum ClayHorseVariants implements NameableVariant {
    CAKE(0,"cake", 0xE81D1D, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_horse/cake.png")),
    GRASS(1,"grass", 0x81B052, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_horse/grass.png")),
    SNOW(2,"snow", 0xD7E1E1, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_horse/snow.png")),
    MYCELIUM(3,"mycelium", 0x6F6262, ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "textures/entity/clay_horse/mycelium.png"));

    public static final EnumMap<ClayHorseVariants, Supplier<MultiSpawnItem<?>>> CLAY_HORSE_ITEM_BY_VARIANT = Maps.newEnumMap(ClayHorseVariants.class);
    public static final EnumMap<ClayHorseVariants, Supplier<MultiSpawnItem<?>>> CLAY_PEGASUS_ITEM_BY_VARIANT = Maps.newEnumMap(ClayHorseVariants.class);


    private final int id;
    private final String name;
    private final int pouchColor;
    private final ResourceLocation textureLocation;

    ClayHorseVariants(int id, String name, int pouchColor, ResourceLocation textureLocation) {
        this.id = id;
        this.name = name;
        this.pouchColor = pouchColor;
        this.textureLocation = textureLocation;
    }

    @Override
    public int getPouchColor() {
        return pouchColor;
    }

    public int getId() {
        return id;
    }

    public String getVariantName() {
        return name;
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public static ClayHorseVariants getById(int id) {
        for (ClayHorseVariants clayHorseVariant : ClayHorseVariants.values()) {
            if (id == clayHorseVariant.id) {
                return clayHorseVariant;
            }
        }
        return CAKE;
    }

    public static Supplier<MultiSpawnItem<?>> clayHorseByVariant(ClayHorseVariants variant) {
        return CLAY_HORSE_ITEM_BY_VARIANT.get(variant);
    }
    public static Supplier<MultiSpawnItem<?>> clayPegasusByVariant(ClayHorseVariants variant) {
        return CLAY_PEGASUS_ITEM_BY_VARIANT.get(variant);
    }
}
