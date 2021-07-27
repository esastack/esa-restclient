package esa.restclient;

import esa.commons.Checks;
import esa.restclient.codec.*;

public class ContentType {

    private final MediaType mediaType;
    private final Encoder encoder;
    private final Decoder decoder;

    private ContentType(MediaType mediaType, Encoder encoder, Decoder decoder) {
        Checks.checkNotNull(mediaType, "mediaType");
        this.mediaType = mediaType;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public MediaType mediaType() {
        return mediaType;
    }

    public Encoder encoder() {
        return encoder;
    }

    public Decoder decoder() {
        return decoder;
    }

    public static ContentType of(MediaType mediaType, Encoder encoder) {
        return new ContentType(mediaType, encoder, null);
    }

    public static ContentType of(MediaType mediaType, Decoder decoder) {
        return new ContentType(mediaType, null, decoder);
    }

    public static ContentType of(MediaType mediaType, Encoder encoder, Decoder decoder) {
        return new ContentType(mediaType, encoder, decoder);
    }

    public static ContentType of(MediaType mediaType, ByteCodec codec) {
        return new ContentType(mediaType, codec, codec);
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

    public static final ContentType APPLICATION_FORM_URLENCODED =
            of(MediaType.APPLICATION_FORM_URLENCODED, new FormURLEncodedEncoder());

}
