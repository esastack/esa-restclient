package io.esastack.restclient.codec.impl;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.ResponseContent;
import io.netty.handler.codec.CodecException;

import java.lang.reflect.Type;

final class DecodeContextImpl<T> implements DecodeContext<T> {

    private final MediaType contentType;
    private final HttpHeaders headers;
    private final ResponseContent responseContent;
    private final Class<T> type;
    private final Type genericType;
    private final Decoder[] decoders;
    private int index = 0;

    DecodeContextImpl(MediaType contentType,
                      HttpHeaders headers,
                      ResponseContent responseContent,
                      Class<T> type,
                      Type genericType,
                      Decoder[] decoders) {
        this.contentType = contentType;
        this.headers = headers;
        this.responseContent = responseContent;
        this.type = type;
        this.genericType = genericType;
        this.decoders = decoders;
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public ResponseContent responseContent() {
        return responseContent;
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public Type genericType() {
        return genericType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T continueToDecode() throws Exception {
        if (index < decoders.length) {
            return (T) decoders[index++].decode(this);
        }

        throw new CodecException("There is no suitable decoder for this response,"
                + " Please set correct decoder!"
                + " , headers of response : " + headers
                + " , expected type : " + type
                + " , expected genericType : " + genericType);
    }
}
