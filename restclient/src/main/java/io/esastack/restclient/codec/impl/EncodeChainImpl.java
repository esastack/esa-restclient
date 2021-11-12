/*
 * Copyright 2021 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.Collections;
import java.util.List;

public final class EncodeChainImpl implements EncodeAdviceContext, EncodeContext {

    private final RestRequest request;
    private final List<EncodeAdvice> advices;
    private final int advicesSize;
    private final List<Encoder> encoders;
    private final int encodersSize;
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
                           List<EncodeAdvice> advices,
                           List<Encoder> encodersOfClient) {
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(entity, "entity");
        Checks.checkNotNull(advices, "advices");
        Checks.checkNotNull(encodersOfClient, "encodersOfClient");
        this.request = request;
        this.entity = entity;
        this.type = type;
        this.genericType = geneticType;
        this.advices = advices;
        this.advicesSize = this.advices.size();
        Encoder encoderOfRequest = request.encoder();
        if (encoderOfRequest == null) {
            this.encoders = encodersOfClient;
            this.encodersSize = this.encoders.size();
        } else {
            this.encoders = Collections.singletonList(encoderOfRequest);
            this.encodersSize = 1;
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
        Class<?> typeTem = entity.getClass();
        GenericTypeUtil.checkTypeCompatibility(typeTem, genericType);
        this.entity = entity;
        this.type = typeTem;
        this.genericType = genericType;
    }

    @Override
    public RequestContent<?> next() throws Exception {
        if (encodeHadStart) {
            return encode();
        }

        if (adviceIndex >= advicesSize) {
            this.encodeHadStart = true;
            return encode();
        }
        return advices.get(adviceIndex++).aroundEncode(this);
    }

    private RequestContent<?> encode() throws Exception {
        if (encodeIndex < encodersSize) {
            return encoders.get(encodeIndex++).encode(this);
        }

        throw new CodecException("There is no suitable encoder for this request,"
                + " Please set correct encoder!"
                + " , headers of request : " + headers()
                + " , entity of request : " + entity
                + " , type of request : " + type
                + " , genericType of request : " + genericType);
    }

}
