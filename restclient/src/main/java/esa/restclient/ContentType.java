package esa.restclient;

import esa.commons.Checks;
import esa.restclient.serializer.HttpOutputStream;
import esa.restclient.serializer.JacksonSerializer;
import esa.restclient.serializer.Serializer;
import esa.restclient.serializer.TxSerializer;
import sun.dc.pr.PRError;

import java.nio.charset.StandardCharsets;

public class ContentType {

    private final MediaType mediaType;
    private final TxSerializer txSerializer;

    public static final TxSerializer NO_NEED_SERIALIZE = new TxSerializer() {
        private static final String CAUSE = "The txSerializer can,t serialize any object";

        @Override
        public byte[] serialize(Object target) {
            throw new UnsupportedOperationException(CAUSE);
        }

        @Override
        public void serialize(Object target, HttpOutputStream outputStream) {
            throw new UnsupportedOperationException(CAUSE);
        }
    };

    public ContentType(MediaType mediaType, TxSerializer txSerializer) {
        Checks.checkNotNull(mediaType, "MediaType must not be null");
        Checks.checkNotNull(txSerializer, "TxSerializer must not be null");
        this.mediaType = mediaType;
        this.txSerializer = txSerializer;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public TxSerializer getTxSerializer() {
        return txSerializer;
    }

    public static ContentType of(MediaType mediaType, TxSerializer txSerializer) {
        return new ContentType(mediaType, txSerializer);
    }

    /**
     * Media type for {@code application/json;charset=utf-8}.
     */
    public static final ContentType APPLICATION_JSON_UTF8_JACKSON
            = of(MediaType.APPLICATION_JSON_UTF8, new JacksonSerializer());
}
