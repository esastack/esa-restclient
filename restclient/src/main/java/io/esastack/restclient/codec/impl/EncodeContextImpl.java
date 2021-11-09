package io.esastack.restclient.codec.impl;

import esa.commons.Checks;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestRequestBase;
import io.esastack.restclient.codec.CodecResult;
import io.esastack.restclient.codec.EncodeAdvice;
import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestContent;
import io.esastack.restclient.utils.GenericTypeUtil;
import io.netty.handler.codec.CodecException;

import java.lang.reflect.Type;

public final class EncodeContextImpl implements EncodeContext {

    private final RestRequest request;
    private final EncodeAdvice[] advices;
    private final Encoder encoderOfRequest;
    private final Encoder[] encodersOfClient;
    private int adviceIndex = 0;
    private Object entity;
    private Class<?> type;
    private Type genericType;

    public EncodeContextImpl(RestRequestBase request,
                             Object entity,
                             Class<?> type,
                             Type geneticType,
                             EncodeAdvice[] advices,
                             Encoder[] encodersOfClient) {
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(entity, "entity");
        Checks.checkNotNull(advices, "advices");
        Checks.checkNotNull(encodersOfClient, "encodersOfClient");
        this.request = request;
        this.entity = entity;
        this.type = type;
        this.genericType = geneticType;
        this.advices = advices;
        this.encodersOfClient = encodersOfClient;
        this.encoderOfRequest = request.encoder();
    }

    @Override
    public RestRequest request() {
        return request;
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
    public void entity(Object entity) {
        Checks.checkNotNull(entity, "entity");
        this.entity = entity;
        this.type = entity.getClass();
        this.genericType = type;
    }

    @Override
    public void entity(Object entity, Type genericType) {
        Checks.checkNotNull(entity, "entity");
        Checks.checkNotNull(genericType, "genericType");
        this.entity = entity;
        this.type = entity.getClass();
        GenericTypeUtil.checkTypeCompatibility(type, genericType);
        this.genericType = type;
    }

    @Override
    public RequestContent proceed() throws Exception {
        if (advices == null || adviceIndex >= advices.length) {
            MediaType contentType = request.contentType();
            HttpHeaders headers = request.headers();

            if (encoderOfRequest != null) {
                return encodeByEncoderOfRequest(contentType, headers, type, genericType);
            } else {
                return encodeByEncodersOfClient(contentType, headers, type, genericType);
            }
        }
        return advices[adviceIndex++].aroundEncode(this);
    }

    private RequestContent encodeByEncoderOfRequest(MediaType contentType, HttpHeaders headers,
                                                    Class<?> type, Type genericType) throws Exception {
        CodecResult<RequestContent> encodeResult = encoderOfRequest.encode(contentType, headers, entity,
                type, genericType);

        if (encodeResult == null) {
            throw new CodecException("EncodeResult should never be null!"
                    + " Please set correct encoder to the request!"
                    + " encoder of request : " + encoderOfRequest
                    + " , headers of request : " + headers
                    + " , entity of request : " + entity);
        }

        if (encodeResult.isSuccess()) {
            return encodeResult.getResult();
        }

        throw new CodecException("Encode is not success by encoderOfRequest,"
                + " Please set correct encoder to the request!"
                + " encoder of request : " + encoderOfRequest
                + " , headers of request : " + headers
                + " , entity of request : " + entity);
    }

    private RequestContent encodeByEncodersOfClient(MediaType contentType, HttpHeaders headers,
                                                    Class<?> type, Type genericType) throws Exception {

        for (Encoder encoder : encodersOfClient) {
            CodecResult<RequestContent> encodeResult = encoder.encode(contentType, headers, entity,
                    type, genericType);

            if (encodeResult.isSuccess()) {
                return encodeResult.getResult();
            }
        }
        throw new CodecException("There is no suitable encoder for this request,"
                + " Please add correct encoder to the client!"
                + " , headers of request : " + headers
                + " , entity of request : " + entity);
    }

}
