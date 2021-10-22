package io.esastack.restclient.it;

import io.esastack.restclient.RestClient;
import io.esastack.restclient.RestResponseBase;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.BDDAssertions.then;

class FileRequestTest {

    @Test
    void testFileRequest() throws Exception {
        String path = "/file";
        String contentString = "hello";
        byte[] content = contentString.getBytes(StandardCharsets.UTF_8);
        File file = createFile(content);
        ClientAndServer mockServer = MockServerUtil.startMockServer(content, content, MediaType.TEXT_PLAIN, path);
        RestResponseBase response =
                RestClient.ofDefault().post("http://localhost:" + MockServerUtil.PORT + path)
                        .entity(file)
                        .execute()
                        .toCompletableFuture().get();
        then(response.bodyToEntity(String.class)).isEqualTo(contentString);
        if (!file.delete()) {
            throw new IllegalStateException("File delete error!");
        }
    }

    private File createFile(byte[] content) throws IOException {
        File file = new File("temTestFile");

        if (file.exists()) {
            file.delete();
        }
        if (!file.createNewFile()) {
            throw new IllegalStateException("File create error!");
        }
        BufferedOutputStream fileStream = new BufferedOutputStream(new FileOutputStream(file));
        fileStream.write(content);
        fileStream.flush();
        fileStream.close();
        return file;
    }
}