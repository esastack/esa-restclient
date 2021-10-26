package io.esastack.restclient.codec;

import io.esastack.restclient.codec.impl.FileToFileEncoder;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.BDDAssertions.then;

class FileToFileEncoderTest {

    @Test
    void testEncode() {
        FileToFileEncoder fileEncoder = new FileToFileEncoder();
        then(fileEncoder.encode(null, null, null).content())
                .isEqualTo(null);

        File file = new File("test");
        then(fileEncoder.encode(null, null, file).content())
                .isEqualTo(file);
    }

}
