package io.esastack.restclient.codec.impl;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.ResponseContent;
import io.netty.handler.codec.CodecException;

import java.lang.reflect.Type;

final class DecodeContextImpl<V> implements DecodeContext<V> {

    private final MediaType contentType;
    private final HttpHeaders headers;
    private final ResponseContent<V> content;
    private final Class<?> type;
    private final Type genericType;
    private final Decoder<V>[] decoders;
    private int index = 0;

    DecodeContextImpl(MediaType contentType,
                      HttpHeaders headers,
                      ResponseContent<V> content,
                      Class<?> type,
                      Type genericType,
                      Decoder<V>[] decoders) {
        this.contentType = contentType;
        this.headers = headers;
        this.content = content;
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
    public ResponseContent<V> content() {
        return content;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public Type genericType() {
        return genericType;
    }

    @Override
    public Object next() throws Exception {
        if (index < decoders.length) {
            return decoders[index++].decode(this);
        }

        throw new CodecException("There is no suitable decoder for this response,"
                + " Please set correct decoder!"
                + " , headers of response : " + headers
                + " , expected type : " + type
                + " , expected genericType : " + genericType);
    }
}
