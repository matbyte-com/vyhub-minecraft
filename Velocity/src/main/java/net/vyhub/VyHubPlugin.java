package net.vyhub;


import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

@Plugin(id = "VyHub", name = "VyHub", version = "1.5.1",
        url = "https://vyhub.net", description = "VyHub plugin to manage and monetize your Minecraft server. You can create your webstore for free with VyHub!", authors = {"VyHub, Matbyte"})
public class VelocityTest {

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public VelocityTest(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        logger.info("Hello there! I made my first plugin with Velocity.");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Hello there! I made my first plugin with Velocity.");
    }
}
