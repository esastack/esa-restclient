package esa.restclient.codec.impl;

import esa.commons.annotation.Internal;
import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.ResponseBodyContent;
import esa.restclient.RestRequest;
import esa.restclient.codec.Decoder;
import esa.restclient.codec.DecoderSelector;
import io.esastack.commons.net.http.MediaType;

import java.lang.reflect.Type;

@Internal
public class DefaultDecoderSelector implements DecoderSelector {

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
