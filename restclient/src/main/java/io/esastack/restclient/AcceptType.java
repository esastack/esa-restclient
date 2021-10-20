package io.esastack.restclient;

import esa.commons.Checks;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.impl.ByteToByteCodec;
import io.esastack.restclient.codec.impl.DefaultDecoder;
import io.esastack.restclient.codec.impl.JacksonCodec;
import io.esastack.restclient.codec.impl.ProtoBufCodec;
import io.esastack.restclient.codec.impl.StringCodec;

import java.nio.charset.StandardCharsets;

public class AcceptType {

    public static final MediaType EMPTY_MEDIA_TYPE = null;
    private final MediaType mediaType;
    private final Decoder decoder;

    public AcceptType(MediaType mediaType, Decoder decoder) {
        Checks.checkNotNull(decoder, "decoder");
        this.mediaType = mediaType;
        this.decoder = decoder;
    }

    public MediaType mediaType() {
        return mediaType;
    }

    public Decoder decoder() {
        return decoder;
    }

    public static final AcceptType DEFAULT
            = new AcceptType(AcceptType.EMPTY_MEDIA_TYPE, new DefaultDecoder());

    public static final AcceptType PROTOBUF
            = new AcceptType(MediaTypeUtil.of("application", "x-protobuf", StandardCharsets.UTF_8), new ProtoBufCodec());

    public static final AcceptType APPLICATION_JSON_UTF8
            = new AcceptType(MediaTypeUtil.APPLICATION_JSON_UTF8, new JacksonCodec());

    public static final AcceptType TEXT_PLAIN =
            new AcceptType(MediaTypeUtil.TEXT_PLAIN, new StringCodec());

    public static final AcceptType APPLICATION_OCTET_STREAM =
            new AcceptType(MediaTypeUtil.APPLICATION_OCTET_STREAM, new ByteToByteCodec());
}
