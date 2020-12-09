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

import esa.httpclient.core.resolver.HostResolver;
import esa.httpclient.core.resolver.SystemDefaultResolver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.BDDAssertions.then;

class ResolverGroupImplTest {

    @Test
    void testMappingTo() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(10);
        final ConcurrentHashMap<Integer, ResolverGroupImpl> resolvers = new ConcurrentHashMap<>(10);

        final HostResolver resolver = new SystemDefaultResolver();
        for (int i = 0; i < 10; i++) {
            final int key = i;
            new Thread(() -> {
                try {
                    resolvers.putIfAbsent(key, ResolverGroupImpl.mappingTo(resolver));
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        final ResolverGroupImpl resolverGroup = resolvers.get(0);
        for (int i = 0; i < 10; i++) {
            then(resolvers.get(i)).isSameAs(resolverGroup);
        }
    }

    @Test
    void testClose() {
        final AtomicInteger count = new AtomicInteger();

        final HostResolver resolver = new SystemDefaultResolver() {
            @Override
            public void close() {
                count.incrementAndGet();
            }
        };
        final ResolverGroupImpl resolverGroup = ResolverGroupImpl.mappingTo(resolver);
        resolverGroup.close();
        then(count.get()).isEqualTo(1);
    }

}
