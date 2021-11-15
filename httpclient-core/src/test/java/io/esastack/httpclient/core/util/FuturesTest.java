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
package io.esastack.httpclient.core.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FuturesTest {

    @Test
    void testCompletedEx() {
        final CompletableFuture<String> future = Futures.completed(new RuntimeException());
        then(future.isCompletedExceptionally()).isTrue();
        then(future.isDone()).isTrue();

        assertThrows(IllegalArgumentException.class, () -> Futures.completed(null));
    }

    @Test
    void testCompletedN() throws Exception {
        final CompletableFuture<String> future = Futures.completed();
        then(future.isCompletedExceptionally()).isFalse();
        then(future.isDone()).isTrue();
        then(future.get()).isNull();
    }

    @Test
    void testCompletedV() throws Exception {
        final CompletableFuture<String> future1 = Futures.completed((String) null);
        then(future1.isCompletedExceptionally()).isFalse();
        then(future1.isDone()).isTrue();
        then(future1.get()).isNull();

        final CompletableFuture<String> future2 = Futures.completed("A");
        then(future2.isCompletedExceptionally()).isFalse();
        then(future2.isDone()).isTrue();
        then(future2.get()).isEqualTo("A");
    }

    @Test
    void testUnwrapped() {
        then(Futures.unwrapped(null)).isNull();

        final Exception ex = new RuntimeException();
        then(Futures.unwrapped(new CompletionException(ex))).isSameAs(ex);
        then(Futures.unwrapped(new ExecutionException(ex))).isSameAs(ex);

        then(Futures.unwrapped(ex)).isSameAs(ex);
    }

    @Test
    void testGetCause() {
        final CompletableFuture<String> future0 = new CompletableFuture<>();
        then(Futures.getCause(future0)).isNull();

        future0.complete("ABC");
        then(Futures.getCause(future0)).isNull();

        final CompletableFuture<String> future1 = new CompletableFuture<>();
        final Exception ex = new IOException();
        future1.completeExceptionally(ex);
        then(Futures.getCause(future1)).isSameAs(ex);
    }
}
