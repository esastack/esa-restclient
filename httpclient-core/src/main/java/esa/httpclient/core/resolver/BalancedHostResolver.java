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
package esa.httpclient.core.resolver;

import esa.commons.Checks;
import esa.commons.loadbalance.LoadBalancer;
import esa.commons.loadbalance.RandomLoadBalancer;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class BalancedHostResolver implements HostResolver {

    private static final LoadBalancer<InetAddress> RANDOM_BALANCER = new RandomLoadBalancer<>();

    private final LoadBalancer<InetAddress> loadBalancer;

    public BalancedHostResolver() {
        this(RANDOM_BALANCER);
    }

    public BalancedHostResolver(LoadBalancer<InetAddress> loadBalancer) {
        Checks.checkNotNull(loadBalancer, "LoadBalancer must not be null");
        this.loadBalancer = loadBalancer;
    }

    @Override
    public final CompletableFuture<InetAddress> resolve(String inetHost) {
        return resolveAll(inetHost).thenApply(addresses -> {
            if (addresses != null && !addresses.isEmpty()) {
                return loadBalancer.select(addresses);
            }

            return null;
        });
    }

    /**
     * Resolves the given {@code inetHost} and returns all resolved {@link InetAddress}s.
     *
     * @param inetHost inetHost
     * @return addresses
     */
    protected abstract CompletableFuture<List<InetAddress>> resolveAll(String inetHost);
}
