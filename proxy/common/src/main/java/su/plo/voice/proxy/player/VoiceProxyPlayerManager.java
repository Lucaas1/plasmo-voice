package su.plo.voice.proxy.player;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.proxy.McProxyLib;
import su.plo.slib.api.proxy.player.McProxyPlayer;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler;
import su.plo.voice.server.player.BaseVoicePlayerManager;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@RequiredArgsConstructor
public final class VoiceProxyPlayerManager
        extends BaseVoicePlayerManager<VoiceProxyPlayer>
        implements su.plo.voice.api.server.player.VoiceProxyPlayerManager {

    private final PlasmoVoiceProxy voiceProxy;
    private final McProxyLib minecraftProxy;

    @Override
    public Optional<VoiceProxyPlayer> getPlayerById(@NotNull UUID playerId, boolean useServerInstance) {
        VoiceProxyPlayer voicePlayer = playerById.get(playerId);
        if (voicePlayer != null) return Optional.of(voicePlayer);
        else if (!useServerInstance) return Optional.empty();

        return Optional.ofNullable(minecraftProxy.getPlayerById(playerId))
                .map((player) -> playerById.computeIfAbsent(
                        player.getUuid(),
                        (pId) -> {
                            VoiceProxyPlayer newPlayer = new VoiceProxyPlayerConnection(voiceProxy, player);

                            playerByName.put(newPlayer.getInstance().getName(), newPlayer);
                            return newPlayer;
                        }
                ));
    }

    @Override
    public Optional<VoiceProxyPlayer> getPlayerByName(@NotNull String playerName, boolean useServerInstance) {
        VoiceProxyPlayer voicePlayer = playerByName.get(playerName);
        if (voicePlayer != null) return Optional.of(voicePlayer);
        else if (!useServerInstance) return Optional.empty();

        return Optional.ofNullable(minecraftProxy.getPlayerByName(playerName))
                .map((player) -> playerByName.computeIfAbsent(
                        player.getName(),
                        (pId) -> {
                            VoiceProxyPlayer newPlayer = new VoiceProxyPlayerConnection(voiceProxy, player);

                            playerById.put(newPlayer.getInstance().getUuid(), newPlayer);
                            return newPlayer;
                        }
                ));
    }

    @Override
    public @NotNull VoiceProxyPlayer getPlayerByInstance(@NotNull Object instance) {
        McProxyPlayer serverPlayer = minecraftProxy.getPlayerByInstance(instance);

        return playerById.computeIfAbsent(
                serverPlayer.getUuid(),
                (playerId) -> new VoiceProxyPlayerConnection(voiceProxy, serverPlayer)
        );
    }

    @Override
    public void broadcast(@NotNull Packet<ClientPacketTcpHandler> packet, @Nullable Predicate<VoiceProxyPlayer> filter) {
        for (VoiceProxyPlayer player : getPlayers()) {
            if ((filter == null || filter.test(player)) && player.hasVoiceChat())
                player.sendPacket(packet);
        }
    }
}
