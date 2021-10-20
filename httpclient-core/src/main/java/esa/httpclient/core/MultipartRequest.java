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

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

public interface MultipartRequest extends ExecutableRequest, MultipartItemsConfigure {

    MultipartRequest multipartEncode(boolean multipartEncode);


    /**
     * add multipart attribute,this method is not thread-safe.
     *
     * @param name  name
     * @param value value
     * @return this
     */
    @Override
    MultipartRequest attr(String name, String value);

    /**
     * add multipart file,this method is not thread-safe.
     *
     * @param name name
     * @param file value
     * @return this
     */
    @Override
    MultipartRequest file(String name, File file);

    /**
     * add multipart file,this method is not thread-safe.
     *
     * @param name        name
     * @param file        file
     * @param contentType contentType
     * @return this
     */
    @Override
    MultipartRequest file(String name, File file, String contentType);

    /**
     * add multipart file,this method is not thread-safe.
     *
     * @param name        name
     * @param file        file
     * @param contentType contentType
     * @param isText      isText
     * @return this
     */
    @Override
    MultipartRequest file(String name, File file, String contentType, boolean isText);

    /**
     * add multipart file,this method is not thread-safe.
     *
     * @param name        name
     * @param filename    filename
     * @param file        file
     * @param contentType contentType
     * @param isText      isText
     * @return this
     */
    @Override
    MultipartRequest file(String name, String filename, File file, String contentType, boolean isText);

    @Override
    MultipartRequest enableUriEncode();

    @Override
    MultipartRequest disableExpectContinue();

    @Override
    MultipartRequest maxRedirects(int maxRedirects);

    @Override
    MultipartRequest maxRetries(int maxRetries);

    @Override
    MultipartRequest readTimeout(long readTimeout);

    @Override
    MultipartRequest handle(Consumer<Handle> handle);

    @Override
    MultipartRequest handler(Handler handler);

    @Override
    MultipartRequest addHeader(CharSequence name, CharSequence value);

    @Override
    MultipartRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    MultipartRequest setHeader(CharSequence name, CharSequence value);

    @Override
    MultipartRequest removeHeader(CharSequence name);

    @Override
    MultipartRequest addParam(String name, String value);

    @Override
    MultipartRequest addParams(Map<String, String> params);

    @Override
    MultipartRequest copy();

    @Override
    default boolean isMultipart() {
        return true;
    }
}
