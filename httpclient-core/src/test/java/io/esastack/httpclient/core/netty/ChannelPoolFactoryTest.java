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
import io.esastack.httpclient.core.config.ChannelPoolOptions;
import io.esastack.httpclient.core.config.NetOptions;
import io.esastack.httpclient.core.resolver.SystemDefaultResolver;
import io.esastack.httpclient.core.spi.SslEngineFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Java6BDDAssertions.then;
import static org.mockito.Mockito.mock;

class ChannelPoolFactoryTest {

    @Test
    void testBuildBootstrap() {
        final SocketAddress address = InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        final EventLoopGroup group = new NioEventLoopGroup();

        final NetOptions options = NetOptions.options().soRcvBuf(10)
                .soSndBuf(100)
                .soKeepAlive(true)
                .soLinger(1000)
                .soKeepAlive(true)
                .soReuseAddr(true)
                .tcpNoDelay(true)
                .writeBufferHighWaterMark(2000)
                .writeBufferLowWaterMark(1000).build();
        final int connectTimeout = ThreadLocalRandom.current().nextInt(2000);
        final Bootstrap bootstrap = ChannelPoolFactory
                .buildBootstrap(address,
                        group,
                        options,
                        connectTimeout,
                        new SystemDefaultResolver());

        then(bootstrap.config().remoteAddress()).isSameAs(address);
        then(bootstrap.config().group()).isSameAs(group);
        then(bootstrap.config().options().get(ChannelOption.CONNECT_TIMEOUT_MILLIS)).isEqualTo(connectTimeout);
        then(bootstrap.config().options().get(ChannelOption.SO_RCVBUF)).isEqualTo(10);
        then(bootstrap.config().options().get(ChannelOption.SO_SNDBUF)).isEqualTo(100);
        then(bootstrap.config().options().get(ChannelOption.SO_LINGER)).isEqualTo(1000);
        then(bootstrap.config().options().get(ChannelOption.SO_REUSEADDR)).isEqualTo(true);
        then(bootstrap.config().options().get(ChannelOption.SO_KEEPALIVE)).isEqualTo(true);
        then(bootstrap.config().options().get(ChannelOption.TCP_NODELAY)).isEqualTo(true);
    }

    @Test
    void testCreate() {
        final ChannelPoolFactory factory = new ChannelPoolFactory(mock(SslEngineFactory.class));
        final SocketAddress address = InetSocketAddress.createUnresolved("127.0.0.1", 8080);

        final ChannelPool channelPool0 = factory.create(false, true, address,
                mock(EventLoopGroup.class), ChannelPoolOptions.ofDefault(), HttpClient.create());
        then(channelPool0.ssl).isFalse();
        then(channelPool0.underlying).isInstanceOf(FixedChannelPool.class);

        final ChannelPool channelPool1 = factory.create(false, false, address,
                mock(EventLoopGroup.class), ChannelPoolOptions.ofDefault(), HttpClient.create());
        then(channelPool1.ssl).isFalse();
        then(channelPool1.underlying).isInstanceOf(SimpleChannelPool.class);
        then(channelPool1.underlying).isNotInstanceOf(FixedChannelPool.class);
    }

}
