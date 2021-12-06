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

import esa.commons.NetworkUtils;
import io.esastack.commons.net.http.HttpStatus;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.MediaType;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class MockServerUtil {

    public static int PORT = NetworkUtils.selectRandomPort();
    private static volatile ClientAndServer mockServer;

    public static ClientAndServer startMockServer(byte[] requestBody, byte[] responseBody,
                                                  MediaType responseMediaType, String path) {
        mockServer = getMockServer();
        HttpRequest request = request().withMethod("POST").withPath(path);
        if (requestBody != null) {
            request = request.withBody(requestBody);
        }
        mockServer.when(request).respond(
                response().withStatusCode(HttpStatus.OK.code())
                        .withContentType(responseMediaType)
                        .withBody(responseBody)
        );
        return mockServer;
    }

    public static ClientAndServer getMockServer() {
        if (mockServer == null) {
            synchronized (MockServerUtil.class) {
                if (mockServer == null) {
                    mockServer = startClientAndServer(PORT);
                }
            }
        }
        return mockServer;
    }
}
