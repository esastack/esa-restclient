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
package esa.httpclient.core.exec;

import esa.httpclient.core.Context;
import esa.httpclient.core.HttpRequest;
import esa.httpclient.core.HttpResponse;
import esa.httpclient.core.util.Futures;

import java.net.ConnectException;

public class RetryPredicateImpl implements RetryPredicate {

    /**
     * Default to retry only when {@link ConnectException} was caught.
     */
    public static final RetryPredicateImpl DEFAULT = new RetryPredicateImpl();

    @Override
    public boolean canRetry(HttpRequest request,
                            HttpResponse response,
                            Context ctx,
                            Throwable cause) {
        if (cause == null) {
            return false;
        }

        final Throwable unwrapped = Futures.unwrapped(cause);
        if (unwrapped instanceof ConnectException) {
            return true;
        }

        return canRetry0(request, response, ctx, cause);
    }

    /**
     * Whether to retry or not.
     *
     * @param request       request
     * @param response      response
     * @param ctx           ctx
     * @param cause         cause
     * @return true or false
     */
    protected boolean canRetry0(HttpRequest request,
                                HttpResponse response,
                                Context ctx,
                                Throwable cause) {
        return false;
    }

}
