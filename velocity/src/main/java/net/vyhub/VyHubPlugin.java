package net.vyhub;


import com.google.inject.Inject;
import com.velocitypowered.api.command.*;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.vyhub.abstractClasses.AServer;
import net.vyhub.command.ConfigCommand;
import net.vyhub.command.SetupCommand;
import net.vyhub.config.I18n;
import net.vyhub.config.VyHubConfiguration;
import net.vyhub.lib.Utility;
import net.vyhub.tasks.TServer;
import net.vyhub.tasks.TUser;
import okhttp3.OkHttpClient;
import retrofit2.Response;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

@Plugin(id = "vyhub", name = "vyhub", version = "1.5.1",
        url = "https://vyhub.net", description = "VyHub plugin to manage and monetize your Minecraft server. You can create your webstore for free with VyHub!", authors = {"VyHub, Matbyte"})
public class VyHubPlugin {
    public static VyHubPlugin plugin;
    private static ScheduledTask readyCheckTask;
    private VelocityVyHubPlatform platform;
    private VyHubAPI apiClient;
    private OkHttpClient httpClient;
    private I18n i18n;
    public TUser tUser;
    public TServer tServer;
    private final ProxyServer server;
    private final Path dataDirectory;
    private final Logger logger;

    public I18n getI18n() {
        return i18n;
    }
    public VyHubAPI getApiClient() {
        return apiClient;
    }
    public ProxyServer getServer() {
        return server;
    }
    public VelocityVyHubPlatform getPlatform() {
        return platform;
    }
    public Logger getLogger() {
        return logger;
    }

    @Inject
    public VyHubPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        // Initialization happens in onProxyInit
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        plugin = this;
        platform = new VelocityVyHubPlatform(this);

        tUser = new TUser(this.platform);
        tServer = new TServer(this.platform, this.tUser, this.server);

        // Configuration, I18N and API Client
        VyHubConfiguration.setPlatform(platform);
        VyHubConfiguration.loadConfig();
        i18n = new I18n(VyHubConfiguration.getLocale());
        httpClient = Utility.okhttp(new File("plugins/VyHub/", "cache"));

        sendStartupMessage();

        registerCommands();

        readyCheckTask = server.getScheduler().buildTask(plugin, this::checkReady).repeat(1L, TimeUnit.MINUTES).schedule();
    }

    public void onReady() {
        readyCheckTask.cancel();
        server.getScheduler().buildTask(plugin, tServer::patchServer).repeat(1L, TimeUnit.MINUTES).schedule();
    }


    public void registerCommands() {
        CommandManager commandManager = server.getCommandManager();
        // vh_config
        CommandMeta configMeta = commandManager.metaBuilder("vh_config").plugin(this).build();
        SimpleCommand configCommand = new ConfigCommand(plugin);
        commandManager.register(configMeta, configCommand);

        // vh_setup
        CommandMeta setupMeta = commandManager.metaBuilder("vh_setup").plugin(this).build();
        SimpleCommand setupCommand = new SetupCommand(plugin);
        commandManager.register(setupMeta, setupCommand);
    }

    public void checkReady() {
        String apiBaseUrl = VyHubConfiguration.getApiUrl();
        final String apiKey = VyHubConfiguration.getApiKey();

        if (!apiBaseUrl.endsWith("/")) {
            apiBaseUrl = apiBaseUrl + "/";
        }

        if (Objects.equals(apiBaseUrl, "") || Objects.equals(apiKey, "")) {
            this.platform.log(INFO, "Looks like this is a fresh setup. Get started by creating a server and generating the correct command in your VyHub server settings.");
        } else {
            this.platform.log(INFO, "Validating API credentials...");
            apiClient = VyHubAPI.create(apiBaseUrl, apiKey, httpClient);
        }


        Response response = null;
        try {
            response = apiClient.getServer(VyHubConfiguration.getServerId()).execute();
        } catch (Exception e) {
            this.platform.log(WARNING, "Cannot connect to VyHub API! Please follow the installation instructions.");
            return;
        }

        if (response == null || !response.isSuccessful()) {
            this.platform.log(WARNING, "Cannot connect to VyHub API! Please follow the installation instructions.");
            return;
        }

        tServer.getServerInformation();

        if (AServer.serverbundleID == null || AServer.serverbundleID.isEmpty()) {
            this.platform.log(WARNING, "Cannot fetch serverbundle id from VyHub API! Please follow the installation instructions.");
            return;
        }

        this.platform.log(INFO, "Successfully connected to VyHub API.");
        this.onReady();
    }


    public void sendStartupMessage() {
        server.getConsoleCommandSource().sendMessage(Component.text(""));
        server.getConsoleCommandSource().sendMessage(Component.text("  \\  / |__|   ").append(Component.text("VyHub", NamedTextColor.DARK_RED)));
        server.getConsoleCommandSource().sendMessage(Component.text("   \\/  |  |   ").append(Component.text("Running on Velocity", NamedTextColor.DARK_GRAY)));
        server.getConsoleCommandSource().sendMessage(Component.text(""));
    }
}
