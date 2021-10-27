/*
 * Copyright 2021 OPPO ESA Stack Project
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

import esa.commons.loadbalance.LoadBalancer;
import esa.commons.loadbalance.RoundRobinLoadBalancer;
import io.esastack.httpclient.core.util.Futures;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class BalancedHostResolverTest {

    @Test
    void testResolve() throws Throwable {
        final List<InetAddress> addresses = new ArrayList<>();
        final InetAddress address1 = mock(InetAddress.class);
        final InetAddress address2 = mock(InetAddress.class);
        final InetAddress address3 = mock(InetAddress.class);

        addresses.add(address1);
        addresses.add(address2);
        addresses.add(address3);

        final Function<String, CompletableFuture<List<InetAddress>>> function =
                (address) -> Futures.completed(addresses);

        final HostResolver resolver = new HostResolverImpl(new RoundRobinLoadBalancer<>(), function);
        assertSame(address1, resolver.resolve("localhost").get());
        assertSame(address2, resolver.resolve("localhost").get());
        assertSame(address3, resolver.resolve("localhost").get());
    }

    private static class HostResolverImpl extends BalancedHostResolver {

        private final Function<String, CompletableFuture<List<InetAddress>>> function;

        private HostResolverImpl(LoadBalancer<InetAddress> loadBalancer,
                                 Function<String, CompletableFuture<List<InetAddress>>> function) {
            super(loadBalancer);
            this.function = function;
        }

        @Override
        protected CompletableFuture<List<InetAddress>> resolveAll(String inetHost) {
            return function.apply(inetHost);
        }
    }
}

