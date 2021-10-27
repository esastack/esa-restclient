package io.esastack.restclient.codec;

import esa.commons.netty.http.Http1HeadersImpl;
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

        DataInfo.Student student = DataInfo.Student.newBuilder()
                .setName("LiMing").setAge(100).setAddress("China").build();
        byte[] bytes = student.toByteArray();
        then(protoBufCodec.encode(null, new Http1HeadersImpl(), student).content())
                .isEqualTo(bytes);
    }

    @Test
    void testDecode() throws Exception {
        ProtoBufCodec protoBufCodec = new ProtoBufCodec();
        then((Object) protoBufCodec.decode(null, null, ResponseBodyContent.of(null), null))
                .isEqualTo(null);

        DataInfo.Student student = DataInfo.Student.newBuilder()
                .setName("LiMing").setAge(100).setAddress("China").build();
        byte[] bytes = student.toByteArray();
        then((Object) protoBufCodec.decode(null, new Http1HeadersImpl(), ResponseBodyContent.of(bytes), DataInfo.Student.class))
                .isEqualTo(student);
    }
}
