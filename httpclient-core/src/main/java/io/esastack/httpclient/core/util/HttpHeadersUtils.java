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
package io.esastack.httpclient.core.util;

import esa.commons.StringUtils;
import esa.commons.http.HttpHeaders;
import esa.commons.netty.http.EmptyHttpHeaders;
import esa.commons.netty.http.Http1HeadersImpl;
import io.esastack.httpclient.core.HttpRequest;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.AsciiString;
import io.netty.util.internal.SystemPropertyUtil;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import static io.netty.handler.codec.http.HttpUtil.isAsteriskForm;
import static io.netty.handler.codec.http.HttpUtil.isOriginForm;
import static io.netty.util.AsciiString.EMPTY_STRING;

public final class HttpHeadersUtils {

    public static final String TTFB = "$ttfb";

    private static final String VALIDATE_KEY = "io.esastack.httpclient.validateHttpHeaders";

    public static final boolean VALIDATE = SystemPropertyUtil
            .getBoolean(VALIDATE_KEY, true);

    private static final AsciiString EMPTY_REQUEST_PATH = AsciiString.cached("/");

    private HttpHeadersUtils() {
    }

    public static HttpHeaders copyFrom(HttpHeaders headers0) {
        if (headers0 == null || headers0.isEmpty()) {
            return new Http1HeadersImpl();
        }

        if (headers0 instanceof Http1HeadersImpl) {
            return ((Http1HeadersImpl) headers0).copy();
        }

        return new Http1HeadersImpl().add(headers0);
    }

    public static io.netty.handler.codec.http.HttpHeaders toHttpHeaders(HttpHeaders headers) {
        if (headers == null || headers instanceof EmptyHttpHeaders) {
            return io.netty.handler.codec.http.EmptyHttpHeaders.INSTANCE;
        }

        if (headers instanceof Http1HeadersImpl) {
            return (Http1HeadersImpl) headers;
        }

        io.netty.handler.codec.http.HttpHeaders headers0 = new DefaultHttpHeaders(VALIDATE);
        Iterator<Map.Entry<CharSequence, CharSequence>> iterator = headers.iteratorCharSequence();
        Map.Entry<CharSequence, CharSequence> item;
        while (iterator.hasNext()) {
            item = iterator.next();
            headers0.add(item.getKey(), item.getValue());
        }

        return headers0;
    }

    public static Http2Headers toHttp2Headers(HttpRequest request,
                                              Http1HeadersImpl headers,
                                              boolean uriEncodeEnabled) {
        final Http2Headers out = new DefaultHttp2Headers(VALIDATE, headers.size());

        String path = request.uri().relative(uriEncodeEnabled);
        out.path(StringUtils.isEmpty(path) ? EMPTY_REQUEST_PATH : new AsciiString(path));
        out.method(new AsciiString(request.method().name()));

        final URI uri = request.uri().netURI();
        setHttp2Scheme(headers, request.scheme(), out);

        if (!isOriginForm(uri) && !isAsteriskForm(uri)) {
            // Attempt to take from HOST header before taking from the request-line
            String host = headers.getAsString(HttpHeaderNames.HOST);
            setHttp2Authority((host == null || host.isEmpty()) ? uri.getAuthority() : host, out);
        }

        // Add the HTTP headers which have not been consumed above
        HttpConversionUtil.toHttp2Headers(headers, out);
        return out;
    }

    private static void setHttp2Scheme(io.netty.handler.codec.http.HttpHeaders in,
                                       String scheme,
                                       Http2Headers out) {
        if (StringUtils.isNotEmpty(scheme)) {
            out.scheme(AsciiString.cached(scheme));
            return;
        }

        scheme = in.get(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text());
        if (StringUtils.isNotEmpty(scheme)) {
            out.scheme(AsciiString.cached(scheme));
        } else {
            throw new IllegalArgumentException(":scheme must be specified");
        }
    }

    private static void setHttp2Authority(String authority, Http2Headers out) {
        // The authority MUST NOT include the deprecated "userinfo" subcomponent
        if (authority != null) {
            if (authority.isEmpty()) {
                out.authority(EMPTY_STRING);
            } else {
                int start = authority.indexOf('@') + 1;
                int length = authority.length() - start;
                if (length == 0) {
                    throw new IllegalArgumentException("authority: " + authority);
                }
                out.authority(new AsciiString(authority, start, length));
            }
        }
    }

}
