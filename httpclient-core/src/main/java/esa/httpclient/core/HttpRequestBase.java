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
package esa.httpclient.core;

import java.util.Map;
import java.util.function.Consumer;

/**
 * The modifiable {@link HttpRequest} which is allowed to be modified.
 */
public interface HttpRequestBase extends HttpRequest {

    HttpRequestBase uriEncodeEnabled(Boolean uriEncodeEnabled);

    HttpRequestBase expectContinueEnabled(Boolean expectContinueEnabled);

    HttpRequestBase maxRedirects(int maxRedirects);

    HttpRequestBase maxRetries(int maxRetries);

    HttpRequestBase readTimeout(int readTimeout);

    HttpRequestBase addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    HttpRequestBase addParams(Map<String, String> params);

    HttpRequestBase handle(Consumer<Handle> handle);

    HttpRequestBase handler(Handler handler);

    @Override
    HttpRequestBase addHeader(CharSequence name, CharSequence value);

    @Override
    HttpRequestBase setHeader(CharSequence name, CharSequence value);

    @Override
    HttpRequestBase removeHeader(CharSequence name);

    @Override
    HttpRequestBase addParam(String name, String value);

    @Override
    HttpRequestBase copy();
}

