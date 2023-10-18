package su.plo.voice.api.client.audio.device;

/**
 * Exception indicates issues related to audio devices.
 */
public class DeviceException extends Exception {

    public DeviceException() {
        super();
    }

    public DeviceException(String message) {
        super(message);
    }

    public DeviceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceException(Throwable cause) {
        super(cause);
    }
}
