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

import java.io.File;
import java.util.Map;

public interface RestFileRequest extends ExecutableRestRequest {

    File file();

    @Override
    RestFileRequest addParams(Map<String, String> params);

    @Override
    RestFileRequest addParam(String name, String value);

    @Override
    RestFileRequest addCookie(String name, String value);

    @Override
    RestFileRequest addCookie(Cookie... cookies);

    @Override
    RestFileRequest contentType(MediaType contentType);

    @Override
    RestFileRequest accept(MediaType... acceptTypes);

    @Override
    RestFileRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestFileRequest addHeader(CharSequence name, CharSequence value);

    @Override
    RestFileRequest setHeader(CharSequence name, CharSequence value);

    @Override
    RestFileRequest removeHeader(CharSequence name);

    @Override
    RestFileRequest enableUriEncode();

    @Override
    RestFileRequest readTimeout(long readTimeout);

    @Override
    RestFileRequest disableExpectContinue();

    @Override
    RestFileRequest maxRedirects(int maxRedirects);

    @Override
    RestFileRequest maxRetries(int maxRetries);

    @Override
    RestFileRequest encoder(Encoder encoder);

    @Override
    RestFileRequest decoder(Decoder decoder);

}
