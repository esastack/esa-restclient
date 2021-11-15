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

import io.esastack.httpclient.core.util.Futures;

import java.net.InetAddress;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The default implementation of {@link HostResolver} which use {@link InetAddress#getAllByName(String)} to get
 * ips by host name.
 */
public class SystemDefaultResolver extends BalancedHostResolver {

    @Override
    protected CompletableFuture<List<InetAddress>> resolveAll(String inetHost) {
        try {
            return Futures.completed(Arrays.asList(AccessController.doPrivileged(
                    (PrivilegedExceptionAction<InetAddress[]>) () -> InetAddress.getAllByName(inetHost))));
        } catch (Throwable th) {
            return Futures.completed(th);
        }
    }
}
