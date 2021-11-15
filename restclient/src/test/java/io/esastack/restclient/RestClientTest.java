/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restclient;

import io.esastack.commons.net.http.HttpMethod;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class RestClientTest {

    @Test
    void testMethod() {
        RestClient client = RestClient.ofDefault();
        String url = "http://localhost:8080/test";
        RestRequest request = client.get(url);
        then(request.method()).isEqualTo(HttpMethod.GET);

        request = client.post(url);
        then(request.method()).isEqualTo(HttpMethod.POST);

        request = client.head(url);
        then(request.method()).isEqualTo(HttpMethod.HEAD);

        request = client.delete(url);
        then(request.method()).isEqualTo(HttpMethod.DELETE);

        request = client.options(url);
        then(request.method()).isEqualTo(HttpMethod.OPTIONS);

        request = client.put(url);
        then(request.method()).isEqualTo(HttpMethod.PUT);
    }

}
