package io.esastack.restclient;

import esa.commons.Checks;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
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

    public ContentType(MediaType mediaType, Encoder encoder) {
        Checks.checkNotNull(mediaType, "mediaType");
        Checks.checkNotNull(encoder, "encoder");
        this.mediaType = mediaType;
        this.encoder = encoder;
    }

    public MediaType mediaType() {
        return mediaType;
    }

    public Encoder encoder() {
        return encoder;
    }

    public static final ContentType PROTOBUF
            = new ContentType(
                    MediaTypeUtil.of("application", "x-protobuf", StandardCharsets.UTF_8), new ProtoBufCodec());

    public static final ContentType APPLICATION_JSON_UTF8
            = new ContentType(MediaTypeUtil.APPLICATION_JSON_UTF8, new JacksonCodec());

    public static final ContentType TEXT_PLAIN =
            new ContentType(MediaTypeUtil.TEXT_PLAIN, new StringCodec());

    public static final ContentType APPLICATION_OCTET_STREAM =
            new ContentType(MediaTypeUtil.APPLICATION_OCTET_STREAM, new ByteToByteCodec());

    public static final ContentType FILE =
            new ContentType(MediaTypeUtil.APPLICATION_OCTET_STREAM, new FileToFileEncoder());

    public static final ContentType MULTIPART_FORM_DATA =
            new ContentType(MediaTypeUtil.MULTIPART_FORM_DATA, new MultipartToMultipartEncoder());

    public static final ContentType APPLICATION_FORM_URLENCODED =
            new ContentType(MediaTypeUtil.APPLICATION_FORM_URLENCODED, new FormURLEncodedEncoder());

}
