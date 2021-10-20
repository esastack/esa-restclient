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

import esa.commons.Checks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public final class Futures {

    private Futures() {
    }

    public static <T> CompletableFuture<T> completed(Throwable t) {
        Checks.checkArg(t != null, "throwable must not be null");
        final CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(t);
        return future;
    }

    public static <T> CompletableFuture<T> completed() {
        return CompletableFuture.completedFuture(null);
    }

    public static <T> CompletableFuture<T> completed(T result) {
        return CompletableFuture.completedFuture(result);
    }

    public static Throwable unwrapped(Throwable t) {
        if (t instanceof CompletionException || t instanceof ExecutionException) {
            // unwrap exception of CompletableFuture
            return t.getCause();
        }
        return t;
    }

    public static Throwable getCause(CompletableFuture<?> future) {
        if (!future.isCompletedExceptionally()) {
            return null;
        }

        try {
            future.getNow(null);
        } catch (Throwable ex) {
            return unwrapped(ex);
        }

        return null;
    }
}
