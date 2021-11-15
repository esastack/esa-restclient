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
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.httpclient.core.CompositeRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.HttpUri;
import io.esastack.httpclient.core.MultipartBody;
import io.esastack.httpclient.core.util.Futures;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestContent;
import io.esastack.restclient.codec.impl.EncodeChainImpl;
import io.esastack.restclient.exec.RestRequestExecutor;
import io.esastack.restclient.utils.CookiesUtil;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

abstract class AbstractExecutableRestRequest implements ExecutableRestRequest {

    protected final CompositeRequest target;
    protected final RestClientOptions clientOptions;
    protected final RestRequestExecutor requestExecutor;
    private Encoder encoder;
    private Decoder decoder;

    protected AbstractExecutableRestRequest(CompositeRequest request,
                                            RestClientOptions clientOptions,
                                            RestRequestExecutor requestExecutor) {
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(clientOptions, "clientOptions");
        Checks.checkNotNull(requestExecutor, "requestExecutor");
        this.target = request;
        this.clientOptions = clientOptions;
        this.requestExecutor = requestExecutor;
    }

    @Override
    public HttpMethod method() {
        return target.method();
    }

    @Override
    public String scheme() {
        return target.scheme();
    }

    @Override
    public String path() {
        return target.path();
    }

    @Override
    public HttpUri uri() {
        return target.uri();
    }

    @Override
    public String getParam(String name) {
        return target.getParam(name);
    }

    @Override
    public List<String> getParams(String name) {
        return target.getParams(name);
    }

    @Override
    public Set<String> paramNames() {
        return target.paramNames();
    }

    @Override
    public HttpHeaders headers() {
        return target.headers();
    }

    @Override
    public CharSequence getHeader(CharSequence name) {
        return target.getHeader(name);
    }

    @Override
    public ExecutableRestRequest removeHeader(CharSequence name) {
        target.removeHeader(name);
        return self();
    }

    @Override
    public boolean uriEncode() {
        return target.uriEncode();
    }

    @Override
    public long readTimeout() {
        return target.readTimeout();
    }

    @Override
    public CompletionStage<RestResponseBase> execute() {
        return requestExecutor.execute(this);
    }

    CompletionStage<HttpResponse> sendRequest() {
        try {
            if (hasBody()) {
                fillBody(encode());
            }
        } catch (Exception e) {
            return Futures.completed(e);
        }
        return target.execute();
    }

    private boolean hasBody() {
        return entity() != null;
    }

    private RequestContent<?> encode() throws Exception {
        return new EncodeChainImpl(this,
                entity(),
                type(),
                genericType(),
                clientOptions.unmodifiableEncodeAdvices(),
                clientOptions.unmodifiableEncoders()).next();
    }

    private void fillBody(RequestContent<?> requestContent) {
        Object entity = requestContent.value();
        if (entity instanceof byte[]) {
            target.body((byte[]) entity);
        } else if (entity instanceof File) {
            target.body((File) entity);
        } else if (entity instanceof MultipartBody) {
            target.multipart((MultipartBody) entity);
        } else {
            throw new IllegalStateException("Illegal type("
                    + (entity == null ? null : entity.getClass())
                    + ") of requestContent's value!");
        }
    }

    @Override
    public ExecutableRestRequest readTimeout(long readTimeout) {
        target.readTimeout(readTimeout);
        return self();
    }

    @Override
    public ExecutableRestRequest maxRedirects(int maxRedirects) {
        target.maxRedirects(maxRedirects);
        return self();
    }

    @Override
    public ExecutableRestRequest maxRetries(int maxRetries) {
        target.maxRetries(maxRetries);
        return self();
    }

    @Override
    public ExecutableRestRequest disableExpectContinue() {
        target.disableExpectContinue();
        return self();
    }

    @Override
    public ExecutableRestRequest enableUriEncode() {
        target.enableUriEncode();
        return self();
    }

    @Override
    public ExecutableRestRequest addParams(Map<String, String> params) {
        target.addParams(params);
        return self();
    }

    @Override
    public ExecutableRestRequest addParam(String name, String value) {
        target.addParam(name, value);
        return self();
    }

    @Override
    public ExecutableRestRequest addCookie(String name, String value) {
        CookiesUtil.addCookie(name, value, headers(), false);
        return self();
    }

    @Override
    public ExecutableRestRequest addCookie(Cookie... cookies) {
        CookiesUtil.addCookies(headers(), false, cookies);
        return self();
    }

    @Override
    public Cookie cookie(String name) {
        return CookiesUtil.getCookie(name, headers(), false);
    }

    @Override
    public Set<Cookie> cookies() {
        return CookiesUtil.getCookieSet(headers(), false);
    }

    @Override
    public ExecutableRestRequest contentType(MediaType contentType) {
        Checks.checkNotNull(contentType, "contentType");
        headers().set(HttpHeaderNames.CONTENT_TYPE, contentType.value());
        return self();
    }

    @Override
    public MediaType contentType() {
        String contentTypeString = headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (StringUtils.isBlank(contentTypeString)) {
            return null;
        }

        return MediaTypeUtil.valueOf(contentTypeString);
    }

    @Override
    public ExecutableRestRequest accept(MediaType... acceptTypes) {
        Checks.checkNotNull(acceptTypes, "acceptTypes");
        if (acceptTypes.length == 0) {
            headers().remove(HttpHeaderNames.ACCEPT);
            return self();
        }

        StringBuilder acceptBuilder = new StringBuilder();
        for (int i = 0; i < acceptTypes.length; i++) {
            MediaType acceptType = acceptTypes[i];
            if (acceptType == null) {
                throw new NullPointerException("acceptType is null when index is equal to " + i);
            }
            if (acceptBuilder.length() > 0) {
                acceptBuilder.append(",");
            }
            acceptBuilder.append(acceptType.value());
        }

        int length = acceptBuilder.length();
        if (length == 0) {
            headers().remove(HttpHeaderNames.ACCEPT);
        } else if (acceptBuilder.length() > 0) {
            headers().set(HttpHeaderNames.ACCEPT, acceptBuilder.toString());
        }
        return self();
    }

    @Override
    public ExecutableRestRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        target.addHeaders(headers);
        return self();
    }

    @Override
    public ExecutableRestRequest addHeader(CharSequence name, CharSequence value) {
        target.addHeader(name, value);
        return self();
    }

    @Override
    public ExecutableRestRequest setHeader(CharSequence name, CharSequence value) {
        target.setHeader(name, value);
        return self();
    }

    @Override
    public ExecutableRestRequest encoder(Encoder encoder) {
        this.encoder = encoder;
        return self();
    }

    @Override
    public Encoder encoder() {
        return encoder;
    }

    @Override
    public ExecutableRestRequest decoder(Decoder decoder) {
        this.decoder = decoder;
        return self();
    }

    @Override
    public Decoder decoder() {
        return decoder;
    }

    private ExecutableRestRequest self() {
        return this;
    }
}
