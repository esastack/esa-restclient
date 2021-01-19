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

import esa.commons.annotation.Internal;
import esa.httpclient.core.exec.Interceptor;
import esa.httpclient.core.filter.FilterContext;
import esa.httpclient.core.filter.RequestFilter;

import java.net.SocketAddress;
import java.util.EventListener;

@Internal
public interface Listener extends EventListener {

    /**
     * Be informed while starting to pass {@link Interceptor}s.
     *
     * @param request   request
     * @param ctx       ctx
     */
    default void onInterceptorsStart(HttpRequest request, Context ctx) {
    }

    /**
     * Be informed while finishing pass {@link Interceptor}s.
     *
     * @param request request
     * @param ctx     ctx
     */
    default void onInterceptorsEnd(HttpRequest request, Context ctx) {
    }

    /**
     * Be informed while starting to pass {@link RequestFilter}s.
     *
     * @param request request
     * @param ctx     ctx
     */
    default void onFiltersStart(HttpRequest request, FilterContext ctx) {
    }

    /**
     * Be informed while finishing pass {@link RequestFilter}s.
     *
     * @param request request
     * @param ctx     ctx
     */
    default void onFiltersEnd(HttpRequest request, Context ctx) {
    }

    /**
     * Be informed while attempting to acquire connection pool.
     *
     * @param request request
     * @param ctx     ctx
     * @param address address
     */
    default void onConnectionPoolAttempt(HttpRequest request,
                                         Context ctx,
                                         SocketAddress address) {
    }

    /**
     * Be informed while acquiring connection pool successfully.
     *
     * @param request request
     * @param ctx     ctx
     * @param address address
     */
    default void onConnectionPoolAcquired(HttpRequest request,
                                          Context ctx,
                                          SocketAddress address) {
    }

    /**
     * Be informed while acquiring connection pool failed.
     *
     * @param request request
     * @param ctx     ctx
     * @param address address
     * @param cause   cause
     */
    default void onAcquireConnectionPoolFailed(HttpRequest request,
                                               Context ctx,
                                               SocketAddress address,
                                               Throwable cause) {
    }

    /**
     * Be informed while attempting to acquire connection.
     *
     * @param request request
     * @param ctx     ctx
     * @param address remote address
     */
    default void onConnectionAttempt(HttpRequest request, Context ctx, SocketAddress address) {
    }

    /**
     * Be informed while acquiring connection successfully.
     *
     * @param request   request
     * @param ctx       ctx
     * @param address   remote address
     */
    default void onConnectionAcquired(HttpRequest request, Context ctx, SocketAddress address) {
    }

    /**
     * Be informed while acquiring channel failed.
     *
     * @param request   request
     * @param ctx       ctx
     * @param address   address
     * @param cause     cause
     */
    default void onAcquireConnectionFailed(HttpRequest request,
                                           Context ctx,
                                           SocketAddress address,
                                           Throwable cause) {
    }

    /**
     * Be informed while attempting to write to channel.
     *
     * @param request request
     * @param ctx     ctx
     */
    default void onWriteAttempt(HttpRequest request, Context ctx) {
    }

    /**
     * Be informed while writing to network successfully.
     *
     * @param request request
     * @param ctx     ctx
     */
    default void onWriteDone(HttpRequest request, Context ctx) {
    }

    /**
     * Be informed while writing to network failed.
     *
     * @param request       request
     * @param ctx           ctx
     * @param cause         cause
     */
    default void onWriteFailed(HttpRequest request, Context ctx, Throwable cause) {
    }

    /**
     * Be informed while receiving response's status.
     *
     * @param request request
     * @param ctx     ctx
     * @param message message
     */
    default void onMessageReceived(HttpRequest request, Context ctx, HttpMessage message) {
    }

    /**
     * Be informed while completing response.
     *
     * @param request  request
     * @param ctx      ctx
     * @param response response
     */
    default void onCompleted(HttpRequest request, Context ctx, HttpResponse response) {
    }

    /**
     * Be informed on any unexpected {@link Throwable}
     *
     * @param request request
     * @param ctx     ctx
     * @param cause   throwable
     */
    default void onError(HttpRequest request, Context ctx, Throwable cause) {
    }
}
