package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.ResponseBodyContent;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

public abstract class ByteDecoderSelector implements DecoderSelector {

    @Override
    public final Decoder select(RestRequest request, ContentType[] acceptTypes, MediaType responseMediaType,
                                HttpHeaders responseHeaders, ResponseBodyContent<?> content, Type type) {
        if (content.type() == ResponseBodyContent.TYPE.BYTES) {
            return doSelect(request, acceptTypes, type, responseMediaType, responseHeaders);
        } else {
            return null;
        }
    }

    abstract public ByteDecoder doSelect(RestRequest request, ContentType[] acceptTypes, Type type,
                                         MediaType responseMediaType, HttpHeaders responseHeaders);

}
