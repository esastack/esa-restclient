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

import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.HttpClientBuilder;
import io.esastack.httpclient.core.config.CacheOptions;
import io.esastack.httpclient.core.config.ChannelPoolOptions;
import io.esastack.httpclient.core.metrics.ConnectionPoolMetric;
import io.esastack.httpclient.core.spi.SslEngineFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.StringJoiner;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachedChannelPoolsTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new CachedChannelPools(null));
    }

    @Test
    void testBasicOperation() {
        final SocketAddress address1 = InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        final SocketAddress address2 = InetSocketAddress.createUnresolved("127.0.0.1", 8081);

        final ChannelPool pool1 = new ChannelPool(false, mock(io.netty.channel.pool.ChannelPool.class),
                ChannelPoolOptions.ofDefault());

        final ChannelPool pool2 = new ChannelPool(false, mock(io.netty.channel.pool.ChannelPool.class),
                ChannelPoolOptions.ofDefault());

        final CachedChannelPools pools1 = new CachedChannelPools(CacheOptions.ofDefault());
        pools1.put(address1, pool1);
        pools1.put(address1, pool2);
        pools1.put(address2, pool2);
        then(pools1.getIfPresent(address1)).isSameAs(pool2);
        then(pools1.getIfPresent(address2)).isSameAs(pool2);
        pools1.close();

        final CachedChannelPools pools2 = new CachedChannelPools(CacheOptions.ofDefault());
        then(pools2.getIfPresent(address1)).isNull();
        then(pools2.getIfPresent(address2)).isNull();

        pools2.getOrCreate(true, address1, (addr) -> pool1);
        pools2.getOrCreate(true, address2, (addr) -> pool2);
        then(pools2.getIfPresent(address1)).isSameAs(pool1);
        then(pools2.getIfPresent(address2)).isSameAs(pool2);

        // Connection which keepAlive is true will be cached.
        final ChannelPool pool11 = pools2.getOrCreate(true, address1, (addr) -> pool1);
        final ChannelPool pool12 = pools2.getOrCreate(true, address1, (addr) -> pool2);
        then(pool11).isSameAs(pool12);

        // Connection which keepAlive is false won't be cached.
        final ChannelPool pool21 = pools2.getOrCreate(false, address1, (addr) -> pool1);
        final ChannelPool pool22 = pools2.getOrCreate(false, address1, (addr) -> pool2);
        then(pool21).isNotSameAs(pool22);

        pools2.close();
    }

    @Test
    void testAfterClosing() {
        final SocketAddress address = InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        final ChannelPool pool = new ChannelPool(false, mock(io.netty.channel.pool.ChannelPool.class),
                ChannelPoolOptions.ofDefault());
        final CachedChannelPools pools = new CachedChannelPools(CacheOptions.ofDefault());
        pools.close();
        assertThrows(IllegalStateException.class, () -> pools.getIfPresent(address));
        assertThrows(IllegalStateException.class, () -> pools.getOrCreate(true, address,
                (addr) -> pool));
        assertThrows(IllegalStateException.class, () -> pools.put(address, pool));
        then(pools.get(address)).isNull();
        then(pools.all()).isSameAs(Collections.emptyMap());
    }

    @Test
    void testMetric() {
        final SocketAddress address = InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        final ChannelPoolFactory factory = new ChannelPoolFactory(mock(SslEngineFactory.class));

        final EventLoopGroup group = new NioEventLoopGroup(1);
        final HttpClientBuilder builder = HttpClient.create();

        final CachedChannelPools pools = new CachedChannelPools(CacheOptions.ofDefault());
        pools.getOrCreate(true, address, (addr) -> factory.create(false, true,
                addr, group, ChannelPoolOptions.ofDefault(), builder));

        final ChannelPoolOptions options = ChannelPoolOptions.ofDefault();
        final ConnectionPoolMetric metric = pools.get(address);
        assert metric != null;
        then(metric.active()).isEqualTo(0);
        then(metric.maxPendingAcquires()).isEqualTo(options.waitingQueueLength());
        then(metric.pendingAcquireCount()).isEqualTo(0);

        then(metric.maxSize()).isEqualTo(options.poolSize());
        then(metric.options()).isEqualTo(options);

        then(metric.toString()).isEqualTo(
                new StringJoiner(", ", metric.getClass().getSimpleName() + "[", "]")
                .add("options=" + options)
                .add("maxSize=" + options.poolSize())
                .add("maxPendingAcquires=" + options.waitingQueueLength())
                .add("active=" + 0)
                .add("pendingAcquireCount=" + 0)
                .toString());

        then(pools.all().size()).isEqualTo(1);
        then(pools.all().get(address)).isNotNull();
        pools.close();
    }

    @Test
    void testClose() {
        final CachedChannelPools channelPools = new CachedChannelPools(CacheOptions.ofDefault());

        final SocketAddress address1 = InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        final io.netty.channel.pool.ChannelPool underlying1 = mock(io.netty.channel.pool.ChannelPool.class);

        channelPools.put(address1, new ChannelPool(false, underlying1, ChannelPoolOptions.ofDefault()));

        final SocketAddress address2 = InetSocketAddress.createUnresolved("127.0.0.1", 8989);
        final io.netty.channel.pool.ChannelPool underlying2 = mock(io.netty.channel.pool.ChannelPool.class);

        channelPools.put(address2, new ChannelPool(false, underlying2, ChannelPoolOptions.ofDefault()));

        channelPools.close();
        verify(underlying1).close();
        verify(underlying2).close();

        assertThrows(IllegalStateException.class, () -> channelPools.put(address1, null));
    }

    @Test
    void testStaticClose() {
        final CachedChannelPools channelPools = new CachedChannelPools(CacheOptions.ofDefault());

        final SocketAddress address1 = InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        final FixedChannelPool underlying1 = mock(io.netty.channel.pool.FixedChannelPool.class);
        final ChannelPool channelPool1 = new ChannelPool(false, underlying1, ChannelPoolOptions.ofDefault());

        channelPools.put(address1, channelPool1);

        final SocketAddress address2 = InetSocketAddress.createUnresolved("127.0.0.1", 8989);
        final io.netty.channel.pool.ChannelPool underlying2 = mock(io.netty.channel.pool.FixedChannelPool.class);
        final ChannelPool channelPool2 = new ChannelPool(false, underlying2, ChannelPoolOptions.ofDefault());

        channelPools.put(address2, channelPool2);

        final ChannelFuture closeFuture = mock(ChannelFuture.class);
        when(underlying1.closeAsync()).then(answer -> closeFuture);
        when(closeFuture.isDone()).thenReturn(true);
        when(closeFuture.isSuccess()).thenReturn(true);
        CachedChannelPools.close(address1, channelPool1, true);
        verify(underlying1).closeAsync();

        CachedChannelPools.close(address2, channelPool2, false);
        verify(underlying2).close();
    }
}
