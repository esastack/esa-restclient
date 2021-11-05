package io.esastack.restclient.codec.impl;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.util.Ordered;
import io.esastack.restclient.codec.CodecResult;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestBody;
import io.esastack.restclient.codec.ResponseBody;

import java.lang.reflect.Type;

public class ByteCodec implements Encoder, Decoder {

    @Override
    public CodecResult<RequestBody<?>> encode(MediaType mediaType, HttpHeaders headers,
                                              Object entity, Class<?> type, Type genericType) {
        if (type.isArray() && type.getComponentType().equals(byte.class)) {
            return CodecResult.success(RequestBody.of((byte[]) entity));
        }

        return CodecResult.fail();
    }

    @Override
    public <T> CodecResult<T> decode(MediaType mediaType, HttpHeaders headers,
                                     ResponseBody<?> responseBody, Class<T> type, Type genericType) {
        if (responseBody.isBytes() && type.isArray() && type.getComponentType().equals(byte.class)) {
            return CodecResult.success((T) responseBody.getBytes());
        }

        return CodecResult.fail();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWER_PRECEDENCE;
    }
}
