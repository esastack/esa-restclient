package io.esastack.restclient.codec;

import io.esastack.restclient.codec.impl.FileEncoder;
import org.junit.jupiter.api.Test;

import java.io.File;

class FileToFileEncoderTest {

    @Test
    void testEncode() {
        FileEncoder fileEncoder = new FileEncoder();
        then(fileEncoder.encode(null, null, null).content())
                .isEqualTo(null);

        File file = new File("test");
        then(fileEncoder.encode(null, null, file).content())
                .isEqualTo(file);
    }

}
