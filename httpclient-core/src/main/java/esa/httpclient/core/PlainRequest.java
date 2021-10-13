/*
 * Copyright 2020 OPPO ESA Stack Project
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

public interface PlainRequest extends ExecutableRequest {

    @Override
    PlainRequest enableUriEncode();

    @Override
    PlainRequest disableExpectContinue();

    @Override
    PlainRequest maxRedirects(int maxRedirects);

    @Override
    PlainRequest maxRetries(int maxRetries);

    @Override
    PlainRequest readTimeout(long readTimeout);

    @Override
    PlainRequest handle(Consumer<Handle> handle);

    @Override
    PlainRequest handler(Handler handler);

    @Override
    PlainRequest addHeader(CharSequence name, CharSequence value);

    @Override
    PlainRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    PlainRequest setHeader(CharSequence name, CharSequence value);

    @Override
    PlainRequest removeHeader(CharSequence name);

    @Override
    PlainRequest addParam(String name, String value);

    @Override
    PlainRequest addParams(Map<String, String> params);

    @Override
    PlainRequest copy();
}
