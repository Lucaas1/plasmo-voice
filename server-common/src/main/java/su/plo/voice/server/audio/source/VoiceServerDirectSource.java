package su.plo.voice.server.audio.source;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.position.Pos3d;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.PlasmoBaseVoiceServer;
import su.plo.voice.api.server.audio.line.BaseServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerDirectSource;
import su.plo.voice.api.server.connection.UdpConnectionManager;
import su.plo.voice.api.server.event.audio.source.ServerSourceAudioPacketEvent;
import su.plo.voice.api.server.event.audio.source.ServerSourcePacketEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.data.audio.codec.CodecInfo;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket;
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

// todo: it should be reworked because of new broadcast source
public final class VoiceServerDirectSource
        extends BaseServerAudioSource<DirectSourceInfo>
        implements ServerDirectSource
{

    private final PlasmoBaseVoiceServer voiceServer;
    private final UdpConnectionManager<? extends VoicePlayer, ? extends UdpConnection> udpConnections;

    private @Nullable VoicePlayer sender;
    private @Nullable Pos3d relativePosition;
    private @Nullable Pos3d lookAngle;
    private boolean cameraRelative = true;

    @Setter
    @Getter
    private int angle;

    private Supplier<Collection<VoicePlayer>> playersSupplier;

    public VoiceServerDirectSource(
            @NotNull PlasmoBaseVoiceServer voiceServer,
            @NotNull UdpConnectionManager<? extends VoicePlayer, ? extends UdpConnection> udpConnections,
            @NotNull AddonContainer addon,
            @NotNull BaseServerSourceLine line,
            @Nullable CodecInfo decoderInfo,
            boolean stereo
    ) {
        super(addon, UUID.randomUUID(), line, decoderInfo, stereo);

        this.voiceServer = voiceServer;
        this.udpConnections = udpConnections;
    }

    @Override
    public @Nullable VoicePlayer getSender() {
        return sender;
    }

    @Override
    public void setSender(@NotNull VoicePlayer player) {
        this.sender = player;
        updateSourceInfo();
    }

    @Override
    public @Nullable Pos3d getRelativePosition() {
        return relativePosition;
    }

    @Override
    public void setRelativePosition(@NotNull Pos3d position) {
        this.relativePosition = position;
        updateSourceInfo();
    }

    @Override
    public @Nullable Pos3d getLookAngle() {
        return lookAngle;
    }

    @Override
    public void setLookAngle(@NotNull Pos3d position) {
        this.lookAngle = position;
        updateSourceInfo();
    }

    @Override
    public boolean isCameraRelative() {
        return cameraRelative;
    }

    @Override
    public void setCameraRelative(boolean cameraRelative) {
        this.cameraRelative = cameraRelative;
        updateSourceInfo();
    }

//    @Override
//    public void setPlayers(@Nullable Supplier<Collection<VoicePlayer>> playersSupplier) {
//        this.playersSupplier = playersSupplier;
//    }

    @Override
    public @NotNull DirectSourceInfo getSourceInfo() {
        return new DirectSourceInfo(
                addon.getId(),
                id,
                line.getId(),
                name,
                (byte) state.get(),
                decoderInfo,
                stereo,
                iconVisible,
                angle,
                sender == null ? null : sender.getInstance().getGameProfile(),
                relativePosition,
                lookAngle,
                cameraRelative
        );
    }

    @Override
    public boolean sendAudioPacket(@NotNull SourceAudioPacket packet, @Nullable UUID activationId) {
        ServerSourceAudioPacketEvent event = new ServerSourceAudioPacketEvent(this, packet, activationId);
        if (!voiceServer.getEventBus().fire(event)) return false;

        packet.setSourceState((byte) state.get());

        if (dirty.compareAndSet(true, false))
            sendPacket(new SourceInfoPacket(getSourceInfo()));

        if (playersSupplier != null) {
            for (VoicePlayer player : playersSupplier.get()) {
                if (super.notMatchFilters(player)) continue;
                udpConnections.getConnectionByPlayerId(player.getInstance().getUuid())
                        .ifPresent(connection -> connection.sendPacket(packet));
            }
        } else {
            for (UdpConnection connection : udpConnections.getConnections()) {
                if (super.notMatchFilters(connection.getPlayer())) continue;
                connection.sendPacket(packet);
            }
        }

        return true;
    }

    @Override
    public boolean sendPacket(Packet<?> packet) {
        ServerSourcePacketEvent event = new ServerSourcePacketEvent(this, packet);
        if (!voiceServer.getEventBus().fire(event)) return false;

        if (playersSupplier != null) {
            for (VoicePlayer player : playersSupplier.get()) {
                if (super.notMatchFilters(player)) continue;
                player.sendPacket(packet);
            }
        } else {
            for (UdpConnection connection : udpConnections.getConnections()) {
                if (super.notMatchFilters(connection.getPlayer())) continue;
                connection.getPlayer().sendPacket(packet);
            }
        }

        return true;
    }

    @Override
    public boolean matchFilters(@NotNull VoicePlayer player) {
        if (playersSupplier != null) {
            if (!playersSupplier.get().contains(player)) return false;
        }

        return super.matchFilters(player);
    }

    private void updateSourceInfo() {
        sendPacket(new SourceInfoPacket(getSourceInfo()));
    }

    @NotNull
    @Override
    public VoicePlayer getPlayer() {
        return null;
    }
}
