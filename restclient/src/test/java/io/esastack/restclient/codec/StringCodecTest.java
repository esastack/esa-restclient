package io.esastack.restclient.codec;

import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.ResponseBodyContent;
import io.esastack.restclient.codec.impl.StringCodec;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.BDDAssertions.then;

class StringCodecTest {

    @Test
    void testEncode() throws Exception {

        StringCodec codec = new StringCodec();

        then(codec.encode(MediaTypeUtil.of("text", "plain", StandardCharsets.UTF_16),
                null, null).content()).isEqualTo(null);

        String content = "content";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_16);
        then(codec.encode(MediaTypeUtil.of("text", "plain", StandardCharsets.UTF_16),
                null, content).content()).isEqualTo(bytes);

        bytes = content.getBytes(StandardCharsets.UTF_8);
        then(codec.encode(MediaTypeUtil.of("text", "plain"),
                null, content).content()).isEqualTo(bytes);

    }

    @Test
    void testDecode() throws Exception {
        StringCodec codec = new StringCodec();

        then((String) codec.decode(MediaTypeUtil.of("text", "plain", StandardCharsets.UTF_16),
                null, ResponseBodyContent.of(null), String.class)).isEqualTo(null);

        String content = "content";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_16);
        then((String) codec.decode(MediaTypeUtil.of("text", "plain", StandardCharsets.UTF_16),
                null, ResponseBodyContent.of(bytes), String.class)).isEqualTo(content);

        bytes = content.getBytes(StandardCharsets.UTF_8);
        then((String) codec.decode(MediaTypeUtil.of("text", "plain"),
                null, ResponseBodyContent.of(bytes), String.class)).isEqualTo(content);
    }
}
