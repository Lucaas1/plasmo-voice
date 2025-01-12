package su.plo.voice.api.proxy.event.socket;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.proxy.socket.UdpProxyServer;

/**
 * This event is fired once the UDP proxy server is started.
 */
@RequiredArgsConstructor
public final class UdpProxyServerStartedEvent implements Event {

    @Getter
    private final @NonNull UdpProxyServer server;
}
