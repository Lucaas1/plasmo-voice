package su.plo.voice.proxy.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.URI;

public class AddressUtil {

    public static InetSocketAddress parseAddress(@NotNull String ip) {
        Preconditions.checkNotNull(ip, "ip");
        URI uri = URI.create("udp://" + ip);
        if (uri.getHost() == null) {
            throw new IllegalStateException("Invalid hostname/IP " + ip);
        }

        int port = uri.getPort() == -1 ? 60606 : uri.getPort();
        try {
            return new InetSocketAddress(uri.getHost(), port);
        } catch (IllegalArgumentException e) {
            return InetSocketAddress.createUnresolved(uri.getHost(), port);
        }
    }

    public static InetSocketAddress resolveAddress(@NotNull InetSocketAddress address) {
        URI uri = URI.create("udp://" + address.getHostString());
        if (uri.getHost() == null) {
            throw new IllegalStateException("Invalid hostname/IP " + address.getHostString());
        }

        address = new InetSocketAddress(uri.getHost(), address.getPort());

        return address;
    }

    private AddressUtil() {
    }
}
