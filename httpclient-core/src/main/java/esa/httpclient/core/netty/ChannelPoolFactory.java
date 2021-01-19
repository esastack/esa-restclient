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
import esa.commons.function.ThrowingSupplier;
import esa.httpclient.core.HttpClientBuilder;
import esa.httpclient.core.config.ChannelPoolOptions;
import esa.httpclient.core.config.NetOptions;
import esa.httpclient.core.resolver.HostResolver;
import esa.httpclient.core.spi.ChannelPoolOptionsProvider;
import esa.httpclient.core.util.LoggerUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.internal.SystemPropertyUtil;

import java.net.SocketAddress;

final class ChannelPoolFactory {

    static final NettyClientConfigure NETTY_CONFIGURE = new NettyClientConfigureImpl();

    private static final String PREFER_UNPOOLED_KEY = "esa.httpclient.preferUnpooled";
    private static final boolean PREFER_UNPOOLED =
            SystemPropertyUtil.getBoolean(PREFER_UNPOOLED_KEY, false);

    private static final String PREFER_NATIVE_KEY = "esa.httpclient.preferNative";
    static final boolean PREFER_NATIVE = SystemPropertyUtil.getBoolean(PREFER_NATIVE_KEY, true);

    ChannelPoolFactory() {
    }

    ChannelPool create(boolean ssl,
                       boolean keepAlive,
                       SocketAddress address,
                       EventLoopGroup ioThreads,
                       HttpClientBuilder builder,
                       ChannelPoolOptions options,
                       ThrowingSupplier<SslHandler> sslHandler) {
        final Bootstrap bootstrap = buildBootstrap(address,
                ioThreads,
                builder.netOptions(),
                options.connectTimeout(),
                builder.resolver());

        NETTY_CONFIGURE.onBootstrapCreated(address, bootstrap);

        final ChannelPoolHandler handler = new AbstractChannelPoolHandler() {
            @Override
            public void channelReleased(Channel ch) {
                ch.flush();
            }

            @Override
            public void channelCreated(Channel ch) {

            }
        };
        final ChannelInitializer initializer = new ChannelInitializer(builder, sslHandler, ssl);
        final io.netty.channel.pool.ChannelPool underlying;
        if (keepAlive) {
            LoggerUtils.logger().info("Begin to create a new connection pool, address: {}, options: {}",
                    address, options);
            underlying = new ChannelPoolImpl(bootstrap,
                    handler,
                    initializer,
                    ChannelHealthChecker.ACTIVE,
                    FixedChannelPool.AcquireTimeoutAction.FAIL,
                    options.connectTimeout(),
                    options.poolSize(),
                    options.waitingQueueLength());
        } else {
            LoggerUtils.logger().debug("Begin to create a new connection pool, address: {}, options: {}",
                    address, options);
            underlying = new DirectConnectAndCloseChannelPool(bootstrap, handler, initializer);
        }

        return new ChannelPool(underlying,
                options,
                ssl,
                sslHandler);
    }

    ChannelPool create(boolean ssl,
                       boolean keepAlive,
                       SocketAddress address,
                       EventLoopGroup ioThreads,
                       HttpClientBuilder builder,
                       ThrowingSupplier<SslHandler> sslHandler) {
        return create(ssl,
                keepAlive,
                address,
                ioThreads,
                builder,
                detectOptions(address, builder),
                sslHandler);
    }

    /**
     * Designed as package visibility for unit test purpose.
     *
     * @param address       address
     * @param builder       builder
     * @return              options
     */
    static ChannelPoolOptions detectOptions(SocketAddress address,
                                            HttpClientBuilder builder) {
        ChannelPoolOptionsProvider provider;
        ChannelPoolOptions channelPoolOptions = null;
        if ((provider = builder.channelPoolOptionsProvider()) != null) {
            channelPoolOptions = provider.get(address);
        }
        if (channelPoolOptions != null) {
            return channelPoolOptions;
        }

        return ChannelPoolOptions.options()
                .poolSize(builder.connectionPoolSize())
                .connectTimeout(builder.connectTimeout())
                .waitingQueueLength(builder.connectionPoolWaitingQueueLength())
                .readTimeout(builder.readTimeout())
                .build();
    }

    /**
     * Designed as package visibility for unit test purpose.
     *
     * @param address           address
     * @param ioThreads         ioThreads
     * @param netOptions        net options
     * @param connectTimeout    connect timeout
     * @param resolver          resolver
     * @return                  bootstrap
     */
    static Bootstrap buildBootstrap(SocketAddress address,
                                    EventLoopGroup ioThreads,
                                    NetOptions netOptions,
                                    int connectTimeout,
                                    HostResolver resolver) {
        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ioThreads);
        if (PREFER_NATIVE && Epoll.isAvailable()) {
            bootstrap.channel(EpollSocketChannel.class);
        } else {
            bootstrap.channel(NioSocketChannel.class);
        }

        if (netOptions != null) {
            applyNetOptions(bootstrap, netOptions);
        }

        if (PREFER_UNPOOLED) {
            bootstrap.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);
        }

        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        if (resolver != null) {
            bootstrap.resolver(ResolverGroupImpl.mappingTo(resolver));
        }

        bootstrap.remoteAddress(address);

        return bootstrap;
    }

    private static void applyNetOptions(Bootstrap bootstrap, NetOptions options) {
        if (options.isSoKeepAlive()) {
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        }
        if (options.isTcpNoDelay()) {
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
        }
        if (options.isSoReuseAddr()) {
            bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        }
        if (options.soRcvBuf() > 0) {
            bootstrap.option(ChannelOption.SO_RCVBUF, options.soRcvBuf());
        }
        if (options.soSndBuf() > 0) {
            bootstrap.option(ChannelOption.SO_SNDBUF, options.soSndBuf());
        }
        if (options.soLinger() > 0) {
            bootstrap.option(ChannelOption.SO_LINGER, options.soLinger());
        }
    }

    private static final class ChannelPoolImpl extends FixedChannelPool {

        private final ChannelInitializer initializer;

        private ChannelPoolImpl(Bootstrap bootstrap,
                                io.netty.channel.pool.ChannelPoolHandler handler,
                                ChannelInitializer initializer,
                                ChannelHealthChecker healthCheck,
                                AcquireTimeoutAction action,
                                final long acquireTimeoutMillis,
                                int maxConnections,
                                int maxPendingAcquires) {
            super(bootstrap, handler, healthCheck, action,
                    acquireTimeoutMillis, maxConnections, maxPendingAcquires);
            Checks.checkNotNull(initializer, "ChannelInitializer must not be null");
            this.initializer = initializer;
        }

        @Override
        protected ChannelFuture connectChannel(Bootstrap bs) {
            return initializer.onConnected(super.connectChannel(bs));
        }
    }

    private static final class DirectConnectAndCloseChannelPool extends SimpleChannelPool {

        private final ChannelInitializer initializer;

        private DirectConnectAndCloseChannelPool(Bootstrap bootstrap,
                                                 ChannelPoolHandler handler,
                                                 ChannelInitializer initializer) {
            super(bootstrap, handler, ChannelHealthChecker.ACTIVE, false, false);
            Checks.checkNotNull(initializer, "ChannelInitializer must not be null");
            this.initializer = initializer;
        }

        @Override
        protected Channel pollChannel() {
            return null;
        }

        @Override
        protected boolean offerChannel(Channel channel) {
            channel.close();
            return true;
        }

        @Override
        protected ChannelFuture connectChannel(Bootstrap bs) {
            return initializer.onConnected(super.connectChannel(bs));
        }
    }

}
