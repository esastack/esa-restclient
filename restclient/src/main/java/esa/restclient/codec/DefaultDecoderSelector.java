package esa.restclient.codec;

import esa.commons.annotation.Internal;
import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.ResponseBodyContent;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

@Internal
public class DefaultDecoderSelector implements DecoderSelector {

    public DefaultDecoderSelector() {
    }

    @Override
    public Decoder select(RestRequest request, ContentType[] acceptTypes, MediaType responseMediaType,
                          HttpHeaders responseHeaders, ResponseBodyContent<?> data, Type type) {
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