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
package io.esastack.httpclient.core.exec;

import esa.commons.StringUtils;
import esa.commons.collection.MultiValueMap;
import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpMethod;
import esa.commons.logging.Logger;
import esa.commons.netty.core.Buffer;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.httpclient.core.DelegatingRequest;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.HttpUri;
import io.esastack.httpclient.core.MultipartFileItem;
import io.esastack.httpclient.core.exception.RedirectException;
import io.esastack.httpclient.core.util.LoggerUtils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RedirectInterceptor implements Interceptor {

    static final String HAS_REDIRECTED_COUNT = "$redirected.count";
    private static final String SLASH = "/";
    private static final Logger logger = LoggerUtils.logger();

    private static final int SC_MOVED_PERMANENTLY = 301;
    private static final int SC_MOVED_TEMPORARILY = 302;
    private static final int SC_SEE_OTHER = 303;
    private static final int SC_TEMPORARY_REDIRECT = 307;
    private static final int SC_PERMANENT_REDIRECT = 308;

    @Override
    public CompletableFuture<HttpResponse> proceed(HttpRequest request, ExecChain next) {
        // Pass directly when redirect is disabled
        final int maxRedirects = next.ctx().maxRedirects();
        if (request.isSegmented() || maxRedirects < 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("Redirection is disabled, uri: {}, maxRedirects: {}",
                        request.uri().toString(), maxRedirects);
            }
            return next.proceed(request);
        }

        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        doRedirect(response, request, next, maxRedirects);
        return response;
    }

    @Override
    public int getOrder() {
        return -3000;
    }

    protected void doRedirect(CompletableFuture<HttpResponse> response,
                              HttpRequest request,
                              ExecChain next,
                              int maxRedirects) {
        if (response.isDone()) {
            return;
        }

        next.proceed(request).whenComplete((rsp, th) -> {
            try {
                // Update hasRedirectedCount immediately
                final int hasDirectedCount = next.ctx().getAttr(HAS_REDIRECTED_COUNT, -1) + 1;
                next.ctx().removeAttr(HAS_REDIRECTED_COUNT);
                next.ctx().setAttr(HAS_REDIRECTED_COUNT, hasDirectedCount);

                if (th != null) {
                    response.completeExceptionally(th);
                    return;
                }

                // Judge whether the request has been handled successfully.
                final boolean shouldRedirect = shouldRedirect(rsp);
                if (!shouldRedirect) {
                    response.complete(rsp);
                    return;
                }

                if (hasDirectedCount < maxRedirects) {
                    URI uri = detectURI(request, rsp);
                    HttpRequest request0 = newRequest(request, uri, rsp.status());

                    if (logger.isDebugEnabled()) {
                        logger.debug("Begin to redirect from {} to {}, current redirectCount: {}",
                                request, request0, hasDirectedCount + 1);
                    }

                    doRedirect(response, request0, next, maxRedirects);
                } else {
                    response.completeExceptionally(new RedirectException(String
                            .format("Failed to proceed request after maxRedirects: %d", maxRedirects)));
                }
            } catch (Throwable ex) {
                response.completeExceptionally(new RedirectException("Unexpected exception occurred" +
                        " when redirecting", ex));
            }
        });
    }

    protected HttpRequest newRequest(HttpRequest request, URI uri, int status) {
        final HttpMethod method0 = switchToGet(request, status) ? HttpMethod.GET : request.method();
        final HttpUri uri0 = new HttpUri(uri, request.uri().unmodifiableParams());
        boolean cleanBody = cleanBody(status);

        final HttpRequest request0 = new DelegatingRequest(request.copy()) {
            @Override
            public HttpMethod method() {
                return method0;
            }

            @Override
            public String scheme() {
                return uri0.netURI().getScheme();
            }

            @Override
            public String path() {
                return uri0.path();
            }

            @Override
            public HttpUri uri() {
                return uri0;
            }

            @Override
            public boolean isSegmented() {
                return !cleanBody && super.isSegmented();
            }

            @Override
            public boolean isMultipart() {
                return !cleanBody && super.isMultipart();
            }

            @Override
            public Buffer buffer() {
                return cleanBody ? null : super.buffer();
            }

            @Override
            public File file() {
                return cleanBody ? null : super.file();
            }

            @Override
            public MultiValueMap<String, String> attrs() {
                return cleanBody ? null : super.attrs();
            }

            @Override
            public List<MultipartFileItem> files() {
                return cleanBody ? null : super.files();
            }
        };

        standardHeaders(request0.headers(), cleanBody);
        return request0;
    }

    protected boolean shouldRedirect(HttpResponse response) {
        if (!response.headers().contains(HttpHeaderNames.LOCATION)) {
            return false;
        }

        int status = response.status();
        switch (status) {
            case SC_MOVED_PERMANENTLY:
            case SC_MOVED_TEMPORARILY:
            case SC_SEE_OTHER:
            case SC_TEMPORARY_REDIRECT:
            case SC_PERMANENT_REDIRECT:
                return true;
            default:
                return false;
        }
    }

    protected boolean switchToGet(HttpRequest request, int status) {
        return status == SC_PERMANENT_REDIRECT
                || status == SC_TEMPORARY_REDIRECT
                || status == SC_SEE_OTHER;
    }

    protected boolean cleanBody(int status) {
        return status == SC_MOVED_PERMANENTLY || status == SC_MOVED_TEMPORARILY || status == SC_SEE_OTHER;
    }

    protected URI detectURI(HttpRequest request, HttpResponse response)
            throws RedirectException {
        final String location = response.headers().get(HttpHeaderNames.LOCATION);

        if (StringUtils.isEmpty(location)) {
            throw new RedirectException("Redirect location is missing");
        }

        // Handle relative path
        if (location.startsWith(SLASH)) {
            try {
                URI uri0 = request.uri().netURI();
                RelativeInfo info = toRelativeInfo(location);

                return new URI(uri0.getScheme(), uri0.getRawAuthority(), info.path, info.query, info.fragment);
            } catch (URISyntaxException ex) {
                throw new RedirectException("Invalid redirect location: " + location, ex);
            }
        }

        return URI.create(location);
    }

    protected void standardHeaders(HttpHeaders headers, boolean cleanBody) {
        headers.remove(HttpHeaderNames.HOST)
                .remove(HttpHeaderNames.CONTENT_LENGTH)
                .remove(HttpHeaderNames.TRANSFER_ENCODING)
                .remove(HttpHeaderNames.CONTENT_TYPE);
    }

    RelativeInfo toRelativeInfo(String uri) {
        int queryIndex = uri.indexOf("?");
        int fragmentIndex = uri.indexOf("#");
        int length = uri.length();

        final RelativeInfo info = new RelativeInfo();
        if (queryIndex < 0 && fragmentIndex < 0) {
            info.path(uri);
            return info;
        }

        if (queryIndex > 0) {
            if (fragmentIndex < 0) {
                info.path(uri.substring(0, queryIndex));
                info.query(uri.substring(queryIndex + 1, length));
            } else {
                info.path(uri.substring(0, queryIndex));
                info.query(uri.substring(queryIndex + 1, fragmentIndex));
                info.fragment(uri.substring(fragmentIndex + 1, length));
            }
        } else {
            if (fragmentIndex > 0) {
                info.path(uri.substring(0, fragmentIndex));
                info.fragment(uri.substring(fragmentIndex + 1, length));
            } else {
                info.path(uri);
            }
        }

        return info;
    }

    static final class RelativeInfo {

        private String path;
        private String query;
        private String fragment;

        String path() {
            return path;
        }

        void path(String path) {
            this.path = path;
        }

        String query() {
            return query;
        }

        void query(String query) {
            this.query = query;
        }

        String fragment() {
            return fragment;
        }

        void fragment(String fragment) {
            this.fragment = fragment;
        }
    }

}
