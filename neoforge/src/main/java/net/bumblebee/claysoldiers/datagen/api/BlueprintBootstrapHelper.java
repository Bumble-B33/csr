package net.bumblebee.claysoldiers.datagen.api;

import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;

public abstract class BlueprintBootstrapHelper extends BootstrapHelper<BlueprintData> {
    public BlueprintBootstrapHelper(BootstrapContext<BlueprintData> context, String modId) {
        super(context,ModRegistries.BLUEPRINTS,  modId);
    }

    protected void register(String key, ResourceLocation structureLocation, String name, float marking) {
        register(key, new BlueprintData(structureLocation, name, marking));
    }
}
