package net.bumblebee.claysoldiers.datagen;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class DummyGenerator implements DataProvider {
    private final Path packOutput;
    private final String value;

    public DummyGenerator(Path path, String value) {
        this.packOutput = path;
        this.value = value;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return save(cachedOutput, packOutput.resolve("dummy.json"));
    }

    private CompletableFuture<?> save(CachedOutput cache, Path target) {
        JsonObject json = new JsonObject();
        json.addProperty("info", value);
        return DataProvider.saveStable(cache, json, target);
    }

    @Override
    public String getName() {
        return "Dummy Generator (" + value + ")";
    }
}
