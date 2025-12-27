package net.bumblebee.claysoldiers.datagen.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModBlueprintAdvancementProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final List<AdvancementSubProvider> subProviders = List.of(ModAdvancements::generateForBlueprint);
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final String loader;

    public ModBlueprintAdvancementProvider(String loader, PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.pathProvider = output.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
        this.registries = registries;
        this.loader = loader;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput p_254268_) {
        return this.registries.thenCompose(p_323115_ -> {
            Set<ResourceLocation> set = new HashSet<>();
            List<CompletableFuture<?>> list = new ArrayList<>();
            Consumer<AdvancementHolder> consumer = p_339356_ -> {
                if (!set.add(p_339356_.id())) {
                    throw new IllegalStateException("Duplicate advancement " + p_339356_.id());
                } else {
                    Path path = this.pathProvider.json(p_339356_.id());
                    list.add(DataProvider.saveStable(p_254268_, p_323115_, Advancement.CODEC, p_339356_.value(), path));
                }
            };

            for (AdvancementSubProvider advancementsubprovider : this.subProviders) {
                advancementsubprovider.generate(p_323115_, consumer);
            }

            return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public final String getName() {
        return loader + " Blueprint Advancements";
    }
}
