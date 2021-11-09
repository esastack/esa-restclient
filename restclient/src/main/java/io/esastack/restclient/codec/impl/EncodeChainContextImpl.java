package io.esastack.restclient.codec.impl;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.codec.EncodeChainContext;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestContent;
import io.netty.handler.codec.CodecException;

import java.lang.reflect.Type;

public class EncodeChainContextImpl implements EncodeChainContext {

    private final MediaType contentType;
    private final HttpHeaders headers;
    private final Object entity;
    private final Class<?> type;
    private final Type genericType;
    private final Encoder[] encoders;
    private int index = 0;

    public EncodeChainContextImpl(MediaType contentType,
                                  HttpHeaders headers,
                                  Object entity,
                                  Class<?> type,
                                  Type genericType,
                                  Encoder[] encoders) {
        this.contentType = contentType;
        this.headers = headers;
        this.entity = entity;
        this.type = type;
        this.genericType = genericType;
        this.encoders = encoders;
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
    public Object entity() {
        return entity;
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
    public RequestContent continueToEncode() throws Exception {
        if (index < encoders.length) {
            return encoders[index++].encode(this);
        }

        throw new CodecException("There is no suitable encoder for this request,"
                + " Please set correct encoder!"
                + " , headers of request : " + headers
                + " , entity of request : " + entity
                + " , type of request : " + type
                + " , genericType of request : " + genericType);
    }
}
