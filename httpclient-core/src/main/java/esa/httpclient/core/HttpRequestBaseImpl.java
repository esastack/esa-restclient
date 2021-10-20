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

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.httpclient.core.netty.NettyContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class HttpRequestBaseImpl implements HttpRequestBase {

    protected final NettyContext ctx;
    protected final HttpClientBuilder builder;
    private final HttpUri uri;
    private final HttpMethod method;
    private final HttpHeaders headers = new Http1HeadersImpl();

    protected Consumer<Handle> handle;
    protected Handler handler;
    private long readTimeout;
    private boolean useUriEncode;

    protected HttpRequestBaseImpl(HttpClientBuilder builder,
                                  HttpMethod method,
                                  String uri) {
        Checks.checkNotNull(builder, "builder");
        Checks.checkNotEmptyArg(uri, "uri");
        Checks.checkNotNull(method, "method");
        this.builder = builder;
        this.method = method;
        this.uri = new HttpUri(uri);
        this.ctx = new NettyContext();
        this.readTimeout = builder.readTimeout();
        if (builder.retryOptions() != null) {
            this.ctx.maxRetries(builder.retryOptions().maxRetries());
        }
        this.ctx.maxRedirects(builder.maxRedirects());
        this.ctx.useExpectContinue(builder.isUseExpectContinue());
    }

    @Override
    public HttpRequestBase enableUriEncode() {
        this.useUriEncode = true;
        return self();
    }

    @Override
    public HttpRequestBase disableExpectContinue() {
        ctx.useExpectContinue(false);
        return self();
    }

    @Override
    public HttpRequestBase maxRedirects(int maxRedirects) {
        ctx.maxRedirects(maxRedirects);
        return self();
    }

    @Override
    public HttpRequestBase maxRetries(int maxRetries) {
        ctx.maxRetries(maxRetries);
        return self();
    }

    @Override
    public HttpRequestBase readTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        return self();
    }

    @Override
    public HttpRequestBase addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        if (headers == null) {
            return self();
        }

        for (Map.Entry<? extends CharSequence, ? extends CharSequence> row : headers.entrySet()) {
            addHeader(row.getKey(), row.getValue());
        }

        return self();
    }

    @Override
    public HttpRequestBase addHeader(CharSequence name, CharSequence value) {
        if (illegalArgs(name, value)) {
            return self();
        }
        this.headers.add(name, value);
        return self();
    }

    @Override
    public HttpRequestBase setHeader(CharSequence name, CharSequence value) {
        if (illegalArgs(name, value)) {
            return self();
        }
        this.headers.set(name, value);
        return self();
    }

    @Override
    public HttpRequestBase removeHeader(CharSequence name) {
        this.headers.remove(name);
        return self();
    }

    @Override
    public HttpRequestBase addParams(Map<String, String> params) {
        if (params == null) {
            return self();
        }

        for (Map.Entry<String, String> row : params.entrySet()) {
            addParam(row.getKey(), row.getValue());
        }

        return self();
    }

    @Override
    public HttpRequestBase addParam(String name, String value) {
        if (illegalArgs(name, value)) {
            return self();
        }
        this.uri.addParam(name, value);
        return self();
    }

    @Override
    public HttpRequestBase handle(Consumer<Handle> handle) {
        reset();
        this.handle = handle;
        return self();
    }

    @Override
    public HttpRequestBase handler(Handler handler) {
        reset();
        this.handler = handler;
        return self();
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    @Override
    public String scheme() {
        return uri.netURI().getScheme();
    }

    @Override
    public String path() {
        return uri.netURI().getPath();
    }

    @Override
    public HttpUri uri() {
        return uri;
    }

    @Override
    public String getParam(String name) {
        return uri.getParam(name);
    }

    @Override
    public List<String> getParams(String name) {
        List<String> values = uri.params(name);
        return values == null ? null : Collections.unmodifiableList(values);
    }

    @Override
    public Set<String> paramNames() {
        return Collections.unmodifiableSet(uri.paramNames());
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public CharSequence getHeader(CharSequence name) {
        return headers.get(name);
    }

    @Override
    public long readTimeout() {
        return readTimeout;
    }

    @Override
    public boolean uriEncode() {
        return useUriEncode;
    }

    @Override
    public HttpRequestBase copy() {
        final HttpRequestBaseImpl copied = new HttpRequestBaseImpl(builder, method, uri.toString());
        copyTo(this, copied);
        return copied;
    }

    protected static void copyTo(HttpRequestBaseImpl source, HttpRequestBaseImpl dest) {
        for (String name : source.ctx.attrNames()) {
            dest.ctx.setAttr(name, source.ctx.getAttr(name));
        }

        source.uri().params().forEach(dest.uri::addParams);

        dest.ctx.useExpectContinue(source.ctx.isUseExpectContinue());
        dest.ctx.maxRedirects(source.ctx.maxRedirects());
        dest.ctx.maxRetries(source.ctx.maxRetries());

        dest.headers.add(source.headers);
        dest.handle(source.handle);
        dest.handler(source.handler);
        dest.readTimeout(source.readTimeout);
        if (source.useUriEncode) {
            dest.enableUriEncode();
        }
    }

    private HttpRequestBaseImpl self() {
        return this;
    }

    private void reset() {
        this.handler = null;
        this.handle = null;
    }

    private static boolean illegalArgs(Object obj1, Object obj2) {
        return obj1 == null || obj2 == null;
    }

}

