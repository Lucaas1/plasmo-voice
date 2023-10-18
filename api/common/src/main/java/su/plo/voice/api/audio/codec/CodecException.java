package su.plo.voice.api.audio.codec;

/**
 * Exception indicates issues related to codecs.
 */
public class CodecException extends Exception {

    public CodecException() {
        super();
    }

    public CodecException(String message) {
        super(message);
    }

    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodecException(Throwable cause) {
        super(cause);
    }
}
