package esa.restclient.codec;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

public class JsonDecoderSelector extends ByteDecoderSelector {

    private final JsonCodec codec;

    public JsonDecoderSelector() {
        this.codec = new JacksonCodec();
    }

    public JsonDecoderSelector(JsonCodec jsonCodec) {
        Checks.checkNotNull(jsonCodec, "codec");
        this.codec = jsonCodec;
    }

    @Override
    public ByteDecoder doSelect(RestRequest request, ContentType[] acceptTypes, Type type,
                                MediaType responseMediaType, HttpHeaders responseHeaders) {
        if (MediaType.APPLICATION_JSON.isCompatibleWith(responseMediaType)) {
            return codec;
        } else {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return LOWER_PRECEDENCE;
    }
}
