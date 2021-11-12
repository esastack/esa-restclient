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
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.Encoder;

import java.util.Map;
import java.util.concurrent.CompletionStage;

public interface ExecutableRestRequest extends RestRequestBase {

    CompletionStage<RestResponseBase> execute();

    @Override
    ExecutableRestRequest readTimeout(long readTimeout);

    @Override
    ExecutableRestRequest maxRedirects(int maxRedirects);

    @Override
    ExecutableRestRequest maxRetries(int maxRetries);

    @Override
    ExecutableRestRequest disableExpectContinue();

    @Override
    ExecutableRestRequest enableUriEncode();

    @Override
    ExecutableRestRequest addParams(Map<String, String> params);

    @Override
    ExecutableRestRequest addParam(String name, String value);

    @Override
    ExecutableRestRequest addCookie(String name, String value);

    @Override
    ExecutableRestRequest addCookie(Cookie... cookies);

    @Override
    ExecutableRestRequest contentType(MediaType contentType);

    @Override
    ExecutableRestRequest accept(MediaType... acceptTypes);

    @Override
    ExecutableRestRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    ExecutableRestRequest addHeader(CharSequence name, CharSequence value);

    @Override
    ExecutableRestRequest setHeader(CharSequence name, CharSequence value);

    @Override
    ExecutableRestRequest removeHeader(CharSequence name);

    @Override
    ExecutableRestRequest encoder(Encoder encoder);

    @Override
    ExecutableRestRequest decoder(Decoder decoder);

}
