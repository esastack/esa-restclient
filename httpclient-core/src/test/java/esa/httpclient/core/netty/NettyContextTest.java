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

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.BDDAssertions.then;

class NettyContextTest {

    @Test
    void testSetAndGet100ContinueCallback() {
        final NettyContext context = new NettyContext();
        final Runnable runnable = () -> {
        };
        context.set100ContinueCallback(runnable);
        then(context.remove100ContinueCallback()).isSameAs(runnable);

        then(context.remove100ContinueCallback()).isNull();
    }

    @Test
    void testSetAndGetWriter() {
        final CompletableFuture<SegmentWriter> writer = new CompletableFuture<>();
        final NettyContext context = new NettyContext();
        context.setWriter(writer);
        then(context.getWriter().orElse(null)).isSameAs(writer);
        context.setWriter(null);
        then(context.getWriter().isPresent()).isFalse();
    }
}
