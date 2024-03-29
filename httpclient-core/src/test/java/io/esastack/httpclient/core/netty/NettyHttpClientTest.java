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
package io.esastack.httpclient.core.netty;

import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.HttpClientBuilder;
import io.esastack.httpclient.core.HttpRequest;
import io.esastack.httpclient.core.HttpResponse;
import io.esastack.httpclient.core.config.CacheOptions;
import io.esastack.httpclient.core.config.CallbackThreadPoolOptions;
import io.esastack.httpclient.core.config.ChannelPoolOptions;
import io.esastack.httpclient.core.config.Decompression;
import io.esastack.httpclient.core.config.SslOptions;
import io.esastack.httpclient.core.exec.ExecContext;
import io.esastack.httpclient.core.exec.RequestExecutor;
import io.esastack.httpclient.core.metrics.CallbackExecutorMetric;
import io.esastack.httpclient.core.metrics.IoThreadGroupMetric;
import io.esastack.httpclient.core.metrics.IoThreadMetric;
import io.esastack.httpclient.core.spi.SslEngineFactory;
import io.esastack.httpclient.core.util.BufferUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NettyHttpClientTest {

    private static final RequestExecutor EXECUTOR = mock(RequestExecutor.class);
    private static final SslEngineFactory FACTORY = mock(SslEngineFactory.class);

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new NettyHttpClient(null, mock(CachedChannelPools.class)));
        assertThrows(NullPointerException.class, () -> new NettyHttpClient(HttpClient.create(), null));
        assertDoesNotThrow(() -> new NettyHttpClient(HttpClient.create(), mock(CachedChannelPools.class)));
    }

    @Test
    void testMethods() {
        final HttpClient client = HttpClient.ofDefault();
        final String uri = "http://127.0.0.1:8080/abc";
        checkUriAndMethods(HttpMethod.GET, uri, client.get(uri));
        checkUriAndMethods(HttpMethod.HEAD, uri, client.head(uri));
        checkUriAndMethods(HttpMethod.POST, uri, client.post(uri));
        checkUriAndMethods(HttpMethod.OPTIONS, uri, client.options(uri));
        checkUriAndMethods(HttpMethod.DELETE, uri, client.delete(uri));
        checkUriAndMethods(HttpMethod.TRACE, uri, client.trace(uri));
        checkUriAndMethods(HttpMethod.CONNECT, uri, client.connect(uri));
        checkUriAndMethods(HttpMethod.PATCH, uri, client.patch(uri));
        checkUriAndMethods(HttpMethod.PUT, uri, client.put(uri));
    }

    private void checkUriAndMethods(HttpMethod method, String uri, HttpRequest request) {
        assertEquals(method, request.method());
        assertEquals(uri, request.uri().toString());
    }

    @Test
    void testExecute() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final NettyHttpClientImpl client = new NettyHttpClientImpl(HttpClient.create().useDecompress(true));

        when(EXECUTOR.execute(any(HttpRequest.class),
                any(ExecContext.class)))
                .thenAnswer(answer -> response);

        assertThrows(NullPointerException.class, () -> client.execute(null,
                null, null, null));
        final HttpRequest request = client.get("http://127.0.0.1:8080");

        // Case 1: Accept-Encoding has set
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, Decompression.GZIP.format());
        CompletableFuture<HttpResponse> rsp = client.execute(request, new Context(), null, null);
        then(rsp).isSameAs(response);
        then(request.headers().get(HttpHeaderNames.ACCEPT_ENCODING)).isEqualTo(Decompression.GZIP.format());

        // Case 2: Accept-Encoding is null
        request.headers().remove(HttpHeaderNames.ACCEPT_ENCODING);
        rsp = client.execute(request, new Context(), null, null);
        then(rsp).isSameAs(response);
        then(request.headers().get(HttpHeaderNames.ACCEPT_ENCODING)).isEqualTo(Decompression.GZIP_DEFLATE.format());
    }

    @Test
    void testExecuteAndReleaseBuffer() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final NettyHttpClientImpl client = new NettyHttpClientImpl(HttpClient.create().useDecompress(true));

        when(EXECUTOR.execute(any(HttpRequest.class),
                any(ExecContext.class)))
                .thenAnswer(answer -> response);

        assertThrows(NullPointerException.class, () -> client.execute(null,
                null, null, null));
        final HttpRequest request = client.post("http://127.0.0.1:8080").body("Hello World!".getBytes());

        BufferUtils.toByteBuf(request.buffer()).retain().retain().retain();
        then(BufferUtils.toByteBuf(request.buffer()).refCnt()).isEqualTo(4);
        CompletableFuture<HttpResponse> rsp = client.execute(request, new Context(), null, null);
        then(rsp).isNotSameAs(response);
        response.completeExceptionally(new IOException());
        then(BufferUtils.toByteBuf(request.buffer()).refCnt()).isEqualTo(3);
    }

    @Test
    void testConnectionPoolMetric() {
        final HttpClientBuilder builder = HttpClient.create();
        final CachedChannelPools channelPools = mock(CachedChannelPools.class);

        final NettyHttpClientImpl client = new NettyHttpClientImpl(builder, channelPools);
        then(client.connectionPoolMetric()).isSameAs(channelPools);

    }

    @Test
    void testIoThreadsMetric() {
        final String id = "IO-Threads-Pool-1";
        final IoThreadGroupMetric metric = new NettyHttpClient.IoThreadGroupMetricImpl(
                new NioEventLoopGroup(2), id);

        then(metric.isShutdown()).isFalse();
        then(metric.isTerminated()).isFalse();

        then(metric.groupId()).isEqualTo(id);
        then(metric.childExecutors().size()).isEqualTo(2);

        then(metric.toString()).isEqualTo(new StringJoiner(", ",
                NettyHttpClient.IoThreadGroupMetricImpl.class.getSimpleName()
                        + "[", "]")
                .add("id='" + id + "'")
                .add("shutdown=" + false)
                .add("terminated=" + false)
                .add("childExecutors=" + metric.childExecutors())
                .toString());

        IoThreadMetric childMetric = metric.childExecutors().get(0);
        then(childMetric).isNotNull();

        childMetric.name();
        then(childMetric.ioRatio()).isEqualTo(50);
        childMetric.maxPendingTasks();
        then(childMetric.pendingTasks()).isEqualTo(0);
        childMetric.priority();
        then(childMetric.state()).isEqualTo("RUNNABLE");

        then(childMetric.toString()).contains(
                new StringJoiner(", ", childMetric.getClass().getSimpleName() + "[", "]")
                        .add("name='" + childMetric.name() + "'")
                        .add("pendingTasks=" + 0)
                        .add("maxPendingTasks=" + Integer.MAX_VALUE)
                        .add("ioRatio=" + 50)
                        .add("priority=" + childMetric.priority())
                        .add("state='" + "RUNNABLE" + "'")
                        .toString());

    }

    @Test
    void testCallbackExecutorMetric() {
        final String id = "abc";
        final CallbackThreadPoolOptions options = CallbackThreadPoolOptions.options().coreSize(10).build();

        final CallbackExecutorMetric metric = new NettyHttpClient.CallbackExecutorMetricImpl(
                NettyHttpClient.newCallbackExecutor(options), id);

        then(metric.coreSize()).isEqualTo(options.coreSize());
        then(metric.maxSize()).isEqualTo(options.maxSize());
        then(metric.queueSize()).isEqualTo(0);
        then(metric.poolSize()).isEqualTo(0);
        then(metric.keepAliveSeconds()).isEqualTo(options.keepAliveSeconds());
        then(metric.activeCount()).isEqualTo(0);
        then(metric.taskCount()).isEqualTo(0);
        then(metric.completedTaskCount()).isEqualTo(0);
        then(metric.executorId()).isEqualTo(id);
        then(metric.largestPoolSize()).isEqualTo(0);
        then(metric.toString()).isEqualTo(new StringJoiner(", ",
                NettyHttpClient.CallbackExecutorMetricImpl.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("coreSize=" + options.coreSize())
                .add("maxSize=" + options.maxSize())
                .add("keepAliveSeconds=" + options.keepAliveSeconds())
                .add("activeCount=" + 0)
                .add("poolSize=" + 0)
                .add("largestPoolSize=" + 0)
                .add("taskCount=" + 0)
                .add("queueSize=" + 0)
                .add("completedTaskCount=" + 0)
                .toString());
    }

    @Test
    void testClose() {
        final HttpClientBuilder builder = HttpClient.create();
        final CachedChannelPools channelPools = mock(CachedChannelPools.class);

        final NettyHttpClientImpl client = new NettyHttpClientImpl(builder, channelPools);
        client.close();
        verify(FACTORY).onDestroy();
        verify(channelPools).close();
    }

    @Test
    void testApplyChannelPoolOptions() {
        when(FACTORY.create(any(), anyString(), anyInt())).thenReturn(mock(SSLEngine.class));

        final ChannelPoolOptions preOptions = ChannelPoolOptions.ofDefault();
        final HttpClientBuilder builder = HttpClient.create().channelPoolOptionsProvider(s -> preOptions);

        final CachedChannelPools channelPools = new CachedChannelPools(CacheOptions.ofDefault());
        final NettyHttpClientImpl client = new NettyHttpClientImpl(builder, channelPools);

        final SocketAddress address1 = InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        final SimpleChannelPool underlying1 = mock(FixedChannelPool.class);
        final ChannelPool channelPool1 = new ChannelPool(false, underlying1, preOptions);
        channelPools.put(address1, channelPool1);

        final SocketAddress address2 = InetSocketAddress.createUnresolved("127.0.0.1", 8989);
        final SimpleChannelPool underlying2 = mock(FixedChannelPool.class);
        final ChannelPool channelPool2 = new ChannelPool(false, underlying2, preOptions);
        channelPools.put(address2, channelPool2);

        assertThrows(NullPointerException.class,
                () -> client.applyChannelPoolOptions(null, true));

        final ChannelFuture closeFuture = mock(ChannelFuture.class);
        when(closeFuture.isDone()).thenReturn(true);
        when(closeFuture.isSuccess()).thenReturn(true);

        when(underlying1.closeAsync()).thenAnswer(answer -> closeFuture);
        when(underlying2.closeAsync()).thenAnswer(answer -> closeFuture);

        client.applyChannelPoolOptions(preOptions, true);
        final ChannelPoolOptions newOptions = ChannelPoolOptions.options()
                .connectTimeout(10000)
                .readTimeout(10000)
                .waitingQueueLength(2048)
                .poolSize(1024).build();
        client.applyChannelPoolOptions(newOptions, true);

        final ChannelPool newChannelPool1 = channelPools.getIfPresent(address1);
        then(newChannelPool1).isNotSameAs(channelPool1);
        then(newChannelPool1.options.poolSize()).isEqualTo(1024);
        then(newChannelPool1.options.connectTimeout()).isEqualTo(10000);
        then(newChannelPool1.options.readTimeout()).isEqualTo(10000);
        then(newChannelPool1.options.waitingQueueLength()).isEqualTo(2048);
        then(newChannelPool1.ssl).isEqualTo(false);

        final ChannelPool newChannelPool2 = channelPools.getIfPresent(address2);
        then(newChannelPool2).isNotSameAs(channelPool1);
        then(newChannelPool2.options.poolSize()).isEqualTo(1024);
        then(newChannelPool2.options.connectTimeout()).isEqualTo(10000);
        then(newChannelPool2.options.readTimeout()).isEqualTo(10000);
        then(newChannelPool2.options.waitingQueueLength()).isEqualTo(2048);
        then(newChannelPool2.ssl).isEqualTo(false);

        final ChannelPoolOptions options = ChannelPoolOptions.options()
                .connectTimeout(1000)
                .readTimeout(1000)
                .waitingQueueLength(204)
                .poolSize(102).build();
        Map<SocketAddress, ChannelPoolOptions> optionsMap = new HashMap<>();
        optionsMap.put(address2, options);
        client.applyChannelPoolOptions(optionsMap);
        final ChannelPool newChannelPool4 = channelPools.getIfPresent(address2);
        then(newChannelPool4.options.poolSize()).isEqualTo(102);
        then(newChannelPool4.options.connectTimeout()).isEqualTo(1000);
        then(newChannelPool4.options.readTimeout()).isEqualTo(1000);
        then(newChannelPool4.options.waitingQueueLength()).isEqualTo(204);
    }

    @Test
    void testLoadSslEngineFactory() {
        final NettyHttpClient client = new NettyHttpClientImpl(HttpClient
                .create()
                .version(HttpVersion.HTTP_2), true);

        final SslOptions options = SslOptions.options()
                .sessionTimeout(2000L)
                .sessionCacheSize(512L)
                .handshakeTimeoutMillis(3000L)
                .build();

        SslEngineFactory factory = client.loadSslEngineFactory(options);
        factory.create(options, "127.0.0.1", 8080);
    }

    @Test
    void testNewCallbackExecutor() {
        final CallbackThreadPoolOptions options = CallbackThreadPoolOptions.options()
                .coreSize(1)
                .maxSize(2)
                .blockingQueueLength(3)
                .gracefullyShutdownSeconds(4L)
                .keepAliveSeconds(5L)
                .build();

        final ThreadPoolExecutor executor = NettyHttpClient.newCallbackExecutor(options);
        then(executor.getCorePoolSize()).isEqualTo(1);
        then(executor.getMaximumPoolSize()).isEqualTo(2);
        then(executor.getQueue().size()).isEqualTo(0);
    }

    static final class NettyHttpClientImpl extends NettyHttpClient {

        private final boolean useSuperLoadSslEngineFactory;

        NettyHttpClientImpl(HttpClientBuilder builder, boolean useSuperLoadSslEngineFactory) {
            super(builder, new CachedChannelPools(CacheOptions.ofDefault()));
            this.useSuperLoadSslEngineFactory = useSuperLoadSslEngineFactory;
        }

        private NettyHttpClientImpl(HttpClientBuilder builder) {
            super(builder, new CachedChannelPools(CacheOptions.ofDefault()));
            this.useSuperLoadSslEngineFactory = false;
        }

        private NettyHttpClientImpl(HttpClientBuilder builder, CachedChannelPools channelPools) {
            super(builder, channelPools);
            this.useSuperLoadSslEngineFactory = false;
        }

        @Override
        protected RequestExecutor build(EventLoopGroup ioThreads,
                                        CachedChannelPools channelPools,
                                        ChannelPoolOptions channelPoolOptions) {
            return EXECUTOR;
        }

        @Override
        protected SslEngineFactory loadSslEngineFactory(SslOptions sslOptions) {
            if (useSuperLoadSslEngineFactory) {
                return super.loadSslEngineFactory(sslOptions);
            }
            return FACTORY;
        }
    }

}
