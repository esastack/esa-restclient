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
package io.esastack.restclient;

import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.RequestBaseConfigure;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.Encoder;

import java.lang.reflect.Type;
import java.util.Map;

public interface RestRequestBase extends RestRequest, RequestBaseConfigure {

    /**
     * @return The entity of request.
     */
    Object entity();

    /**
     * @return The type of entity.
     */
    Class<?> type();

    /**
     * @return The genericType of entity.
     */
    Type genericType();

    @Override
    RestRequestBase readTimeout(long readTimeout);

    @Override
    RestRequestBase maxRedirects(int maxRedirects);

    @Override
    RestRequestBase maxRetries(int maxRetries);

    @Override
    RestRequestBase disableExpectContinue();

    @Override
    RestRequestBase enableUriEncode();

    @Override
    RestRequestBase addParams(Map<String, String> params);

    @Override
    RestRequestBase addParam(String name, String value);

    @Override
    RestRequestBase addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestRequestBase addHeader(CharSequence name, CharSequence value);

    @Override
    RestRequestBase setHeader(CharSequence name, CharSequence value);

    @Override
    RestRequestBase removeHeader(CharSequence name);

    @Override
    RestRequestBase addCookie(String name, String value);

    @Override
    RestRequestBase addCookie(Cookie... cookies);

    @Override
    RestRequestBase contentType(MediaType contentType);

    @Override
    RestRequestBase accept(MediaType... acceptTypes);

    /**
     * set a specific encoder for the request. The request will encode with this encoder
     * and will no longer use the encoders in the client
     *
     * @param encoder specific encoder
     * @return RestRequestBase
     */
    RestRequestBase encoder(Encoder encoder);

    /**
     * @return The specific encoder of request
     */
    Encoder encoder();

    /**
     * set a specific decoder for the request. The request will decode with this decoder
     * and will no longer use the decoders in the client
     *
     * @param decoder specific decoder
     * @return RestRequestBase
     */
    RestRequestBase decoder(Decoder decoder);

    /**
     * @return The specific decoder of request
     */
    Decoder decoder();
}
