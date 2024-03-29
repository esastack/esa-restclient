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
package io.esastack.httpclient.core.resolver;

import java.io.Closeable;
import java.net.InetAddress;
import java.util.concurrent.CompletionStage;

/**
 * This class is designed to resolve given host to detailed ips. eg: localhost : 127.0.0.1
 */
public interface HostResolver extends Closeable {

    /**
     * Resolves the {@code inetHost} to {@link InetAddress} asynchronously. The resolved {@link InetAddress} will be
     * used while connecting remote host.
     *
     * @param inetHost host
     * @return address
     */
    CompletionStage<InetAddress> resolve(String inetHost);

    /**
     * Closes the {@link HostResolver}.
     */
    @Override
    default void close() {

    }
}
