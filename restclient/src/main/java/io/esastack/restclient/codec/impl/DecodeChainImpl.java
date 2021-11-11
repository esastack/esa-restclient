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
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestRequestBase;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.codec.DecodeAdvice;
import io.esastack.restclient.codec.DecodeAdviceContext;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.ResponseContent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.CodecException;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public final class DecodeChainImpl implements DecodeAdviceContext, DecodeContext {

    private final RestRequest request;
    private final RestResponse response;
    private final List<DecodeAdvice> advices;
    private final int advicesSize;
    private final List<Decoder> decoders;
    private final int decodersSize;
    private final Class<?> type;
    private final Type genericType;
    private int adviceIndex = 0;
    private int decodeIndex = 0;
    private boolean decodeHadStart = false;
    private ResponseContent<?> responseContent;

    public DecodeChainImpl(RestRequestBase request,
                           RestResponse response,
                           RestClientOptions clientOptions,
                           Class<?> type,
                           Type genericType,
                           ByteBuf byteBuf) {
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(response, "response");
        Checks.checkNotNull(clientOptions, "clientOptions");
        Checks.checkNotNull(type, "type");
        Checks.checkNotNull(byteBuf, "byteBuf");
        this.request = request;
        this.response = response;
        this.advices = clientOptions.unmodifiableDecodeAdvices();
        this.advicesSize = this.advices.size();
        this.type = type;
        this.genericType = genericType;
        this.responseContent = ResponseContent.of(ByteBufUtil.getBytes(byteBuf));
        Decoder decoderOfRequest = request.decoder();
        if (decoderOfRequest == null) {
            this.decoders = clientOptions.unmodifiableDecoders();
            this.decodersSize = this.decoders.size();
        } else {
            this.decoders = Collections.singletonList(decoderOfRequest);
            this.decodersSize = 1;
        }
    }

    @Override
    public RestRequest request() {
        return request;
    }

    @Override
    public RestResponse response() {
        return response;
    }

    @Override
    public MediaType contentType() {
        return response.contentType();
    }

    @Override
    public HttpHeaders headers() {
        return response.headers();
    }

    @Override
    public ResponseContent<?> content() {
        return responseContent;
    }

    @Override
    public void content(ResponseContent<?> responseContent) {
        this.responseContent = responseContent;
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
        if (this.decodeHadStart) {
            return decode();
        }

        if (adviceIndex >= advicesSize) {
            this.decodeHadStart = true;
            return decode();
        }

        return advices.get(adviceIndex++).aroundDecode(this);
    }

    private Object decode() throws Exception {
        if (decodeIndex < decodersSize) {
            return decoders.get(decodeIndex++).decode(this);
        }

        throw new CodecException("There is no suitable decoder for this response,"
                + " Please set correct decoder!"
                + " , headers of response : " + headers()
                + " , expected type : " + type
                + " , expected genericType : " + genericType);
    }

}
