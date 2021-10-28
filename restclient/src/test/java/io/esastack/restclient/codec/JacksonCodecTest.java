package io.esastack.restclient.codec;

import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.ResponseBodyContent;
import io.esastack.restclient.codec.impl.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.BDDAssertions.then;

public class JacksonCodecTest {
    @Test
    void testEncode() throws Exception {
        JacksonCodec jacksonCodec = new JacksonCodec();
        then(jacksonCodec.encode(MediaTypeUtil.APPLICATION_JSON_UTF8, null, null).content())
                .isEqualTo("null".getBytes(StandardCharsets.UTF_8));

        Person person = new Person("LiMing", "boy");
        then(jacksonCodec.encode(null, null, person).content())
                .isEqualTo(JacksonCodec.getDefaultMapper().writeValueAsBytes(person));
    }

    @Test
    void testDecode() throws Exception {
        JacksonCodec jacksonCodec = new JacksonCodec();
        then((Object) jacksonCodec.decode(null, null, ResponseBodyContent.of(null), null))
                .isEqualTo(null);

        Person person = new Person("LiMing", "boy");
        byte[] bytes = JacksonCodec.getDefaultMapper().writeValueAsBytes(person);
        then((Object) jacksonCodec.decode(null, null, ResponseBodyContent.of(bytes), Person.class))
                .isEqualTo(person);
    }
}
