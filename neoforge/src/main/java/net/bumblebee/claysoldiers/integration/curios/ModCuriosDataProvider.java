package net.bumblebee.claysoldiers.integration.curios;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModCuriosDataProvider extends CuriosDataProvider {
    public ModCuriosDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(ClaySoldiersCommon.MOD_ID, output, registries);
    }

    public static void generateTags(BiConsumer<TagKey<Item>, Item[]> accept) {
        accept.accept(ModTags.Items.CURIOS_HEAD, new Item[]{ModItems.CLAY_GOGGLES.get(), ModItems.CLAY_SOLDIER.get()});
    }

    @Override
    public void generate(HolderLookup.Provider provider) {
        createEntities("players")
                .addPlayer()
                .addSlots("head");
    }
}
