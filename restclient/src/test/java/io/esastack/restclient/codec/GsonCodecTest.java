package io.esastack.restclient.codec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.ResponseBodyContent;
import io.esastack.restclient.codec.impl.GsonCodec;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.BDDAssertions.then;

class GsonCodecTest {

    private final Gson gson = new GsonBuilder().create();

    @Test
    void testEncode() throws Exception {
        GsonCodec gsonCodec = new GsonCodec();

        then(gsonCodec.encode(MediaTypeUtil.APPLICATION_JSON_UTF8, null, null).content())
                .isEqualTo("null".getBytes(StandardCharsets.UTF_8));

        Person person = new Person("LiMing", "boy");
        then(gsonCodec.encode(null, null, person).content())
                .isEqualTo(gson.toJson(person).getBytes(StandardCharsets.UTF_8));

        then(gsonCodec.encode(MediaTypeUtil.of("application", "json", StandardCharsets.UTF_16),
                null, person).content())
                .isEqualTo(gson.toJson(person).getBytes(StandardCharsets.UTF_16));
    }

    @Test
    void testDecode() throws Exception {
        GsonCodec gsonCodec = new GsonCodec();
        then((Object) gsonCodec.decode(null, null, ResponseBodyContent.of(null), null))
                .isEqualTo(null);

        Person person = new Person("LiMing", "boy");
        byte[] bytes = gson.toJson(person).getBytes(StandardCharsets.UTF_8);
        then((Object) gsonCodec.decode(null, null, ResponseBodyContent.of(bytes), Person.class))
                .isEqualTo(person);

        bytes = gson.toJson(person).getBytes(StandardCharsets.UTF_16);
        then((Object) gsonCodec.decode(MediaTypeUtil.of("application", "json", StandardCharsets.UTF_16),
                null, ResponseBodyContent.of(bytes), Person.class))
                .isEqualTo(person);
    }
}
