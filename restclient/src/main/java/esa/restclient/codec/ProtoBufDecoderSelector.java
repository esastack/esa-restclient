package esa.restclient.codec;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

public class ProtoBufDecoderSelector extends ByteDecoderSelector {
    private final ProtoBufCodec codec;

    public ProtoBufDecoderSelector() {
        this.codec = new ProtoBufCodec();
    }

    public ProtoBufDecoderSelector(ProtoBufCodec codec) {
        Checks.checkNotNull(codec, "codec");
        this.codec = codec;
    }

    @Override
    public ByteDecoder doSelect(RestRequest request, ContentType[] acceptTypes, Type type,
                                MediaType responseMediaType, HttpHeaders responseHeaders) {
        if (MediaType.PROTOBUF.isCompatibleWith(responseMediaType)) {
            return codec;
        }
        return null;
    }

    @Override
    public int getOrder() {
        return LOWER_PRECEDENCE;
    }
}
