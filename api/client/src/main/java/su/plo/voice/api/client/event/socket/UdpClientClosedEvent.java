package su.plo.voice.api.client.event.socket;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the UDP client is closed.
 */
public final class UdpClientClosedEvent implements Event {

    @Getter
    private final UdpClient client;

    @Getter
    private final Reason reason;

    public UdpClientClosedEvent(@NotNull UdpClient client, @NotNull Reason reason) {
        this.client = checkNotNull(client, "client cannot be null");
        this.reason = checkNotNull(reason, "reason cannot be null");
    }

    public enum Reason {
        FAILED_TO_CONNECT,
        DISCONNECT,
        TIMED_OUT,
        RECONNECT,
        CUSTOM
    }
}
