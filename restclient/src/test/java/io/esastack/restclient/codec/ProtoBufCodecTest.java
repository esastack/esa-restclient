package io.esastack.restclient.codec;

import io.esastack.restclient.ContentType;
import io.esastack.restclient.ResponseBodyContent;
import io.esastack.restclient.codec.impl.ProtoBufCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class ProtoBufCodecTest {

    @Test
    void testEncode() throws Exception {
        ProtoBufCodec protoBufCodec = new ProtoBufCodec();
        then(protoBufCodec.encode(ContentType.PROTOBUF.mediaType(), null, null).content())
                .isEqualTo(null);
    }

    @Test
    void testDecode() throws Exception {
        ProtoBufCodec protoBufCodec = new ProtoBufCodec();
        then((Object) protoBufCodec.decode(null, null, ResponseBodyContent.of(null), null))
                .isEqualTo(null);
    }
}
