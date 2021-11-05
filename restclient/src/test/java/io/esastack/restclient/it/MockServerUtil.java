package io.esastack.restclient.it;

import esa.commons.NetworkUtils;
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
                response().withStatusCode(200)
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
