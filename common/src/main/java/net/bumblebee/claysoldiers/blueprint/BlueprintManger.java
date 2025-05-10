package net.bumblebee.claysoldiers.blueprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.templates.BaseImmutableTemplate;
import net.bumblebee.claysoldiers.blueprint.templates.BlueprintUtil;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintItem;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class BlueprintManger extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final String BLUEPRINT_FILE_PATH = "%s/blueprint".formatted(ClaySoldiersCommon.MOD_ID);
    private static final Logger LOGGER = LogUtils.getLogger();
    private final HolderLookup<Block> blockLookup;
    private final HolderLookup<BlueprintData> blueprintHolders;
    private final BiConsumer<BlueprintManger, ResourceManager> tagLoader;

    public BlueprintManger(HolderLookup<Block> blockLookup, HolderLookup<BlueprintData> blueprintRegistry, BiConsumer<BlueprintManger, ResourceManager> tagLoader) {
        super(GSON, BLUEPRINT_FILE_PATH);
        this.blockLookup = blockLookup;
        this.blueprintHolders = blueprintRegistry;
        this.tagLoader = tagLoader;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonElementMap, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        tagLoader.accept(this, pResourceManager);

        LOGGER.info("Clay Soldiers: Done Loading {} Blueprints", blueprintHolders.listElements().map(Holder::value).toList());
    }

    public void onTagLoad(ResourceManager pResourceManager, Collection<Holder<Block>> blackListedBlocks) {
        var helper = BlueprintUtil.createBlueprintLoader(pResourceManager, blockLookup, blackListedBlocks);

        blueprintHolders.listElements().map(Holder.Reference::value).forEach(data -> {
            helper.apply(data.location()).ifPresentOrElse(
                    data::bindStructure,
                    () -> LOGGER.error("Clay Soldiers: Couldn't load structure {} for Blueprint", data.location())
            );
        });
    }

    public static List<ItemStack> getBlueprintItems(HolderLookup.Provider provider) {
        return provider.lookupOrThrow(ModRegistries.BLUEPRINTS).listElements().filter(h -> h.value().isValid()).map(BlueprintItem::createStackFromData).toList();
    }

    public static ItemStack createBlueprintItem(BlueprintData data, RegistryAccess registryAccess) {
        return BlueprintItem.createStackFromData(registryAccess.registryOrThrow(ModRegistries.BLUEPRINTS).wrapAsHolder(data));
    }

    @ApiStatus.Internal
    public static void setupClient(Map<ResourceLocation, BaseImmutableTemplate> blueprintShapes, RegistryAccess access) {
        var reg = access.registryOrThrow(ModRegistries.BLUEPRINTS);
        reg.holders().forEach(holder -> holder.value().bindStructure(blueprintShapes.get(holder.key().location())));
        reg.holders().forEach(data -> {
            if (!data.value().isValid()) {
                LOGGER.error("Clay Soldiers: Loaded Invalid Blueprint Data on the Client {}", data);
            }
        });

        LOGGER.info("Clay Soldiers: Done Loading {} Blueprints on Client", reg.stream().toList());
    }

    @ApiStatus.Internal
    public static Map<ResourceLocation, BaseImmutableTemplate> getBlueprintShapeData(RegistryAccess registryAccess) {
        Map<ResourceLocation, BaseImmutableTemplate> map = new HashMap<>();
        registryAccess.registryOrThrow(ModRegistries.BLUEPRINTS).holders().filter(h -> h.value().isValid()).forEach(h -> map.put(h.key().location(), h.value().getTemplate()));
        return map;
    }
}
