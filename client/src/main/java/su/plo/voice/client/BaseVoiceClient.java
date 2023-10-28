package su.plo.voice.client;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.lib.mod.client.MinecraftUtil;
import su.plo.lib.mod.client.chat.ClientChatUtil;
import su.plo.lib.mod.client.chat.ClientLanguageSupplier;
import su.plo.lib.mod.client.gui.screen.ScreenWrapper;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.style.McTextClickEvent;
import su.plo.slib.api.chat.style.McTextHoverEvent;
import su.plo.slib.api.chat.style.McTextStyle;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.addon.ClientAddonsLoader;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.capture.ClientActivationManager;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.api.client.audio.source.ClientSourceManager;
import su.plo.voice.api.client.config.addon.AddonConfig;
import su.plo.voice.api.client.config.hotkey.Hotkeys;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.connection.UdpClientManager;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.event.socket.UdpClientConnectedEvent;
import su.plo.voice.api.client.render.DistanceVisualizer;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.audio.capture.VoiceAudioCapture;
import su.plo.voice.client.audio.capture.VoiceClientActivationManager;
import su.plo.voice.client.audio.device.VoiceDeviceFactoryManager;
import su.plo.voice.client.audio.device.VoiceDeviceManager;
import su.plo.voice.client.audio.line.VoiceClientSourceLineManager;
import su.plo.voice.client.audio.source.VoiceClientSourceManager;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.config.addon.VoiceAddonConfig;
import su.plo.voice.client.config.hotkey.HotkeyActions;
import su.plo.voice.client.connection.VoiceUdpClientManager;
import su.plo.voice.client.crowdin.PlasmoCrowdinMod;
import su.plo.voice.client.gui.PlayerVolumeAction;
import su.plo.voice.client.gui.settings.VoiceNotAvailableScreen;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.render.cape.DeveloperCapeManager;
import su.plo.voice.client.render.voice.HudIconRenderer;
import su.plo.voice.client.render.voice.OverlayRenderer;
import su.plo.voice.client.render.voice.SourceIconRenderer;
import su.plo.voice.client.render.voice.VoiceDistanceVisualizer;
import su.plo.voice.util.version.ModrinthLoader;
import su.plo.voice.util.version.ModrinthVersion;
import su.plo.voice.util.version.SemanticVersion;

import java.io.File;
import java.util.Map;
import java.util.Optional;

public abstract class BaseVoiceClient extends BaseVoice implements PlasmoVoiceClient {

    protected static final ConfigurationProvider toml = ConfigurationProvider.getProvider(TomlConfiguration.class);

    @Getter
    private final DeviceFactoryManager deviceFactoryManager = new VoiceDeviceFactoryManager();
    @Getter
    private final UdpClientManager udpClientManager = new VoiceUdpClientManager();

    @Setter
    private ServerInfo serverInfo;

    @Getter
    private VoiceDeviceManager deviceManager;
    @Getter
    private AudioCapture audioCapture;
    @Getter
    private ClientActivationManager activationManager;
    @Getter
    private ClientSourceLineManager sourceLineManager;
    @Getter
    private ClientSourceManager sourceManager;
    @Getter
    private DistanceVisualizer distanceVisualizer;

    @Getter
    protected VoiceClientConfig config;
    @Getter
    protected final Map<String, AddonConfig> addonConfigs = Maps.newHashMap();

    protected VoiceSettingsScreen settingsScreen;

    private boolean updatesChecked;

    protected BaseVoiceClient(@NotNull ModrinthLoader loader) {
        super(loader);

        ClientAddonsLoader.INSTANCE.setAddonManager(getAddonManager());
    }

    @EventSubscribe
    public void onUdpConnected(@NotNull UdpClientConnectedEvent event) {
        if (this.updatesChecked) return;
        this.updatesChecked = true;

        backgroundExecutor.execute(() -> {
            try {
                // don't check for updates in dev/alpha builds or if it disabled in the config
                if (!SemanticVersion.parse(getVersion()).isRelease() || !config.getCheckForUpdates().value()) return;

                ModrinthVersion.checkForUpdates(getVersion(), MinecraftUtil.getVersion(), loader)
                        .ifPresent(version -> {
                            ClientChatUtil.sendChatMessage(RenderUtil.getTextConverter().convert(
                                    McTextComponent.translatable(
                                            "message.plasmovoice.update_available",
                                            version.version(),
                                            McTextComponent.translatable("message.plasmovoice.update_available.click")
                                                    .withStyle(McTextStyle.YELLOW)
                                                    .clickEvent(McTextClickEvent.openUrl(version.downloadLink()))
                                                    .hoverEvent(McTextHoverEvent.showText(McTextComponent.translatable(
                                                            "message.plasmovoice.update_available.hover",
                                                            version.downloadLink()
                                                    )))
                                    )
                            ));
                        });
            } catch (Exception e) {
                LOGGER.warn("Failed to check for updates", e);
            }
        });
    }

    // todo: why is this here?
    public void openSettings() {
        Optional<ScreenWrapper> wrappedScreen = ScreenWrapper.getCurrentWrappedScreen();
        if (wrappedScreen.map(screen -> screen.getScreen() instanceof VoiceSettingsScreen)
                .orElse(false)
        ) {
            ScreenWrapper.openScreen(null);
            return;
        }

        if (!udpClientManager.isConnected()) {
            openNotAvailable();
            return;
        }

        if (settingsScreen == null) {
            this.settingsScreen = new VoiceSettingsScreen(
                    this,
                    config
            );
        }
        ScreenWrapper.openScreen(settingsScreen);
    }

    // todo: why is this here?
    public void openNotAvailable() {
        VoiceNotAvailableScreen notAvailableScreen = new VoiceNotAvailableScreen(this);

        Optional<UdpClient> udpClient = udpClientManager.getClient();
        if (udpClient.isPresent()) {
            if (udpClient.get().isClosed()) {
                notAvailableScreen.setCannotConnect();
            } else {
                notAvailableScreen.setConnecting();
            }
        }

        ScreenWrapper.openScreen(notAvailableScreen);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        loadConfig();

        if (!config.getDisableCrowdin().value()) {
            PlasmoCrowdinMod.INSTANCE.downloadTranslations(
                    new File(getConfigFolder(), PlasmoCrowdinMod.INSTANCE.getFolderName())
            );
        }

        this.distanceVisualizer = new VoiceDistanceVisualizer(this, config);

        this.deviceManager = new VoiceDeviceManager(this, config);
        this.sourceLineManager = new VoiceClientSourceLineManager(config);
        this.activationManager = new VoiceClientActivationManager(this, config);
        this.sourceManager = new VoiceClientSourceManager(this, config);
        this.audioCapture = new VoiceAudioCapture(this, config);

        eventBus.register(this, sourceManager);

        // hotkey actions
        new HotkeyActions(this, getHotkeys(), config).register();
        PlayerVolumeAction volumeAction = new PlayerVolumeAction(this, config);
        eventBus.register(this, volumeAction);

        // render
        eventBus.register(this, distanceVisualizer);
        eventBus.register(this, new HudIconRenderer(this, config));
        eventBus.register(this, new SourceIconRenderer(this, config, volumeAction));
        eventBus.register(this, new OverlayRenderer(this, config));

        // addons
        addons.initializeLoadedAddons();
    }

    @Override
    protected void onShutdown() {
        LOGGER.info("Shutting down");

        eventBus.unregister(this);

        super.onShutdown();
    }

    protected void onServerDisconnect() {
        config.save(true);
        udpClientManager.removeClient(UdpClientClosedEvent.Reason.DISCONNECT);
        getServerConnection().ifPresent(ServerConnection::close);

        DeveloperCapeManager.INSTANCE.clearLoadedCapes();
        this.updatesChecked = false;
    }

    @Override
    public Map<Class<?>, Object> createInjectModule() {
        Map<Class<?>, Object> injectModule = Maps.newHashMap();
        injectModule.put(PlasmoVoiceClient.class, BaseVoiceClient.this);
        return injectModule;
    }

    @Override
    public Optional<ServerInfo> getServerInfo() {
        return Optional.ofNullable(serverInfo);
    }

    @Override
    public @NotNull Hotkeys getHotkeys() {
        return config.getKeyBindings();
    }

    @Override
    public synchronized @NotNull AddonConfig getAddonConfig(@NotNull Object addonInstance) {
        AddonContainer addon = addons.getAddon(addonInstance)
                .orElseThrow(() -> new IllegalArgumentException("Addon not found"));

        return addonConfigs.computeIfAbsent(
                addon.getId(),
                (addonId) -> new VoiceAddonConfig(addon, config.getAddons().getAddon(addon.getId()))
        );
    }

    protected ClientLanguageSupplier createLanguageSupplier() {
        return () -> getServerConnection().map(ServerConnection::getLanguage);
    }

    private void loadConfig() {
        File configFile = new File(getConfigFolder(), "client.toml");

        try {
            this.config = toml.load(VoiceClientConfig.class, configFile, false);
            toml.save(VoiceClientConfig.class, config, configFile);
        } catch (Exception e) {
            LOGGER.warn("Failed to load the config", e);

            try {
                this.config = new VoiceClientConfig();
                toml.save(VoiceClientConfig.class, config, configFile);
            } catch (Exception e1) {
                throw new RuntimeException("Failed to save default config", e1);
            }
        } finally {
            config.setConfigFile(configFile);
            config.setAsyncExecutor(backgroundExecutor);

            eventBus.register(this, config.getKeyBindings());
        }

        BaseVoice.DEBUG_LOGGER.enabled(config.getDebug().value() || System.getProperty("plasmovoice.debug") != null);
    }
}
