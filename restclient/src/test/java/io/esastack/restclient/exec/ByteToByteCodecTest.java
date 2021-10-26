package io.esastack.restclient.exec;

import io.esastack.restclient.ResponseBodyContent;
import io.esastack.restclient.codec.impl.ByteToByteCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ByteToByteCodecTest {

    @Test
    void testEncode() throws Exception {
        ByteToByteCodec byteCodec = new ByteToByteCodec();
        then(byteCodec.encode(null, null, null).content()).isEqualTo(null);

        assertThrows(ClassCastException.class, () ->
                byteCodec.encode(null, null, "Hello"));

        byte[] bytes = "Hello".getBytes();
        then(byteCodec.encode(null, null, bytes).content()).isEqualTo(bytes);
    }

    @Test
    void testDecode() throws Exception {
        ByteToByteCodec byteCodec = new ByteToByteCodec();
        then((Object) byteCodec.decode(null, null, ResponseBodyContent.of(null), byte[].class))
                .isEqualTo(null);

        byte[] bytes = "Hello".getBytes();
        then((Object) byteCodec.decode(null, null, ResponseBodyContent.of(bytes), byte[].class))
                .isEqualTo(bytes);
    }

}
