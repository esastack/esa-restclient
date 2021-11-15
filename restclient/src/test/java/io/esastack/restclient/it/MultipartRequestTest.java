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
package io.esastack.restclient.it;

import io.esastack.restclient.RestClient;
import io.esastack.restclient.RestResponseBase;
import org.junit.jupiter.api.Test;
import org.mockserver.model.MediaType;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.BDDAssertions.then;

class MultipartRequestTest {

    @Test
    void testMultipartRequest() throws Exception {
        String path = "/multipart";
        String responseContentString = "hello";
        MockServerUtil.startMockServer(
                null,
                responseContentString.getBytes(StandardCharsets.UTF_8),
                MediaType.TEXT_PLAIN, path);
        RestResponseBase response = RestClient.ofDefault().post("http://localhost:" + MockServerUtil.PORT + path)
                .multipart()
                .attr("aaa", "bbb")
                .attr("ccc", "ddd")
                .execute()
                .toCompletableFuture().get();
        then(response.bodyToEntity(String.class)).isEqualTo(responseContentString);
    }
}
