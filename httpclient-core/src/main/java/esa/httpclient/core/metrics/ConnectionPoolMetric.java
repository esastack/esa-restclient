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
package esa.httpclient.core.metrics;

import esa.httpclient.core.config.ChannelPoolOptions;

public interface ConnectionPoolMetric {

    /**
     * Obtains the {@code maxSize} of current channel pool.
     *
     * @return maxSize
     */
    int maxSize();

    /**
     * Obtains the {@code maxPendingAcquires} of current channel pool.
     *
     * @return maxPendingAcquires
     */
    int maxPendingAcquires();

    /**
     * Obtains the {@code active} connection of current pool.
     *
     * @return the number active channel
     */
    int active();

    /**
     * Obtains the {@code pendingAcquireCount} of current pool.
     *
     * @return the count of pending acquire
     */
    int pendingAcquireCount();

    /**
     * Obtains the {@link ChannelPoolOptions} of current channel pool.
     *
     * @return options
     */
    ChannelPoolOptions options();

}
