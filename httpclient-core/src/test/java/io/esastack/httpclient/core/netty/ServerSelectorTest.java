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
package io.esastack.httpclient.core.netty;

import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.HttpClient;
import io.esastack.httpclient.core.HttpRequest;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;

class ServerSelectorTest {

    @Test
    void testDefault() {
        final ServerSelector selector = ServerSelector.DEFAULT;
        final HttpClient client = HttpClient.ofDefault();

        final Context context = mock(Context.class);
        final HttpRequest request1 = client.get("http://127.0.0.1");

        then(selector.select(request1, context))
                .isEqualTo(InetSocketAddress.createUnresolved("127.0.0.1", 80));

        final HttpRequest request2 = client.get("https://127.0.0.1");
        then(selector.select(request2, context))
                .isEqualTo(InetSocketAddress.createUnresolved("127.0.0.1", 443));

        final HttpRequest request3 = client.get("https://127.0.0.1:8989");
        then(selector.select(request3, context))
                .isEqualTo(InetSocketAddress.createUnresolved("127.0.0.1", 8989));
    }
}
