package net.powerscale.config;

import com.google.gson.Gson;

public class ConfigManager {
    public static Config currentConfig = Default.config;

    public static void initialize() {
        var gson = new Gson();
        var json = gson.toJson(currentConfig);
        System.out.println(json);
    }
}
