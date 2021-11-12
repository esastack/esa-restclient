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

import esa.commons.Checks;
import esa.commons.StringUtils;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.httpclient.core.CompositeRequest;
import io.esastack.httpclient.core.MultipartBody;
import io.esastack.httpclient.core.MultipartBodyImpl;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.exec.RestRequestExecutor;
import io.esastack.restclient.utils.GenericTypeUtil;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;

public class RestCompositeRequest extends AbstractExecutableRestRequest
        implements RestRequestFacade, RestFileRequest, RestMultipartRequest {

    private Object entity;
    private Class<?> type;
    private Type genericType;

    RestCompositeRequest(CompositeRequest request,
                         RestClientOptions clientOptions,
                         RestRequestExecutor requestExecutor) {
        super(request, clientOptions, requestExecutor);
    }

    @Override
    public Object entity() {
        return entity;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public Type genericType() {
        return genericType;
    }

    @Override
    public RestCompositeRequest removeHeader(CharSequence name) {
        super.removeHeader(name);
        return self();
    }

    @Override
    public RestCompositeRequest readTimeout(long readTimeout) {
        super.readTimeout(readTimeout);
        return self();
    }

    @Override
    public RestCompositeRequest maxRedirects(int maxRedirects) {
        super.maxRedirects(maxRedirects);
        return self();
    }

    @Override
    public RestCompositeRequest maxRetries(int maxRetries) {
        super.maxRetries(maxRetries);
        return self();
    }

    @Override
    public RestCompositeRequest disableExpectContinue() {
        super.disableExpectContinue();
        return self();
    }

    @Override
    public RestCompositeRequest enableUriEncode() {
        super.enableUriEncode();
        return self();
    }

    @Override
    public File file() {
        return (File) entity;
    }

    @Override
    public RestMultipartRequest attr(String name, String value) {
        if (illegalArgs(name, value)) {
            return self();
        }
        if (entity instanceof MultipartBody) {
            ((MultipartBody) entity).attr(name, value);
        } else {
            throw new IllegalStateException("Entity is not MultipartBody type,please call multipart() firstly");
        }
        return self();
    }

    @Override
    public RestMultipartRequest file(String name, File file) {
        if (illegalArgs(name, file)) {
            return self();
        }
        if (entity instanceof MultipartBody) {
            ((MultipartBody) entity).file(name, file);
        } else {
            throw new IllegalStateException("Entity is not MultipartBody type,please call multipart() firstly");
        }
        return self();
    }

    @Override
    public RestMultipartRequest file(String name, File file, String contentType) {
        if (illegalArgs(name, file)) {
            return self();
        }
        if (entity instanceof MultipartBody) {
            ((MultipartBody) entity).file(name, file, contentType);
        } else {
            throw new IllegalStateException("Entity is not MultipartBody type,please call multipart() firstly");
        }
        return self();
    }

    @Override
    public RestMultipartRequest file(String name, File file, String contentType, boolean isText) {
        if (illegalArgs(name, file)) {
            return self();
        }
        if (entity instanceof MultipartBody) {
            ((MultipartBody) entity).file(name, file, contentType, isText);
        } else {
            throw new IllegalStateException("Entity is not MultipartBody type,please call multipart() firstly");
        }
        return self();
    }

    @Override
    public RestMultipartRequest file(String name, String filename, File file, String contentType, boolean isText) {
        if (illegalArgs(name, file)) {
            return self();
        }
        if (entity instanceof MultipartBody) {
            ((MultipartBody) entity).file(name, filename, file, contentType, isText);
        } else {
            throw new IllegalStateException("Entity is not MultipartBody type,please call multipart() firstly");
        }
        return self();
    }

    @Override
    public RestCompositeRequest addParams(Map<String, String> params) {
        super.addParams(params);
        return self();
    }

    @Override
    public RestCompositeRequest addParam(String name, String value) {
        super.addParam(name, value);
        return self();
    }

    @Override
    public RestCompositeRequest addCookie(String name, String value) {
        super.addCookie(name, value);
        return self();
    }

    @Override
    public RestCompositeRequest addCookie(Cookie... cookies) {
        super.addCookie(cookies);
        return self();
    }

    @Override
    public RestCompositeRequest contentType(MediaType contentType) {
        super.contentType(contentType);
        return self();
    }

    @Override
    public RestCompositeRequest accept(MediaType... acceptTypes) {
        super.accept(acceptTypes);
        return self();
    }

    @Override
    public RestCompositeRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        super.addHeaders(headers);
        return self();
    }

    @Override
    public RestCompositeRequest addHeader(CharSequence name, CharSequence value) {
        super.addHeader(name, value);
        return self();
    }

    @Override
    public RestCompositeRequest setHeader(CharSequence name, CharSequence value) {
        super.setHeader(name, value);
        return self();
    }

    @Override
    public ExecutableRestRequest entity(Object entity) {
        Checks.checkNotNull(entity, "entity");
        checkEntityHadSet();
        setContentTypeIfAbsent(MediaTypeUtil.APPLICATION_JSON_UTF8);
        fillEntity(entity);
        return self();
    }

    @Override
    public ExecutableRestRequest entity(Object entity, Type genericType) {
        Checks.checkNotNull(entity, "entity");
        Checks.checkNotNull(genericType, "genericType");
        checkEntityHadSet();
        Class<?> typeTem = entity.getClass();
        GenericTypeUtil.checkTypeCompatibility(typeTem, genericType);
        setContentTypeIfAbsent(MediaTypeUtil.APPLICATION_JSON_UTF8);
        fillEntity(entity, typeTem, genericType);
        return self();
    }

    @Override
    public ExecutableRestRequest entity(String content) {
        Checks.checkNotNull(content, "content");
        checkEntityHadSet();
        setContentTypeIfAbsent(MediaTypeUtil.TEXT_PLAIN);
        fillEntity(content);
        return self();
    }

    @Override
    public ExecutableRestRequest entity(byte[] data) {
        Checks.checkNotNull(data, "data");
        checkEntityHadSet();
        setContentTypeIfAbsent(MediaTypeUtil.APPLICATION_OCTET_STREAM);
        fillEntity(data);
        return self();
    }

    @Override
    public RestFileRequest entity(File file) {
        Checks.checkNotNull(file, "file");
        checkEntityHadSet();
        setContentTypeIfAbsent(MediaTypeUtil.APPLICATION_OCTET_STREAM);
        fillEntity(file);
        return self();
    }

    @Override
    public RestMultipartRequest multipart() {
        checkEntityHadSet();
        setContentTypeIfAbsent(MediaTypeUtil.MULTIPART_FORM_DATA);
        fillMultipartBody();
        return self();
    }

    @Override
    public RestCompositeRequest encoder(Encoder encoder) {
        super.encoder(encoder);
        return self();
    }

    @Override
    public RestCompositeRequest decoder(Decoder decoder) {
        super.decoder(decoder);
        return self();
    }

    private void fillMultipartBody() {
        this.entity = new MultipartBodyImpl();
        this.type = MultipartBodyImpl.class;
        this.genericType = MultipartBodyImpl.class;
    }

    private void fillEntity(Object entity) {
        this.entity = entity;
        this.type = entity.getClass();
        this.genericType = this.type;
    }

    private void fillEntity(Object entity, Class<?> type, Type genericType) {
        this.entity = entity;
        this.type = type;
        this.genericType = genericType;
    }

    private void checkEntityHadSet() {
        if (this.entity != null) {
            throw new IllegalStateException("Entity had been set,and it cannot be set repeatedly!");
        }
    }

    private void setContentTypeIfAbsent(MediaType contentType) {
        if (StringUtils.isBlank(headers().get(HttpHeaderNames.CONTENT_TYPE))) {
            contentType(contentType);
        }
    }

    private RestCompositeRequest self() {
        return this;
    }

    private static boolean illegalArgs(Object obj1, Object obj2) {
        return obj1 == null || obj2 == null;
    }
}
