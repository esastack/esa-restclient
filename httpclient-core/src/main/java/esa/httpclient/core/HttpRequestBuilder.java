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

import esa.commons.Checks;
import esa.commons.collection.HashMultiValueMap;
import esa.commons.collection.MultiValueMap;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import esa.commons.netty.http.Http1HeadersImpl;
import esa.httpclient.core.netty.NettyRequest;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The utility class which is designed to build a {@link HttpRequest} easily. When the {@link #build()} method
 * is executed, a new {@link RequestOptions} will be generated to avoid the impact of many builds.
 */
public abstract class HttpRequestBuilder<Builder extends HttpRequestBuilder<Builder, Request>,
        Request extends HttpRequest> {

    protected HttpMethod method = HttpMethod.GET;

    /**
     * The readTimeout of current request, if absent, the default {@link HttpClientBuilder#readTimeout()}
     * will be used. Note: ms
     */
    protected int readTimeout = -1;

    /**
     * Whether to auto uri encode for current request.
     */
    protected Boolean uriEncodeEnabled;

    /**
     * value &le; -1: use default;     value = 0: disable;      value &ge; 1: take effect
     */
    protected int maxRedirects = -1;

    /**
     * value &le; -1: use default;     value = 0: disable;      value &ge;: take effect
     */
    protected int maxRetries = -1;

    protected final HttpHeaders headers = new Http1HeadersImpl();

    protected final HttpUri uri;

    /**
     * Whether to support 'Expect: 100-Continue' handshake
     */
    protected Boolean expectContinueEnabled;

    protected Consumer<Handle> handle;
    protected Handler handler;

    public HttpRequestBuilder(String uri) {
        Checks.checkNotEmptyArg(uri, "Request's uri must not be empty");
        this.uri = new HttpUri(uri);
    }

    public Builder uriEncodeEnabled(Boolean uriEncodeEnabled) {
        this.uriEncodeEnabled = uriEncodeEnabled;
        return self();
    }

    public Builder expectContinueEnabled(Boolean expectContinueEnabled) {
        this.expectContinueEnabled = expectContinueEnabled;
        return self();
    }

    public Builder maxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
        return self();
    }

    public Builder maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return self();
    }

    public Builder readTimeout(int readTimeout) {
        this.readTimeout = readTimeout;

        return self();
    }

    public Builder addHeaders(Map<CharSequence, CharSequence> headers) {
        if (headers == null) {
            return self();
        }

        for (Map.Entry<CharSequence, CharSequence> row : headers.entrySet()) {
            addHeader(row.getKey(), row.getValue());
        }

        return self();
    }

    public Builder addHeader(CharSequence name, CharSequence value) {
        if (illegalArgs(name, value)) {
            return self();
        }
        this.headers.add(name, value);
        return self();
    }

    public Builder setHeader(CharSequence name, CharSequence value) {
        if (illegalArgs(name, value)) {
            return self();
        }
        this.headers.set(name, value);
        return self();
    }

    public Builder addParams(Map<String, String> params) {
        if (params == null) {
            return self();
        }

        for (Map.Entry<String, String> row : params.entrySet()) {
            addParam(row.getKey(), row.getValue());
        }

        return self();
    }

    public Builder addParam(String name, String value) {
        if (illegalArgs(name, value)) {
            return self();
        }
        this.uri.addParam(name, value);
        return self();
    }

    public Builder method(HttpMethod method) {
        this.method = method;
        return self();
    }

    public Builder handle(Consumer<Handle> handle) {
        reset();
        this.handle = handle;
        return self();
    }

    public Builder handler(Handler handler) {
        reset();
        this.handler = handler;
        return self();
    }

    private void reset() {
        this.handler = null;
        this.handle = null;
    }

    @SuppressWarnings("unchecked")
    Builder self() {
        return (Builder) this;
    }

    /**
     * Builds a new request using current options.
     *
     * @return target request
     */
    public abstract Request build();

    static boolean illegalArgs(Object obj1, Object obj2) {
        return obj1 == null || obj2 == null;
    }

    public static class BodyForbiddenBuilder extends HttpRequestBuilder<BodyForbiddenBuilder, PlainRequest> {
        BodyForbiddenBuilder(HttpMethod method, String uri) {
            super(uri);
            this.method = method;
        }

        @Override
        public PlainRequest build() {
            return NettyRequest.from(method,
                    uri,
                    readTimeout,
                    uriEncodeEnabled,
                    expectContinueEnabled,
                    maxRetries,
                    maxRedirects,
                    headers,
                    handle,
                    handler,
                    (byte[]) null);
        }
    }

    public static class BodyPermittedBuilder extends HttpRequestBuilder<BodyPermittedBuilder, HttpRequest> {
        private byte[] body;
        private File file;

        BodyPermittedBuilder(HttpMethod method, String uri) {
            super(uri);
            this.method = method;
        }

        public BodyPermittedBuilder body(byte[] body) {
            cleanBody();
            this.body = body;
            return self();
        }

        public BodyPermittedBuilder file(File file) {
            cleanBody();
            this.file = file;
            return self();
        }

        @Override
        public HttpRequest build() {
            if (file != null) {
                return NettyRequest.from(method,
                        uri,
                        readTimeout,
                        uriEncodeEnabled,
                        expectContinueEnabled,
                        maxRetries,
                        maxRedirects,
                        headers,
                        handle,
                        handler,
                        file);
            }
            return NettyRequest.from(method,
                    uri,
                    readTimeout,
                    uriEncodeEnabled,
                    expectContinueEnabled,
                    maxRetries,
                    maxRedirects,
                    headers,
                    handle,
                    handler,
                    body);
        }

        private void cleanBody() {
            this.body = null;
            this.file = null;
        }
    }

    /**
     * The builder of {@link ChunkRequest}.
     */
    public abstract static class ClassicChunk extends HttpRequestBuilder<ClassicChunk, ChunkRequest> {

        protected Boolean aggregate;

        public ClassicChunk aggregate(Boolean aggregate) {
            this.aggregate = aggregate;
            return self();
        }

        public ClassicChunk(String uri) {
            super(uri);
        }
    }

    public static class Multipart extends HttpRequestBuilder<Multipart, MultipartRequest> {

        private static final String DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";
        private static final String DEFAULT_TEXT_CONTENT_TYPE = "text/plain";

        private final MultiValueMap<String, String> attributes = new HashMultiValueMap<>();
        private final List<MultipartFileItem> files = new LinkedList<>();
        private boolean multipart = true;

        Multipart(HttpMethod method, String uri) {
            super(uri);
            this.method = method;
        }

        @Override
        public MultipartRequest build() {
            return NettyRequest.from(method,
                    uri,
                    readTimeout,
                    uriEncodeEnabled,
                    expectContinueEnabled,
                    maxRetries,
                    maxRedirects,
                    headers,
                    handle,
                    handler,
                    multipart,
                    attributes,
                    files);
        }

        public Multipart multipart(boolean multipart) {
            this.multipart = multipart;
            return self();
        }

        public Multipart attribute(String name, String value) {
            if (illegalArgs(name, value)) {
                return self();
            }
            attributes.add(name, value);
            return self();
        }

        public Multipart file(String name, File file) {
            return file(name, file, DEFAULT_BINARY_CONTENT_TYPE);
        }

        public Multipart file(String name, File file, String contentType) {
            return file(name, file, contentType, DEFAULT_TEXT_CONTENT_TYPE.equalsIgnoreCase(contentType));
        }

        public Multipart file(String name, File file, String contentType, boolean isText) {
            if (illegalArgs(name, file)) {
                return self();
            }
            checkMultipartFile();
            return file(name, file.getName(), file, contentType, isText);
        }

        public Multipart file(String name, String filename, File file, String contentType, boolean isText) {
            checkMultipartFile();
            files.add(new MultipartFileItem(name, filename, file, contentType, isText));
            return self();
        }

        private void checkMultipartFile() {
            if (!multipart) {
                throw new IllegalArgumentException("File is not allowed to add, maybe multipart is false?");
            }
        }
    }
}
