package net.bumblebee.claysoldiers.datagen.api;

import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;


public abstract class BootstrapHelper<T> {
    private final BootstrapContext<T> context;
    private final ResourceKey<Registry<T>> registry;
    private final String modId;

    protected BootstrapHelper(BootstrapContext<T> context, ResourceKey<Registry<T>> registry, String modId) {
        this.context = context;
        this.registry = registry;
        this.modId = modId;
        gather();
    }

    protected void register(ResourceKey<T> key, T value) {
        context.register(key, value);
    }
    protected void register(String  key, T value) {
        register(createKey(key), value);
    }

    protected abstract void gather();

    protected ResourceKey<T> createKey(String name) {
        return ResourceKey.create(registry, ResourceLocation.fromNamespaceAndPath(modId, name));
    }

    public abstract static class Blueprint extends BootstrapHelper<BlueprintData> {
        public Blueprint(BootstrapContext<BlueprintData> context, String modId) {
            super(context,ModRegistries.BLUEPRINTS,  modId);
        }

        protected void register(String key, ResourceLocation structureLocation, String name, float marking) {
            register(key, new BlueprintData(structureLocation, name, marking));
        }
    }

    public abstract static class ClayTeam extends BootstrapHelper<ClayMobTeam> {
        public ClayTeam(BootstrapContext<ClayMobTeam> context, String modId) {
            super(context, ModRegistries.CLAY_MOB_TEAMS, modId);
        }

        protected void register(String id, String displayName, int color, Item getFrom) {
            register(id, ClayMobTeam.of(displayName, ColorHelper.color(color)).setGetFrom(getFrom).build());
        }
    }
}
