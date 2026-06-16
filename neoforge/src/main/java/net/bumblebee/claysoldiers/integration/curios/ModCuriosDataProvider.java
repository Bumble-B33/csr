package net.bumblebee.claysoldiers.integration.curios;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.integration.ExternalMods;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosDataProvider;
import top.theillusivec4.curios.api.CuriosSlotTypes;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ModCuriosDataProvider extends CuriosDataProvider {
    public static final TagKey<Item> CURIOS_HEAD = create(ExternalMods.CURIOS.getName(), "head");
    public static final TagKey<Item> CURIOS_BELT = create(ExternalMods.CURIOS.getName(), "belt");


    public ModCuriosDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(ClaySoldiersCommon.MOD_ID, output, registries);
    }

    public static void addTags(Function<TagKey<Item>, TagAppender<Item, Item>> tag) {
        tag.apply(CURIOS_HEAD).add(ModItems.CLAY_GOGGLES.get(), ModItems.CLAY_SOLDIER.get());
        tag.apply(CURIOS_BELT).add(ModItems.STATOMETER.get());
    }

    @Override
    public void generate(HolderLookup.Provider provider) {
        createEntities("players")
                .addPlayer().addPresetSlots(
                        CuriosSlotTypes.Preset.HEAD,
                        CuriosSlotTypes.Preset.BELT
                );
    }

    private static TagKey<Item> create(String modId, String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modId, name));
    }
}
