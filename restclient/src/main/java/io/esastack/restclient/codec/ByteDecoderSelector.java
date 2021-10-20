package io.esastack.restclient.codec;

import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.ContentType;
import io.esastack.restclient.ResponseBodyContent;
import io.esastack.restclient.RestRequest;

import java.lang.reflect.Type;

public abstract class ByteDecoderSelector implements DecoderSelector {

    @Override
    public final Decoder select(RestRequest request, ContentType[] acceptTypes, MediaType responseMediaType,
                                HttpHeaders responseHeaders, ResponseBodyContent<?> content, Type type) {
        Object data = content.content();
        if (data == null || data instanceof byte[]) {
            return doSelect(request, acceptTypes, type, responseMediaType, responseHeaders);
        } else {
            return null;
        }
    }

    public abstract ByteDecoder doSelect(RestRequest request, ContentType[] acceptTypes, Type type,
                                         MediaType responseMediaType, HttpHeaders responseHeaders);

}
