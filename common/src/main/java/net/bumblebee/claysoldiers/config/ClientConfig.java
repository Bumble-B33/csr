package net.bumblebee.claysoldiers.config;

public interface ClientConfig {
    default boolean statItemShowStats() {
        return true;
    }

    default boolean statItemShowCount() {
        return true;
    }

}
