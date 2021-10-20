package io.esastack.restclient;

import esa.commons.Checks;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.codec.ByteCodec;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.impl.ByteToByteCodec;
import io.esastack.restclient.codec.impl.FileToFileEncoder;
import io.esastack.restclient.codec.impl.FormURLEncodedEncoder;
import io.esastack.restclient.codec.impl.JacksonCodec;
import io.esastack.restclient.codec.impl.MultipartToMultipartEncoder;
import io.esastack.restclient.codec.impl.ProtoBufCodec;
import io.esastack.restclient.codec.impl.StringCodec;

import java.nio.charset.StandardCharsets;

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
            = of(MediaTypeUtil.of("application", "x-protobuf", StandardCharsets.UTF_8), new ProtoBufCodec());

    public static final ContentType APPLICATION_JSON_UTF8
            = of(MediaTypeUtil.APPLICATION_JSON_UTF8, new JacksonCodec());

    public static final ContentType TEXT_PLAIN =
            of(MediaTypeUtil.TEXT_PLAIN, new StringCodec());

    public static final ContentType APPLICATION_OCTET_STREAM =
            of(MediaTypeUtil.APPLICATION_OCTET_STREAM, new ByteToByteCodec());

    public static final ContentType FILE =
            of(MediaTypeUtil.APPLICATION_OCTET_STREAM, new FileToFileEncoder());

    public static final ContentType MULTIPART_FORM_DATA =
            of(MediaTypeUtil.MULTIPART_FORM_DATA, new MultipartToMultipartEncoder());

    public static final ContentType APPLICATION_FORM_URLENCODED =
            of(MediaTypeUtil.APPLICATION_FORM_URLENCODED, new FormURLEncodedEncoder());

}
