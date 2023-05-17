package net.vyhub.lib;

import net.vyhub.VyHub;

public class Config {
    public void setConfigValue(String key, String value) {
        VyHub.config.put(key, value);
        logger.info(String.format("Set config value %s -> %s.", key, value));
        VyHub.configCache.save(VyHub.config);
    }

}
