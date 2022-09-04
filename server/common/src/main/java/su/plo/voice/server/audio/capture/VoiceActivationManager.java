package su.plo.voice.server.audio.capture;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.audio.capture.ActivationManager;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.player.PlayerManager;
import su.plo.voice.proto.data.capture.Activation;
import su.plo.voice.proto.data.capture.VoiceActivation;
import su.plo.voice.server.config.ServerConfig;

import java.util.*;

public final class VoiceActivationManager implements ActivationManager {

    private final PlayerManager players;
    private final ServerActivation proximityActivation;
    private final Map<UUID, ServerActivation> activationById = Maps.newConcurrentMap();

    public VoiceActivationManager(PlayerManager players, ServerConfig.Voice voiceConfig) {
        this.players = players;
        this.proximityActivation = new VoiceServerActivation(
                VoiceActivation.PROXIMITY_NAME,
                "gui.plasmovoice.proximity",
                "plasmovoice:textures/icons/microphone.png",
                "plasmovoice:textures/icons/speaker.png",
                voiceConfig.getDistances(),
                voiceConfig.getDefaultDistance(),
                true,
                Activation.Order.NORMAL
        );
    }

    @Override
    public @NotNull ServerActivation getProximityActivation() {
        return proximityActivation;
    }

    @Override
    public Optional<ServerActivation> getActivationById(@NotNull UUID id) {
        return Optional.ofNullable(activationById.get(id));
    }

    @Override
    public Optional<ServerActivation> getActivationByName(@NotNull String name) {
        return Optional.ofNullable(activationById.get(VoiceActivation.generateId(name)));
    }

    @Override
    public Collection<ServerActivation> getActivations() {
        return activationById.values();
    }

    @Override
    public @NotNull ServerActivation register(@NotNull String name,
                                              @NotNull String translation,
                                              @NotNull String hudIconLocation,
                                              @NotNull String sourceIconLocation,
                                              List<Integer> distances,
                                              int defaultDistance,
                                              boolean transitive,
                                              Activation.Order order) {
        return activationById.computeIfAbsent(
                VoiceActivation.generateId(name),
                (id) -> new VoiceServerActivation(
                        name,
                        translation,
                        hudIconLocation,
                        sourceIconLocation,
                        distances,
                        defaultDistance,
                        transitive,
                        Activation.Order.NORMAL
                )
        );
    }

    @Override
    public boolean unregister(@NotNull UUID id) {
        return activationById.remove(id) != null;
    }

    @Override
    public boolean unregister(@NotNull String name) {
        return activationById.remove(VoiceActivation.generateId(name)) != null;
    }

    @Override
    public boolean unregister(@NotNull ServerActivation activation) {
        return activationById.remove(activation.getId()) != null;
    }
}
