package esa.restclient.codec;

import esa.commons.annotation.Internal;
import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

@Internal
public class DefaultCodecSelector implements EncoderSelector, DecoderSelector {

    public DefaultCodecSelector() {
    }

    @Override
    public Encoder<?> select(HttpHeaders requestHeaders, ContentType contentType, Object entity) {
        if (contentType == null) {
            return null;
        }

        return contentType.encoder();
    }

    @Override
    public Decoder<?> select(RestRequest request, ContentType[] acceptTypes, Type type,
                             MediaType responseMediaType, HttpHeaders responseHeaders, Object data) {
        if (acceptTypes == null || acceptTypes.length == 0) {
            return null;
        }

        for (ContentType acceptType : acceptTypes) {
            if (acceptType == null) {
                continue;
            }
            if (acceptType.mediaType().isCompatibleWith(responseMediaType)) {
                return acceptType.decoder();
            }
        }

        return null;
    }

    @Override
    public int getOrder() {
        return HIGHER_PRECEDENCE;
    }
}
