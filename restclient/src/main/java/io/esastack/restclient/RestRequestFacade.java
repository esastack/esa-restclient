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
import java.lang.reflect.Type;
import java.util.Map;

public interface RestRequestFacade extends ExecutableRestRequest {

    /**
     * Fills the request's entity with given entity.
     *
     * @param entity entity
     * @return ExecutableRestRequest
     */
    ExecutableRestRequest entity(Object entity);

    /**
     * Fills the request's entity with given entity.And you can deliver generic
     * information through generics.
     *
     * @param entity      entity
     * @param generics generics
     * @return ExecutableRestRequest
     */
    ExecutableRestRequest entity(Object entity, Type generics);

    /**
     * Fills the request's entity with given content.
     *
     * @param content content
     * @return ExecutableRestRequest
     */
    ExecutableRestRequest entity(String content);

    /**
     * Fills the request's entity with given data.
     *
     * @param data data
     * @return ExecutableRestRequest
     */
    ExecutableRestRequest entity(byte[] data);

    /**
     * Fills the request's entity with given file.
     *
     * @param file data
     * @return RestFileRequest
     */
    RestFileRequest entity(File file);

    /**
     * Converts to a {@link RestMultipartRequest} which can be used to handle the body
     * by multipart encoding.
     *
     * @return RestMultipartRequest
     */
    RestMultipartRequest multipart();

    @Override
    RestRequestFacade addParams(Map<String, String> params);

    @Override
    RestRequestFacade addParam(String name, String value);

    @Override
    RestRequestFacade addCookie(String name, String value);

    @Override
    RestRequestFacade addCookie(Cookie... cookies);

    @Override
    RestRequestFacade contentType(MediaType contentType);

    @Override
    RestRequestFacade accept(MediaType... acceptTypes);

    @Override
    RestRequestFacade addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    RestRequestFacade addHeader(CharSequence name, CharSequence value);

    @Override
    RestRequestFacade setHeader(CharSequence name, CharSequence value);

    @Override
    RestRequestFacade removeHeader(CharSequence name);

    @Override
    RestRequestFacade enableUriEncode();

    @Override
    RestRequestFacade readTimeout(long readTimeout);

    @Override
    RestRequestFacade disableExpectContinue();

    @Override
    RestRequestFacade maxRedirects(int maxRedirects);

    @Override
    RestRequestFacade maxRetries(int maxRetries);

    @Override
    RestRequestFacade encoder(Encoder encoder);

    @Override
    RestRequestFacade decoder(Decoder decoder);


}
