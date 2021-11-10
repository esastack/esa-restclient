package io.esastack.restclient.codec.impl;

import esa.commons.Checks;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestRequestBase;
import io.esastack.restclient.codec.EncodeAdvice;
import io.esastack.restclient.codec.EncodeAdviceContext;
import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestContent;
import io.esastack.restclient.utils.GenericTypeUtil;
import io.netty.handler.codec.CodecException;

import java.lang.reflect.Type;

public final class EncodeChainImpl implements EncodeAdviceContext, EncodeContext {

    private final RestRequest request;
    private final EncodeAdvice[] advices;
    private final Encoder[] encoders;
    private int adviceIndex = 0;
    private int encodeIndex = 0;
    private boolean encodeHadStart = false;
    private Object entity;
    private Class<?> type;
    private Type genericType;

    public EncodeChainImpl(RestRequestBase request,
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
        Encoder encoderOfRequest = request.encoder();
        if (encoderOfRequest == null) {
            encoders = encodersOfClient;
        } else {
            encoders = new Encoder[]{encoderOfRequest};
        }
    }

    @Override
    public RestRequest request() {
        return request;
    }

    @Override
    public MediaType contentType() {
        return request.contentType();
    }

    @Override
    public HttpHeaders headers() {
        return request.headers();
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
    public RequestContent<?> next() throws Exception {
        if (encodeHadStart) {
            return encode();
        }

        if (advices == null || adviceIndex >= advices.length) {
            this.encodeHadStart = true;
            return encode();
        }
        return advices[adviceIndex++].aroundEncode(this);
    }

    private RequestContent<?> encode() throws Exception {
        if (encodeIndex < encoders.length) {
            return encoders[encodeIndex++].encode(this);
        }

        throw new CodecException("There is no suitable encoder for this request,"
                + " Please set correct encoder!"
                + " , headers of request : " + headers()
                + " , entity of request : " + entity
                + " , type of request : " + type
                + " , genericType of request : " + genericType);
    }

}
