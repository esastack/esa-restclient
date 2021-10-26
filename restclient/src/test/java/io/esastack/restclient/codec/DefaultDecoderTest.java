package io.esastack.restclient.codec;

import io.esastack.restclient.ResponseBodyContent;
import io.esastack.restclient.codec.impl.DefaultDecoder;
import io.esastack.restclient.codec.impl.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultDecoderTest {

    @Test
    void testDecodeNull() throws Exception {
        DefaultDecoder decoder = new DefaultDecoder();
        assertThrows(NullPointerException.class, () ->
                decoder.decode(null, null, null, null));

        then((Object) decoder.decode(null, null, ResponseBodyContent.of("hello".getBytes()), null))
                .isEqualTo(null);

        then((Object) decoder.decode(null, null, ResponseBodyContent.of(null), String.class))
                .isEqualTo(null);
    }

    @Test
    void testDecodeString() throws Exception {
        DefaultDecoder decoder = new DefaultDecoder();
        then((Object) decoder.decode(null, null, ResponseBodyContent.of("hello".getBytes()), String.class))
                .isEqualTo("hello");
    }

    @Test
    void testDecodeBytes() throws Exception {
        byte[] bytes = "hello".getBytes();
        DefaultDecoder decoder = new DefaultDecoder();
        then((Object) decoder.decode(null, null, ResponseBodyContent.of(bytes), byte[].class))
                .isEqualTo(bytes);
    }

    @Test
    void testDecodeJson() throws Exception {
        Person person = new Person("LiMing", "boy");
        byte[] bytes = new JacksonCodec().doEncode(null, null, person);
        DefaultDecoder decoder = new DefaultDecoder();
        then((Object) decoder.decode(null, null, ResponseBodyContent.of(bytes), Person.class))
                .isEqualTo(person);
    }

}
