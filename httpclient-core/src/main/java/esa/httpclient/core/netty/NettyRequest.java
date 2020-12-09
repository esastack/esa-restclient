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
package esa.httpclient.core.netty;

import esa.commons.Checks;
import esa.commons.collection.MultiValueMap;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpMethod;
import esa.httpclient.core.FileRequest;
import esa.httpclient.core.Handle;
import esa.httpclient.core.Handler;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpUri;
import esa.httpclient.core.MultipartFileItem;
import esa.httpclient.core.MultipartRequest;
import esa.httpclient.core.PlainRequest;
import esa.httpclient.core.RequestOptions;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class NettyRequest implements HttpRequest {

    final RequestOptions options;

    NettyRequest(RequestOptions options) {
        Checks.checkNotNull(options, "RequestOptions must not be null");
        this.options = options;
    }

    @Override
    public String scheme() {
        return options.scheme().name0();
    }

    @Override
    public String path() {
        return options.uri().path();
    }

    @Override
    public HttpMethod method() {
        return options.method();
    }

    @Override
    public HttpHeaders headers() {
        return options.headers();
    }

    @Override
    public HttpUri uri() {
        return options.uri();
    }

    @Override
    public NettyRequest addParam(String name, String value) {
        options.uri().addParam(name, value);
        return this;
    }

    @Override
    public List<String> getParams(String name) {
        return Collections.unmodifiableList(options.uri().params(name));
    }

    @Override
    public String getParam(String name) {
        return options.uri().getParam(name);
    }

    @Override
    public Set<String> paramNames() {
        return options.uri().paramNames();
    }

    @Override
    public NettyRequest addHeader(CharSequence name, CharSequence value) {
        options.headers().add(name, value);
        return this;
    }

    @Override
    public CharSequence getHeader(CharSequence name) {
        return options.headers().get(name);
    }

    @Override
    public NettyRequest setHeader(CharSequence name, CharSequence value) {
        options.headers().set(name, value);
        return this;
    }

    @Override
    public NettyRequest removeHeader(CharSequence name) {
        options.headers().remove(name);
        return this;
    }

    @Override
    public RequestOptions config() {
        return options;
    }

    public static HttpRequest from(RequestOptions options) {
        Checks.checkNotNull(options, "RequestOptions must not be null");
        if (options.file() != null) {
            return from(options.method(),
                    options.uri(),
                    options.readTimeout(),
                    options.uriEncodeEnabled(),
                    options.expectContinueEnabled(),
                    options.maxRetries(),
                    options.maxRedirects(),
                    options.headers(),
                    options.handle(),
                    options.handler(),
                    options.file());
        } else if (options.attributes() != null || options.files() != null) {
            return from(options.method(),
                    options.uri(),
                    options.readTimeout(),
                    options.uriEncodeEnabled(),
                    options.expectContinueEnabled(),
                    options.maxRetries(),
                    options.maxRedirects(),
                    options.headers(),
                    options.handle(),
                    options.handler(),
                    options.multipart(),
                    options.attributes(),
                    options.files());
        } else {
            return from(options.method(),
                    options.uri(),
                    options.readTimeout(),
                    options.uriEncodeEnabled(),
                    options.expectContinueEnabled(),
                    options.maxRetries(),
                    options.maxRedirects(),
                    options.headers(),
                    options.handle(),
                    options.handler(),
                    options.body());
        }
    }

    public static PlainRequest from(HttpMethod method,
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
        return new PlainRequestImpl(new RequestOptions(method,
                uri,
                readTimeout,
                uriEncodeEnabled,
                expectContinueEnabled,
                maxRetries,
                maxRedirects,
                headers,
                handle,
                handler,
                body));
    }

    public static FileRequest from(HttpMethod method,
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
        Checks.checkNotNull(file, "File must not be null");
        return new FileRequestImpl(new RequestOptions(method,
                uri,
                readTimeout,
                uriEncodeEnabled,
                expectContinueEnabled,
                maxRetries,
                maxRedirects,
                headers,
                handle,
                handler,
                file));
    }

    public static MultipartRequest from(HttpMethod method,
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
        boolean attributesAbsent = attributes == null || attributes.isEmpty();
        boolean filesAbsent = files == null || files.isEmpty();
        if (attributesAbsent && filesAbsent) {
            throw new IllegalStateException("Attributes and MultipartFiles are both empty");
        }
        return new MultipartRequestImpl(new RequestOptions(method,
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
                files));
    }

    @Override
    public String toString() {
        return method() + ": " + uri().toString();
    }
}
