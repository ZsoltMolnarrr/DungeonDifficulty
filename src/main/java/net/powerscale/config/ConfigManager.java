package net.powerscale.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.powerscale.PowerScale;
import org.slf4j.Logger;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    static final Logger LOGGER = LogUtils.getLogger();
    public static Config currentConfig = Default.config;

    public static void initialize() {
        reload();
    }

    public static void reload() {
        var config = Default.config;
        var configFileName = PowerScale.MODID + ".json";
        var sanitizeConfig = true;
        Path configDir = FabricLoader.getInstance().getConfigDir();

        try {
            var gson = new Gson();
            var filePath = configDir.resolve(configFileName);
            var configFileExists = Files.exists(filePath);
            if (configFileExists) {
                // Read
                Reader reader = Files.newBufferedReader(filePath);
                config = gson.fromJson(reader, Config.class);
                reader.close();
                if (config.dimensions.length == 0) {
                    LOGGER.error("PowerScale config loaded empty config! The JSON is most likely malformed.");
                } else {
                    LOGGER.info("PowerScale config loaded: " + gson.toJson(config));
                }
            }

            if (sanitizeConfig || !configFileExists) {
                var prettyGson = new GsonBuilder().setPrettyPrinting().create();
                Writer writer = Files.newBufferedWriter(filePath);
                writer.write(prettyGson.toJson(config));
                writer.close();
                LOGGER.info("PowerScale config written: " + gson.toJson(config));
            }
        } catch(Exception e) {
            LOGGER.error("Failed loading PowerScale config: " + e.getMessage());
        }

        currentConfig = config;
    }
}
