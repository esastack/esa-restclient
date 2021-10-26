package io.esastack.restclient.exec;

import io.esastack.httpclient.core.MultipartBody;
import io.esastack.httpclient.core.MultipartBodyImpl;
import io.esastack.restclient.codec.impl.MultipartToMultipartEncoder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class MultipartToMultipartEncoderTest {

    @Test
    void testEncode() {
        MultipartToMultipartEncoder encoder = new MultipartToMultipartEncoder();
        then(encoder.encode(null, null, null).content())
                .isEqualTo(null);

        MultipartBody multipartBody = new MultipartBodyImpl();
        then(encoder.encode(null, null, multipartBody).content())
                .isEqualTo(multipartBody);
    }

}
