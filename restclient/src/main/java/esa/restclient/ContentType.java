package esa.restclient;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import esa.restclient.codec.*;

import java.lang.reflect.Type;

public class ContentType {

    private final MediaType mediaType;
    private final Encoder<?> encoder;
    private final Decoder<?> decoder;

    public static final Encoder<?> NULL_ENCODER = new Encoder<Object>() {
        private static final String CAUSE = "The NULL_ENCODER can,t encode any object";

        @Override
        public Object encode(MediaType mediaType, HttpHeaders headers, Object entity) {
            throw new UnsupportedOperationException(CAUSE);
        }
    };

    public static final Decoder<?> NULL_DECODER = new Decoder<Object>() {
        private static final String CAUSE = "The NULL_DECODER can,t decode any object";

        @Override
        public <U> U decode(MediaType mediaType, HttpHeaders headers, Object data, Type type) {
            throw new UnsupportedOperationException(CAUSE);
        }
    };

    private ContentType(MediaType mediaType, Encoder<?> encoder, Decoder<?> decoder) {
        Checks.checkNotNull(mediaType, "mediaType");
        Checks.checkNotNull(encoder, "encoder");
        Checks.checkNotNull(decoder, "decoder");
        this.mediaType = mediaType;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public MediaType mediaType() {
        return mediaType;
    }

    public Encoder<?> encoder() {
        return encoder;
    }

    public Decoder<?> decoder() {
        return decoder;
    }

    public static ContentType of(MediaType mediaType, ByteEncoder encoder) {
        return new ContentType(mediaType, encoder, NULL_DECODER);
    }

    public static ContentType of(MediaType mediaType, ByteDecoder decoder) {
        return new ContentType(mediaType, NULL_ENCODER, decoder);
    }

    public static ContentType of(MediaType mediaType, ByteEncoder encoder, ByteDecoder decoder) {
        return new ContentType(mediaType, encoder, decoder);
    }

    public static ContentType of(MediaType mediaType, FileEncoder encoder) {
        return new ContentType(mediaType, encoder, NULL_DECODER);
    }

    public static ContentType of(MediaType mediaType, FileEncoder encoder, ByteDecoder decoder) {
        return new ContentType(mediaType, encoder, decoder);
    }

    public static ContentType of(MediaType mediaType, MultipartEncoder encoder) {
        return new ContentType(mediaType, encoder, NULL_DECODER);
    }

    public static ContentType of(MediaType mediaType, MultipartEncoder encoder, ByteDecoder decoder) {
        return new ContentType(mediaType, encoder, decoder);
    }

    public static ContentType of(MediaType mediaType, ByteCodec byteCodec) {
        return new ContentType(mediaType, byteCodec, byteCodec);
    }

    public static final ContentType PROTOBUF
            = of(MediaType.PROTOBUF, new ProtoBufCodec());

    public static final ContentType APPLICATION_JSON_UTF8
            = of(MediaType.APPLICATION_JSON_UTF8, new JacksonCodec());

    public static final ContentType TEXT_PLAIN =
            of(MediaType.TEXT_PLAIN, new StringCodec());

    public static final ContentType APPLICATION_OCTET_STREAM =
            of(MediaType.APPLICATION_OCTET_STREAM, new ByteToByteCodec());

    public static final ContentType FILE =
            of(MediaType.APPLICATION_OCTET_STREAM, new FileToFileEncoder());

    public static final ContentType MULTIPART_FORM_DATA =
            of(MediaType.MULTIPART_FORM_DATA, new MultipartToMultipartEncoder());

}
