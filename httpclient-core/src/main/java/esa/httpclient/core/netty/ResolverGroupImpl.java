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
import esa.commons.io.IOUtils;
import esa.httpclient.core.resolver.HostResolver;
import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.util.concurrent.EventExecutor;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

class ResolverGroupImpl extends AddressResolverGroup<InetSocketAddress> {

    private static final ConcurrentHashMap<HostResolver, ResolverGroupImpl> RESOLVER_GROUPS =
            new ConcurrentHashMap<>(1);

    private final HostResolver delegate;

    private ResolverGroupImpl(HostResolver delegate) {
        Checks.checkNotNull(delegate, "HostResolver must not be null");
        this.delegate = delegate;
    }

    @Override
    protected AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) {
        return new DelegatingResolver(executor, delegate).asAddressResolver();
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(delegate);
        super.close();
    }

    static ResolverGroupImpl mappingTo(HostResolver hostResolver) {
        return RESOLVER_GROUPS.computeIfAbsent(hostResolver, resolver -> new ResolverGroupImpl(hostResolver));
    }
}
