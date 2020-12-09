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

import esa.httpclient.core.config.ChannelPoolOptions;

import java.net.SocketAddress;
import java.util.Map;

public interface ModifiableClient<CLIENT> {

    /**
     * Applies the newest {@link ChannelPoolOptions} to current {@link HttpClient}.
     *
     * @param options options
     * @return client
     */
    default CLIENT applyChannelPoolOptions(ChannelPoolOptions options) {
        return applyChannelPoolOptions(options, true);
    }

    /**
     * Applies the newest {@link ChannelPoolOptions} to current {@link HttpClient}.
     *
     * @param options        options
     * @param applyToExisted apply the newest options to the existed channelPools or not.
     *                       If false, only update the fields of {@link HttpClientBuilder}, otherwise update
     *                       the fields of {@link HttpClientBuilder} and update new channelPool instances.
     * @return client
     */
    CLIENT applyChannelPoolOptions(ChannelPoolOptions options, boolean applyToExisted);

    /**
     * Applies the newest {@link ChannelPoolOptions} map.
     *
     * @param options options
     * @return client
     */
    CLIENT applyChannelPoolOptions(Map<SocketAddress, ChannelPoolOptions> options);

}
