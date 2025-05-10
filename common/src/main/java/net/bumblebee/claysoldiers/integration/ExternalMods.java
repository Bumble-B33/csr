package net.bumblebee.claysoldiers.integration;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;

import java.util.function.Supplier;

public enum ExternalMods {
    ACCESSORIES("accessories"),
    CURIOS("curios"),
    CREATE("create"),
    SBL("smartbrainlib");

    private final String name;

    ExternalMods(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isLoaded() {
        return ClaySoldiersCommon.PLATFORM.isModLoaded(name);
    }

    public void ifLoaded(Supplier<Runnable> action) {
        if (isLoaded()) {
            action.get().run();
        }
    }

    public <T> T ifLoadedOrElse(Supplier<T> loaded, T notLoaded) {
        return isLoaded() ? loaded.get() : notLoaded;
    }
}
