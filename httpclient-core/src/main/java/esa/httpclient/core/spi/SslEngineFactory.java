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
package esa.httpclient.core.spi;

import esa.httpclient.core.config.SslOptions;

import javax.net.ssl.SSLEngine;

public interface SslEngineFactory {

    /**
     * Creates a {@link SSLEngine} by given {@code options}, {@code peerHost} and {@code peerPort}.
     *
     * @param options  options
     * @param peerHost peerHost
     * @param peerPort peerPort
     * @return engine
     */
    SSLEngine create(SslOptions options, String peerHost, int peerPort);

    /**
     * Be informed before destroying.
     */
    default void onDestroy() {

    }

}
