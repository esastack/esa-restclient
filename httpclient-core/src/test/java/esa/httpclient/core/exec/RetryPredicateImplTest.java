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
import org.junit.jupiter.api.Test;

import java.net.ConnectException;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;

class RetryPredicateImplTest {

    @Test
    void testCanRetry() {
        final RetryPredicate predicate = RetryPredicateImpl.DEFAULT;
        then(predicate.canRetry(mock(HttpRequest.class), mock(HttpResponse.class), mock(Context.class), null))
                .isFalse();
        then(predicate.canRetry(mock(HttpRequest.class), mock(HttpResponse.class), mock(Context.class),
                new ConnectException())).isTrue();
        then(predicate.canRetry(mock(HttpRequest.class), mock(HttpResponse.class), mock(Context.class),
                new RuntimeException())).isFalse();
    }

}
