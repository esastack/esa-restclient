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
import esa.httpclient.core.resolver.HostResolver;
import io.netty.resolver.InetNameResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;

import java.net.InetAddress;
import java.util.List;

class DelegatingResolver extends InetNameResolver {

    private final HostResolver delegate;

    DelegatingResolver(EventExecutor executor, HostResolver delegate) {
        super(executor);
        Checks.checkNotNull(delegate, "delegate");
        this.delegate = delegate;
    }

    @Override
    protected void doResolve(String inetHost, Promise<InetAddress> promise) {
        delegate.resolve(inetHost).whenComplete((address, th) -> {
            if (th != null) {
                promise.setFailure(th);
            } else {
                promise.setSuccess(address);
            }
        });
    }

    @Override
    protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise) {
        throw new UnsupportedOperationException("Please use doResolve() instead of doResolveAll()");
    }
}
