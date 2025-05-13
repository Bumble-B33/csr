package net.bumblebee.claysoldiers.datagen;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.datagen.api.BootstrapHelper;
import net.bumblebee.claysoldiers.init.ModDamageTypes;
import net.bumblebee.claysoldiers.init.ModEnchantments;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.soldieritemtypes.DefaultSoldierItemTypes;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMap;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class ModDatapackProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder();
    public static final RegistrySetBuilder BUILDER_BUILTIN = new RegistrySetBuilder();

    public static final String LANGUAGE_PREFIX = "clay_soldier_blueprint.data_name.";
    public static final String SMALL_HOUSE_LANG = LANGUAGE_PREFIX + "small_house";
    public static final String SMALL_FARM_LANG = LANGUAGE_PREFIX + "small_farm";
    public static final String LARGE_HOUSE_LANG = LANGUAGE_PREFIX + "large_house";
    private final String name;


    public ModDatapackProvider(PackOutput output, RegistrySetBuilder builder, CompletableFuture<HolderLookup.Provider> registries, String name) {
        super(output, registries, builder, Collections.singleton(ClaySoldiersCommon.MOD_ID));
        this.name = name;
    }

    @Override
    public String getName() {
        return super.getName() + name;
    }

    public static ModDatapackProvider builtin(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new ModDatapackProvider(output, BUILDER_BUILTIN, registries, "builtin");
    }

    public static ModDatapackProvider datapack(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new ModDatapackProvider(output, BUILDER, registries, "datapack");
    }

    static {
        BUILDER_BUILTIN.add(Registries.DAMAGE_TYPE, ModDamageTypes::boostrap);
        BUILDER_BUILTIN.add(Registries.ENCHANTMENT, ModEnchantments::boostrap);
        BUILDER_BUILTIN.add(ModRegistries.BLUEPRINTS, (context) -> new BootstrapHelper.Blueprint(context, ClaySoldiersCommon.MOD_ID) {
            @Override
            protected void gather() {
                register("small_house", ResourceLocation.withDefaultNamespace("village/plains/houses/plains_small_house_1"), SMALL_HOUSE_LANG, 0);
                register("small_farm", ResourceLocation.withDefaultNamespace("village/plains/houses/plains_small_farm_1"), SMALL_FARM_LANG, 0.1f);
                register("large_house", ResourceLocation.withDefaultNamespace("village/plains/houses/plains_medium_house_1"), LARGE_HOUSE_LANG, 0.2f);
            }
        });
        BUILDER.add(ModRegistries.SOLDIER_ITEM_TYPES, DefaultSoldierItemTypes::registerAll);
        BUILDER.add(ModRegistries.CLAY_MOB_TEAMS, context -> new BootstrapHelper.ClayTeam(context, ClaySoldiersCommon.MOD_ID) {
            @Override
            protected void gather() {
                register(ClayMobTeamManger.DEFAULT_TYPE.getPath(), ClayMobTeam.of("Normal", ColorHelper.EMPTY).build());
                register(ClayMobTeamManger.NO_TEAM_TYPE.getPath(), ClayMobTeam.of("No Team", ColorHelper.EMPTY).allowFriendlyFire().build());

                register("white", "White", 16383998, Items.WHITE_DYE);
                register("orange", "Orange", 16351261, Items.ORANGE_DYE);
                register("magenta", "Magenta", 13061821, Items.MAGENTA_DYE);
                register("light_blue", "Light Blue", 3847130, Items.LIGHT_BLUE_DYE);
                register("yellow", "Yellow", 16701501, Items.YELLOW_DYE);
                register("lime", "Lime", 8439583, Items.LIME_DYE);
                register("pink", "Pink", 15961002, Items.PINK_DYE);
                register("gray", "Gray", 4673362, Items.GRAY_DYE);
                register("light_gray", "Light Gray", 10329495, Items.LIGHT_GRAY_DYE);
                register("cyan", "Cyan", 1481884, Items.CYAN_DYE);
                register("purple", "Purple", 8991416, Items.PURPLE_DYE);
                register("blue", "Blue", 3949738, Items.BLUE_DYE);
                register("brown", "Brown", 8606770, Items.BROWN_DYE);
                register("green", "Green", 6192150, Items.GREEN_DYE);
                register("red", "Red", 11546150, Items.RED_DYE);
                register("black", "Black", 1908001, Items.BLACK_DYE);
                register("jeb_", ClayMobTeam.of("Jeb_", ColorHelper.jeb()).setProperties(SoldierPropertyMap.builder().size(1.1f).build()).build());
            }
        });
    }
}
