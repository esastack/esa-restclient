package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.ResponseBodyContent;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

public interface DecoderSelector extends Ordered {
    Decoder select(RestRequest request, ContentType[] acceptTypes, MediaType responseMediaType,
                   HttpHeaders responseHeaders, ResponseBodyContent<?> data, Type type);

    @Override
    default int getOrder() {
        return MIDDLE_PRECEDENCE;
    }
}
