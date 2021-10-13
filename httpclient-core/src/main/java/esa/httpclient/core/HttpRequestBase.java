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
public interface HttpRequestBase extends HttpRequest,
        RequestMoreConfig,
        RequestBaseConfig {

    @Override
    HttpRequestBase enableUriEncode();

    @Override
    HttpRequestBase disableExpectContinue();

    @Override
    HttpRequestBase maxRedirects(int maxRedirects);

    @Override
    HttpRequestBase maxRetries(int maxRetries);

    @Override
    HttpRequestBase readTimeout(long readTimeout);

    @Override
    HttpRequestBase addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    HttpRequestBase addParams(Map<String, String> params);

    /**
     * Specifies the given {@link Handle} to handle the inbound message.
     * <p>
     * When this method is invoked, the {@link Handler} which has been set
     * before will be reset to {@code null}. You can get more information
     * about difference between {@link Handle} and {@link Handler} from their
     * class description.
     *
     * @param handle handle
     * @return this
     */
    HttpRequestBase handle(Consumer<Handle> handle);

    /**
     * Specifies the given {@link Handler} to handle the inbound message.
     * <p>
     * When this method is invoked, the {@link Handle} which has been set
     * before will be reset to {@code null}. You can get more information
     * about difference between {@link Handle} and {@link Handler} from their
     * class description.
     *
     * @param handler handler
     * @return this
     */
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

