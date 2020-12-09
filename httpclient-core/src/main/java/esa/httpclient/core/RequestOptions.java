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
import esa.commons.StringUtils;
import esa.commons.collection.HashMultiValueMap;
import esa.commons.collection.MultiValueMap;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import esa.httpclient.core.util.HttpHeadersUtils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RequestOptions {

    private final Scheme scheme;
    private final HttpMethod method;
    private final HttpUri uri;
    private final int readTimeout;
    private final Boolean uriEncodeEnabled;
    private final int maxRedirects;
    private final int maxRetries;
    private final Boolean expectContinueEnabled;
    private final HttpHeaders headers;
    private final Consumer<Handle> handle;
    private final Handler handler;
    private final byte[] body;
    private final File file;
    private final boolean multipart;
    private final MultiValueMap<String, String> attributes;
    private final List<MultipartFileItem> files;

    public RequestOptions(HttpMethod method,
                          HttpUri uri,
                          int readTimeout,
                          Boolean uriEncodeEnabled,
                          Boolean expectContinueEnabled,
                          int maxRetries,
                          int maxRedirects,
                          HttpHeaders headers,
                          Consumer<Handle> handle,
                          Handler handler,
                          byte[] body) {
        this(method, uri, readTimeout, uriEncodeEnabled, maxRetries, maxRedirects, headers,
                expectContinueEnabled, handle, handler, body, null, false, null, null);
    }

    public RequestOptions(HttpMethod method,
                          HttpUri uri,
                          int readTimeout,
                          Boolean uriEncodeEnabled,
                          Boolean expectContinueEnabled,
                          int maxRetries,
                          int maxRedirects,
                          HttpHeaders headers,
                          Consumer<Handle> handle,
                          Handler handler,
                          File file) {
        this(method, uri, readTimeout, uriEncodeEnabled, maxRetries, maxRedirects, headers,
                expectContinueEnabled, handle, handler, null, file, false, null, null);
    }

    public RequestOptions(HttpMethod method,
                          HttpUri uri,
                          int readTimeout,
                          Boolean uriEncodeEnabled,
                          Boolean expectContinueEnabled,
                          int maxRetries,
                          int maxRedirects,
                          HttpHeaders headers,
                          Consumer<Handle> handle,
                          Handler handler,
                          boolean multipart,
                          MultiValueMap<String, String> attributes,
                          List<MultipartFileItem> files) {
        this(method, uri, readTimeout, uriEncodeEnabled, maxRetries, maxRedirects, headers,
                expectContinueEnabled, handle, handler, null, null, multipart, attributes, files);
    }

    public RequestOptions(HttpMethod method,
                          HttpUri uri,
                          int readTimeout,
                          Boolean uriEncodeEnabled,
                          int maxRetries,
                          int maxRedirects,
                          HttpHeaders headers,
                          Consumer<Handle> handle,
                          Handler handler) {
        this(method, uri, readTimeout, uriEncodeEnabled, maxRetries, maxRedirects, headers,
                null, handle, handler, null, null, false,
                null, null);
    }

    public RequestOptions(HttpMethod method,
                          HttpUri uri,
                          int readTimeout,
                          Boolean uriEncodeEnabled,
                          int maxRetries,
                          int maxRedirects,
                          HttpHeaders headers,
                          Boolean expectContinueEnabled,
                          Consumer<Handle> handle,
                          Handler handler,
                          byte[] body,
                          File file,
                          boolean multipart,
                          MultiValueMap<String, String> attributes,
                          List<MultipartFileItem> files) {
        Checks.checkNotNull(method, "HttpMethod must not be null");
        Checks.checkNotNull(uri, "HttpUri must not be null");
        Checks.checkNotNull(headers, "HttpHeaders must not be null");
        this.method = method;
        this.uri = new HttpUri(uri.toString(), uri.params());
        this.readTimeout = readTimeout;
        this.uriEncodeEnabled = uriEncodeEnabled;
        this.maxRetries = maxRetries;
        this.maxRedirects = maxRedirects;
        this.headers = HttpHeadersUtils.copyFrom(headers);
        this.expectContinueEnabled = expectContinueEnabled;
        this.handle = handle;
        this.handler = handler;
        this.body = body;
        this.file = file;
        this.multipart = multipart;
        this.attributes = attributes == null ? null : new HashMultiValueMap<>(attributes);
        this.files = files == null ? null : new ArrayList<>(files);
        this.scheme = toScheme(this.uri.netURI());
    }

    public HttpMethod method() {
        return method;
    }

    public HttpUri uri() {
        return uri;
    }

    public Scheme scheme() {
        return scheme;
    }

    public int readTimeout() {
        return readTimeout;
    }

    public Boolean uriEncodeEnabled() {
        return uriEncodeEnabled;
    }

    public int maxRetries() {
        return maxRetries;
    }

    public int maxRedirects() {
        return maxRedirects;
    }

    public HttpHeaders headers() {
        return headers;
    }

    public Boolean expectContinueEnabled() {
        return expectContinueEnabled;
    }

    public byte[] body() {
        return body;
    }

    public File file() {
        return file;
    }

    public boolean multipart() {
        return multipart;
    }

    public MultiValueMap<String, String> attributes() {
        return attributes;
    }

    public List<MultipartFileItem> files() {
        return files;
    }

    public Consumer<Handle> handle() {
        return handle;
    }

    public Handler handler() {
        return handler;
    }

    private static Scheme toScheme(URI uri) {
        final String scheme = uri.getScheme();
        if (StringUtils.isEmpty(scheme)) {
            return Scheme.HTTP;
        }

        if (Scheme.HTTPS.name0().equalsIgnoreCase(scheme)) {
            return Scheme.HTTPS;
        }

        return Scheme.HTTP;
    }
}
