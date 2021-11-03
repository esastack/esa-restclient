package io.esastack.restclient.codec;

import io.esastack.httpclient.core.MultipartBody;
import io.esastack.httpclient.core.MultipartBodyImpl;
import io.esastack.restclient.codec.impl.MultipartEncoder;
import org.junit.jupiter.api.Test;

class MultipartToMultipartEncoderTest {

    @Test
    void testEncode() {
        MultipartEncoder encoder = new MultipartEncoder();
        then(encoder.encode(null, null, null).content())
                .isEqualTo(null);

        MultipartBody multipartBody = new MultipartBodyImpl();
        then(encoder.encode(null, null, multipartBody).content())
                .isEqualTo(multipartBody);
    }

}
