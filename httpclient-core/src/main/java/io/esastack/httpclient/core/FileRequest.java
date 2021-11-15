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
package io.esastack.httpclient.core;

import java.util.Map;
import java.util.function.Consumer;

public interface FileRequest extends ExecutableRequest {

    @Override
    FileRequest enableUriEncode();

    @Override
    FileRequest disableExpectContinue();

    @Override
    FileRequest maxRedirects(int maxRedirects);

    @Override
    FileRequest maxRetries(int maxRetries);

    @Override
    FileRequest readTimeout(long readTimeout);

    @Override
    FileRequest handle(Consumer<Handle> handle);

    @Override
    FileRequest handler(Handler handler);

    @Override
    FileRequest addHeader(CharSequence name, CharSequence value);

    @Override
    FileRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    FileRequest setHeader(CharSequence name, CharSequence value);

    @Override
    FileRequest removeHeader(CharSequence name);

    @Override
    FileRequest addParam(String name, String value);

    @Override
    FileRequest addParams(Map<String, String> params);

    @Override
    FileRequest copy();

    @Override
    default boolean isFile() {
        return true;
    }
}
