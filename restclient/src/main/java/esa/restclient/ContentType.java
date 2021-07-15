package esa.restclient;

import esa.commons.Checks;
import esa.restclient.serializer.*;

import java.lang.reflect.Type;

public class ContentType {

    private final MediaType mediaType;
    private final TxSerializer txSerializer;
    private final RxSerializer rxSerializer;

    public static final TxSerializer NO_SERIALIZE = new TxSerializer() {
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

    public static final RxSerializer NO_DESERIALIZE = new RxSerializer() {
        private static final String CAUSE = "The rxSerializer can,t deserialize any object";

        @Override
        public <T> T deSerialize(byte[] data, Type type) {
            throw new UnsupportedOperationException(CAUSE);
        }

        @Override
        public <T> T deSerialize(HttpInputStream inputStream, Type type) {
            throw new UnsupportedOperationException(CAUSE);
        }
    };

    private ContentType(MediaType mediaType, TxSerializer txSerializer, RxSerializer rxSerializer) {
        Checks.checkNotNull(mediaType, "MediaType must not be null");
        Checks.checkNotNull(txSerializer, "TxSerializer must not be null");
        Checks.checkNotNull(rxSerializer, "RxSerializer must not be null");
        this.mediaType = mediaType;
        this.txSerializer = txSerializer;
        this.rxSerializer = rxSerializer;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public TxSerializer txSerializer() {
        return txSerializer;
    }

    public RxSerializer rxSerializer() {
        return rxSerializer;
    }

    public static ContentType of(MediaType mediaType, TxSerializer txSerializer) {
        return new ContentType(mediaType, txSerializer, NO_DESERIALIZE);
    }

    public static ContentType of(MediaType mediaType, RxSerializer rxSerializer) {
        return new ContentType(mediaType, NO_SERIALIZE, rxSerializer);
    }

    public static ContentType of(MediaType mediaType, Serializer serializer) {
        return new ContentType(mediaType, serializer, serializer);
    }

    public static ContentType of(MediaType mediaType, TxSerializer txSerializer, RxSerializer rxSerializer) {
        return new ContentType(mediaType, txSerializer, rxSerializer);
    }

    /**
     * Media type for {@code application/json;charset=utf-8}.
     */
    public static final ContentType APPLICATION_JSON
            = of(MediaType.APPLICATION_JSON, new JacksonSerializer());

    public static final ContentType TEXT_PLAIN =
            of(MediaType.TEXT_PLAIN, new StringSerializer());

    public static final ContentType APPLICATION_OCTET_STREAM =
            of(MediaType.APPLICATION_OCTET_STREAM, NO_SERIALIZE, NO_DESERIALIZE);

    public static final ContentType MULTIPART_FORM_DATA =
            of(MediaType.MULTIPART_FORM_DATA, NO_SERIALIZE, NO_DESERIALIZE);

}
