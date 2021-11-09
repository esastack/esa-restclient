package io.esastack.restclient.codec.impl;

import esa.commons.Checks;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestRequestBase;
import io.esastack.restclient.codec.EncodeAdvice;
import io.esastack.restclient.codec.EncodeAdviceContext;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestContent;
import io.esastack.restclient.utils.GenericTypeUtil;

import java.lang.reflect.Type;

public final class EncodeAdviceContextImpl implements EncodeAdviceContext {

    private final RestRequest request;
    private final EncodeAdvice[] advices;
    private final Encoder encoderOfRequest;
    private final Encoder[] encodersOfClient;
    private int adviceIndex = 0;
    private Object entity;
    private Class<?> type;
    private Type genericType;

    public EncodeAdviceContextImpl(RestRequestBase request,
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
                return new EncodeContextImpl(
                        contentType,
                        headers,
                        entity,
                        type,
                        genericType,
                        new Encoder[]{encoderOfRequest}
                ).continueToEncode();
            } else {
                return new EncodeContextImpl(
                        contentType,
                        headers,
                        entity,
                        type,
                        genericType,
                        encodersOfClient
                ).continueToEncode();
            }
        }
        return advices[adviceIndex++].aroundEncode(this);
    }

}
