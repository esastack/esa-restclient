package io.esastack.restclient.codec.impl;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.codec.EncodeAdvice;
import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.EncodeResult;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.GenericEntity;
import io.esastack.restclient.codec.RequestBody;
import io.netty.handler.codec.CodecException;

import java.lang.reflect.Type;

public final class EncodeContextImpl implements EncodeContext {

    private final RestRequest request;
    private final EncodeAdvice[] advices;
    private final Encoder encoderOfRequest;
    private final Encoder[] encodersOfClient;
    private int adviceIndex = 0;
    private Object entity;

    public EncodeContextImpl(RestRequest request, Object entity, EncodeAdvice[] advices,
                             Encoder encoderOfRequest, Encoder[] encodersOfClient) {
        Checks.checkNotNull(advices, "advices");
        Checks.checkNotNull(encodersOfClient, "encodersOfClient");
        this.request = request;
        this.entity = entity;
        this.advices = advices;
        this.encoderOfRequest = encoderOfRequest;
        this.encodersOfClient = encodersOfClient;
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
        if (entity == null) {
            return null;
        }

        if (entity instanceof GenericEntity) {
            return ((GenericEntity<?>) entity).getRawType();
        }

        return entity.getClass();
    }

    @Override
    public Type genericType() {
        if (entity == null) {
            return null;
        }

        if (entity instanceof GenericEntity) {
            return ((GenericEntity<?>) entity).getType();
        }

        return entity.getClass();
    }

    @Override
    public void entity(Object entity) {
        this.entity = entity;
    }

    @Override
    public RequestBody<?> proceed() throws Exception {
        if (advices == null || adviceIndex >= advices.length) {

            Class<?> type = null;
            Type genericType = null;
            if (entity != null) {
                if (entity instanceof GenericEntity) {
                    type = ((GenericEntity<?>) entity).getRawType();
                    genericType = ((GenericEntity<?>) entity).getType();
                } else {
                    type = entity.getClass();
                    genericType = type;
                }
            }

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

    private RequestBody<?> encodeByEncoderOfRequest(MediaType contentType, HttpHeaders headers,
                                                    Class<?> type, Type genericType) throws Exception {
        EncodeResult encodeResult = encoderOfRequest.encode(contentType, headers, entity,
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

    private RequestBody<?> encodeByEncodersOfClient(MediaType contentType, HttpHeaders headers,
                                                    Class<?> type, Type genericType) throws Exception {

        for (Encoder encoder : encodersOfClient) {
            EncodeResult encodeResult = encoder.encode(contentType, headers, entity,
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
