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

import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpHeaderValues;
import esa.commons.netty.core.Buffer;
import esa.commons.netty.core.BufferImpl;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

public interface HttpRequestFacade extends ExecutableRequest {

    /**
     * Fills the request's body with given {@code data}.
     *
     * @param data buffer
     * @return request
     */
    PlainRequest body(Buffer data);

    /**
     * Fills the request's body with given {@code data}.
     *
     * @param data data
     * @return request
     */
    default PlainRequest body(byte[] data) {
        if (data == null || data.length == 0) {
            return body(new BufferImpl(Unpooled.EMPTY_BUFFER));
        } else {
            final Buffer buffer = new BufferImpl(data.length);
            buffer.writeBytes(data);
            return body(buffer);
        }
    }

    /**
     * Fills the request's body with given {@code file}'s content.
     * If you haven't set the header of {@link HttpHeaderNames#CONTENT_TYPE},
     * the default {@link HttpHeaderValues#APPLICATION_OCTET_STREAM} will be
     * set automatically.
     *
     * @param file file
     * @return request
     */
    FileRequest body(File file);

    /**
     * Converts to a {@link MultipartRequest} which can be used to handle the body
     * by multipart encoding.
     *
     * @return request
     */
    MultipartRequest multipart();

    /**
     * Converts to a {@link SegmentRequest} which can be used to write the body's content
     * chunk by chunk (not means chunk protocol will be used).
     *
     * @return request
     */
    SegmentRequest segment();

    @Override
    HttpRequestFacade uriEncodeEnabled(Boolean uriEncodeEnabled);

    @Override
    HttpRequestFacade expectContinueEnabled(Boolean expectContinueEnabled);

    @Override
    HttpRequestFacade maxRedirects(int maxRedirects);

    @Override
    HttpRequestFacade maxRetries(int maxRetries);

    @Override
    HttpRequestFacade readTimeout(int readTimeout);

    @Override
    HttpRequestFacade addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    HttpRequestFacade addParams(Map<String, String> params);

    @Override
    HttpRequestFacade handle(Consumer<Handle> handle);

    @Override
    HttpRequestFacade handler(Handler handler);

    @Override
    HttpRequestFacade addHeader(CharSequence name, CharSequence value);

    @Override
    HttpRequestFacade setHeader(CharSequence name, CharSequence value);

    @Override
    HttpRequestFacade removeHeader(CharSequence name);

    @Override
    HttpRequestFacade addParam(String name, String value);
}

