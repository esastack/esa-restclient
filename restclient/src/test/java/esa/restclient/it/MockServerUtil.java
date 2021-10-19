package esa.restclient.it;

import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MockServerUtil {

    public static ClientAndServer startMockServer(byte[] requestBody, byte[] responseBody,
                                                  MediaType responseMediaType, int port, String path) {
        ClientAndServer mockServer = startClientAndServer(port);
        mockServer.when(
                request().withMethod("POST")
                        .withPath(path)
                        .withBody(requestBody)
        ).respond(
                response().withStatusCode(200)
                        .withContentType(responseMediaType)
                        .withBody(responseBody)
        );
        return mockServer;
    }
}
