package net.vyhub.config;

import com.google.gson.reflect.TypeToken;
import net.vyhub.VyHubPlatform;
import net.vyhub.lib.Cache;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class VyHubConfiguration {
    private static Map<String, String> config = new HashMap<>();
    private static VyHubPlatform platform;

    public static Cache<Map<String, String>> configCache = new Cache<>(
            "config",
            new TypeToken<HashMap<String, String>>() {
            }.getType()
    );

    public static void setPlatform(VyHubPlatform newPlatform) {
        platform = newPlatform;
    }

    public static Map<String, String> loadConfig() {
        Map<String, String> configMap = configCache.load();

        if (configMap == null || configMap.get("api_url") == null || configMap.get("api_key") == null || configMap.get("server_id") == null) {
            platform.log(Level.WARNING, "Config file does not exist or is invalid, creating new one...");

            config.put("api_url", "");
            config.put("api_key", "");
            config.put("server_id", "");
            config.put("is_backend_server", "false");
            config.put("advert_prefix", "[★] ");
            config.put("advert_interval", "180");
            config.put("locale", "en");
            config.put("group_changed_notifications", "true");

            configCache.save(config);
        } else {
            config = configMap;
        }

        return config;
    }

    public static void setConfigValue(String key, String value) {
        config.put(key, value);
        platform.log(Level.INFO, String.format("Set config value %s -> %s.", key, value));
        updateCache();
    }

    public static String getApiUrl() {
        return config.get("api_url");
    }

    public static String getApiKey() {
        return config.get("api_key");
    }

    public static String getServerId() {
        return config.get("server_id");
    }

    public static Locale getLocale() {
        return new Locale(config.get("locale"));
    }

    public static String getAdvertInterval() {
        return config.get("advert_interval");
    }

    public static String getAdvertPrefix() {
        return config.get("advert_prefix");
    }

    public static Boolean getIsBackendServer() { return Boolean.valueOf(config.get("is_backend_server")); }

    public static Boolean getGroupChangedNotifications() { return Boolean.valueOf(config.get("group_changed_notifications")); }

    public static void updateCache() {
        configCache.save(config);
    }
}
