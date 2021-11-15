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

import esa.commons.spi.SpiLoader;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.List;

class NettyClientConfigureImpl implements NettyClientConfigure {

    private static final List<NettyClientConfigure> CONFIGURES = SpiLoader.getAll(NettyClientConfigure.class);

    @Override
    public void onBootstrapCreated(SocketAddress address, Bootstrap bootstrap) {
        CONFIGURES.forEach(configure -> configure.onBootstrapCreated(address, bootstrap));
    }

    @Override
    public void onChannelCreated(Channel channel) {
        CONFIGURES.forEach(configure -> configure.onChannelCreated(channel));
    }

}
