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

import esa.commons.http.HttpHeaderNames;
import esa.commons.http.HttpMethod;
import esa.commons.http.HttpVersion;
import esa.httpclient.core.Context;
import esa.httpclient.core.HttpClient;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpRequestBuilder;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.Listener;
import esa.httpclient.core.config.CacheOptions;
import esa.httpclient.core.config.CallbackThreadPoolOptions;
import esa.httpclient.core.config.ChannelPoolOptions;
import esa.httpclient.core.config.Decompression;
import esa.httpclient.core.config.SslOptions;
import esa.httpclient.core.exec.RequestExecutor;
import esa.httpclient.core.metrics.CallbackExecutorMetric;
import esa.httpclient.core.metrics.IoThreadGroupMetric;
import esa.httpclient.core.metrics.IoThreadMetric;
import esa.httpclient.core.spi.SslEngineFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.BDDAssertions.then;
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

    @BeforeAll
    static void setUp() {
        when(FACTORY.create(any(SslOptions.class), anyString(), anyInt())).thenReturn(null);
    }

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new NettyHttpClient(null, mock(ChannelPools.class)));
        assertThrows(NullPointerException.class, () -> new NettyHttpClient(HttpClient.create(), null));
        new NettyHttpClient(HttpClient.create(), mock(ChannelPools.class));
    }

    @Test
    void testExecute() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final NettyHttpClientImpl client = new NettyHttpClientImpl(HttpClient.create().useDecompress(true));

        when(EXECUTOR.async(any(HttpRequest.class),
                any(Context.class),
                any(Listener.class)))
                .thenAnswer(answer -> response);

        assertThrows(NullPointerException.class, () -> client.execute(null));
        final HttpRequest request = HttpRequest.get("http://127.0.0.1:8080").build();

        // Case 1: Accept-Encoding has set
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, Decompression.GZIP.format());
        CompletableFuture<HttpResponse> rsp = client.execute(request);
        then(rsp).isSameAs(response);
        then(request.headers().get(HttpHeaderNames.ACCEPT_ENCODING)).isEqualTo(Decompression.GZIP.format());

        // Case 2: Accept-Encoding is null
        request.headers().remove(HttpHeaderNames.ACCEPT_ENCODING);
        rsp = client.execute(request);
        then(rsp).isSameAs(response);
        then(request.headers().get(HttpHeaderNames.ACCEPT_ENCODING)).isEqualTo(Decompression.GZIP_DEFLATE.format());
    }

    @Test
    void testPrepare() {
        final NettyHttpClientImpl client = new NettyHttpClientImpl(HttpClient.create());

        assertThrows(IllegalArgumentException.class, () -> client.prepare(null));

        // default aggregate, compute by handle or handler
        final HttpRequestBuilder.ClassicChunk builder = client.prepare("http://127.0.0.1:8080/abc")
                .handle(h -> h.onError(th -> { }));

        then(builder.build().method()).isSameAs(HttpMethod.POST);
        builder.method(HttpMethod.GET);
        then(builder.build().method()).isSameAs(HttpMethod.GET);

        then(builder.build().aggregate()).isFalse();

        builder.handle(null).handler(null);
        then(builder.build().aggregate()).isTrue();

        builder.aggregate(false);
        then(builder.build().aggregate()).isFalse();
    }

    @Test
    void testAsync() {
        final CompletableFuture<HttpResponse> response = new CompletableFuture<>();
        final NettyHttpClientImpl client = new NettyHttpClientImpl(HttpClient.create().useDecompress(true));

        assertThrows(NullPointerException.class, () -> client.async(null));

        when(EXECUTOR.async(any(HttpRequest.class),
                any(Context.class),
                any(Listener.class)))
                .thenAnswer(answer -> response);

        final HttpRequest request = HttpRequest.get("http://127.0.0.1:8080").build();

        // Case 1: Accept-Encoding has set
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, Decompression.GZIP.format());
        CompletableFuture<HttpResponse> rsp = client.execute(request);
        then(rsp).isSameAs(response);
        then(request.headers().get(HttpHeaderNames.ACCEPT_ENCODING)).isEqualTo(Decompression.GZIP.format());

        // Case 2: Accept-Encoding is null
        request.headers().remove(HttpHeaderNames.ACCEPT_ENCODING);
        rsp = client.async(request);
        then(rsp).isSameAs(response);
        then(request.headers().get(HttpHeaderNames.ACCEPT_ENCODING)).isEqualTo(Decompression.GZIP_DEFLATE.format());
    }

    @Test
    void testConnectionPoolMetric() {
        final HttpClientBuilder builder = HttpClient.create();
        final ChannelPools channelPools = mock(ChannelPools.class);

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

        IoThreadMetric childMetric = metric.childExecutors().get(0);
        then(childMetric).isNotNull();

        childMetric.name();
        then(childMetric.ioRatio()).isEqualTo(50);
        childMetric.maxPendingTasks();
        then(childMetric.pendingTasks()).isEqualTo(0);
        childMetric.priority();
        then(childMetric.state()).isEqualTo("RUNNABLE");
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
    }

    @Test
    void testClose() {
        final HttpClientBuilder builder = HttpClient.create();
        final ChannelPools channelPools = mock(ChannelPools.class);

        final NettyHttpClientImpl client = new NettyHttpClientImpl(builder, channelPools);
        client.close();
        verify(FACTORY).onDestroy();
        verify(channelPools).close();
    }

    @Test
    void testApplyChannelPoolOptions() {
        final ChannelPoolOptions preOptions = ChannelPoolOptions.ofDefault();
        final HttpClientBuilder builder = HttpClient.create().channelPoolOptionsProvider(s -> preOptions);

        final ChannelPools channelPools = new ChannelPools(CacheOptions.ofDefault());
        final NettyHttpClientImpl client = new NettyHttpClientImpl(builder, channelPools);

        final SocketAddress address1 = InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        final SimpleChannelPool underlying1 = mock(FixedChannelPool.class);
        final ChannelPool channelPool1 = new ChannelPool(underlying1, preOptions, false, () -> null);
        channelPools.put(address1, channelPool1);

        final SocketAddress address2 = InetSocketAddress.createUnresolved("127.0.0.1", 8989);
        final SimpleChannelPool underlying2 = mock(FixedChannelPool.class);
        final ChannelPool channelPool2 = new ChannelPool(underlying2, preOptions, false, () -> null);
        channelPools.put(address2, channelPool2);

        assertThrows(NullPointerException.class,
                () -> client.applyChannelPoolOptions(null, true));

        final ChannelFuture closeFuture = mock(ChannelFuture.class);
        when(closeFuture.isDone()).thenReturn(true);
        when(closeFuture.isSuccess()).thenReturn(true);

        when(underlying1.closeAsync()).thenAnswer(answer -> closeFuture);
        when(underlying2.closeAsync()).thenAnswer(answer -> closeFuture);

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

        then(client.builder.connectionPoolSize()).isEqualTo(1024);
        then(client.builder.connectTimeout()).isEqualTo(10000);
        then(client.builder.readTimeout()).isEqualTo(10000);
        then(client.builder.connectionPoolWaitingQueueLength()).isEqualTo(2048);
    }

    @Test
    void testLoadSslEngineFactory() {
        final NettyHttpClient client = new NettyHttpClientImpl(HttpClient
                .create()
                .version(HttpVersion.HTTP_2));

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
            super(builder, new ChannelPools(CacheOptions.ofDefault()));
            this.useSuperLoadSslEngineFactory = useSuperLoadSslEngineFactory;
        }

        private NettyHttpClientImpl(HttpClientBuilder builder) {
            super(builder, new ChannelPools(CacheOptions.ofDefault()));
            this.useSuperLoadSslEngineFactory = false;
        }

        private NettyHttpClientImpl(HttpClientBuilder builder, ChannelPools channelPools) {
            super(builder, channelPools);
            this.useSuperLoadSslEngineFactory = false;
        }

        @Override
        protected RequestExecutor build(EventLoopGroup ioThreads,
                                        ChannelPools channelPools,
                                        HttpClientBuilder builder) {
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
