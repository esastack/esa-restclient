package io.esastack.restclient.codec;

import io.esastack.httpclient.core.MultipartBody;
import io.esastack.httpclient.core.MultipartBodyImpl;
import io.esastack.restclient.codec.impl.FormURLEncodedEncoder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class FormURLEncodedEncoderTest {

    @Test
    void testEncode() {
        FormURLEncodedEncoder encoder = new FormURLEncodedEncoder();
        then(encoder.encode(null, null, null).content())
                .isEqualTo(null);

        MultipartBody multipartBody = new MultipartBodyImpl();
        then(encoder.encode(null, null, multipartBody).content().multipartEncode())
                .isEqualTo(false);
    }

}
