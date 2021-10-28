package io.esastack.restclient;

import io.esastack.httpclient.core.MultipartBody;
import io.esastack.httpclient.core.MultipartBodyImpl;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.BDDAssertions.then;

class BodyContentTest {

    @Test
    void testRequestBodyContent() {
        BodyContent<File> fileContent = RequestBodyContent.of((File) null);
        then(fileContent.content()).isEqualTo(null);
        File file = new File("test");
        fileContent = RequestBodyContent.of(file);
        then(fileContent.content()).isEqualTo(file);

        BodyContent<byte[]> bytesContent = RequestBodyContent.of((byte[]) null);
        then(bytesContent.content()).isEqualTo(null);
        byte[] bytes = "test".getBytes();
        bytesContent = RequestBodyContent.of(bytes);
        then(bytesContent.content()).isEqualTo(bytes);

        BodyContent<MultipartBody> multipartContent = RequestBodyContent.of((MultipartBody) null);
        then(multipartContent.content()).isEqualTo(null);
        MultipartBody multipart = new MultipartBodyImpl();
        multipartContent = RequestBodyContent.of(multipart);
        then(multipartContent.content()).isEqualTo(multipart);
    }

    @Test
    void testResponseBodyContent() {
        BodyContent<byte[]> bytesContent = ResponseBodyContent.of(null);
        then(bytesContent.content()).isEqualTo(null);
        byte[] bytes = "test".getBytes();
        bytesContent = ResponseBodyContent.of(bytes);
        then(bytesContent.content()).isEqualTo(bytes);
    }
}
