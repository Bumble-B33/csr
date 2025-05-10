/*
 * Copyright (c) 2021 magistermaks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 */

package net.bumblebee.claysoldiers;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleConfigFabric {
    private static final Logger LOGGER = ClaySoldiersCommon.LOGGER;
    private final HashMap<String, String> config = new HashMap<>();
    private final ConfigRequest request;
    private boolean broken = false;

    public interface DefaultConfig {
        String get(String namespace);

        static String empty(String namespace) {
            return "";
        }
    }

    public static class ConfigRequest {
        private final File file;
        private final String filename;
        private DefaultConfig provider;

        private ConfigRequest(File file, String filename) {
            this.file = file;
            this.filename = filename;
            this.provider = DefaultConfig::empty;
        }

        /**
         * Sets the default config provider, used to generate the
         * config if it's missing.
         *
         * @param provider default config provider
         * @return current config request object
         * @see DefaultConfig
         */
        public ConfigRequest provider(DefaultConfig provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Loads the config from the filesystem.
         *
         * @return config object
         * @see SimpleConfigFabric
         */
        public SimpleConfigFabric request() {
            return new SimpleConfigFabric(this);
        }

        private String getConfig() {
            return provider.get(filename) + "\n";
        }

    }

    /**
     * Creates new config request object, ideally `namespace`
     * should be the name of the mod id of the requesting mod
     *
     * @param filename - name of the config file
     * @return new config request object
     */
    public static ConfigRequest of(String filename) {
        Path path = FabricLoader.getInstance().getConfigDir();
        return new ConfigRequest(path.resolve(filename + ".properties").toFile(), filename);
    }


    private void createConfig() throws IOException {

        // try creating missing files
        request.file.getParentFile().mkdirs();
        Files.createFile(request.file.toPath());

        // write default config data
        PrintWriter writer = new PrintWriter(request.file, StandardCharsets.UTF_8);
        writer.write(request.getConfig());
        writer.close();

    }

    private void loadConfig() throws IOException {
        Scanner reader = new Scanner(request.file);
        for (int line = 1; reader.hasNextLine(); line++) {
            parseConfigEntry(reader.nextLine(), line);
        }
    }

    private void parseConfigEntry(String entry, int line) {
        if (!entry.isEmpty() && !entry.startsWith("#")) {
            String[] parts = entry.split("=", 2);
            if (parts.length == 2) {
                config.put(parts[0], parts[1]);
            } else {
                throw new RuntimeException("Syntax error in config file on line " + line + "!");
            }
        }
    }

    private SimpleConfigFabric(ConfigRequest request) {
        this.request = request;
        String identifier = "Config '" + request.filename + "'";

        if (!request.file.exists()) {
            LOGGER.info("{} is missing, generating default one...", identifier);

            try {
                createConfig();
            } catch (IOException e) {
                LOGGER.error("{} failed to generate!", identifier);
                LOGGER.trace(e.getMessage());
                broken = true;
            }
        }

        if (!broken) {
            try {
                loadConfig();
            } catch (Exception e) {
                LOGGER.error("{} failed to load!", identifier);
                LOGGER.trace(e.getMessage());
                broken = true;
            }
        }

    }

    public boolean getBoolean(String key, boolean def) {
        return getConfig(key, def, str -> {
            if (str.equalsIgnoreCase("true")) {
                return OptionalWithError.of(true);
            }
            if (str.equalsIgnoreCase("false")) {
                return OptionalWithError.of(false);
            }
            return OptionalWithError.empty(str + " cannot be parsed as boolean using the default %s");
        });
    }

    public long getPositiveLong(String key, long def, long max) {
        return getConfig(key, def, val -> {
            try {
                long value = Long.parseLong(val);
                if (value <= 0) {
                    return OptionalWithError.empty(value + " is not positive using the default %s");
                } else if (value > max) {
                    return OptionalWithError.partial(max, value + " is to big using the max: " + max);
                }
                return OptionalWithError.of(value);
            } catch (NumberFormatException e) {
                return OptionalWithError.empty(val + " cannot be parsed as long using the default %s");
            }
        });
    }

    private <T> T getConfig(String key, T def, Function<String, OptionalWithError<T>> getValue) {
        String val = config.get(key);
        if (val != null) {
            return getValue.apply(val).orElseGetAndDo(def, err -> LOGGER.warn("CSR Config: Error reading Config for {}: {}", key, err));
        }
        LOGGER.warn("CSR Config: No Value present for {}, using the default value {}", key, def);
        return def;
    }


    /**
     * If any error occurred during loading or reading from the config
     * a 'broken' flag is set, indicating that the config's state
     * is undefined and should be discarded using `delete()`
     *
     * @return the 'broken' flag of the configuration
     */
    public boolean isBroken() {
        return broken;
    }

    public String configValues() {
        StringBuilder builder = new StringBuilder();
        String seperator = "";
        builder.append("[");
        for (var entry : config.entrySet()) {
            builder.append(seperator);
            seperator = ", ";
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
        }
        builder.append("]");

        return builder.toString();
    }

    @Override
    public String toString() {
        return "SimpleConfigFabric " + request.filename + ": " + config + (broken ? " broken" : "");
    }


    private record OptionalWithError<T>(@Nullable T value, @Nullable String error) {
        public static <T> OptionalWithError<T> of(@NotNull T value) {
            return new OptionalWithError<>(value, null);
        }

        public static <T> OptionalWithError<T> empty(@NotNull String error) {
            return new OptionalWithError<>(null, error);
        }

        public static <T> OptionalWithError<T> partial(@NotNull T value, @NotNull String error) {
            return new OptionalWithError<>(value, error);
        }

        public T orElseGetAndDo(T other, Consumer<String> actionErrorPresent) {
            if (error != null) {
                actionErrorPresent.accept(error.formatted(other));
            }

            return value != null ? value : other;
        }
    }
}
