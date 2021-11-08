package io.esastack.restclient.codec.impl;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpclient.core.util.Ordered;
import io.esastack.restclient.codec.ByteCodec;
import io.esastack.restclient.codec.CodecResult;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringCodec implements ByteCodec {

    @SuppressWarnings("unchecked")
    @Override
    public <T> CodecResult<T> doDecode(MediaType mediaType, HttpHeaders headers,
                                       byte[] content, Class<T> type, Type genericType) {
        if (String.class.isAssignableFrom(type)) {
            Charset charset = null;
            if (mediaType != null) {
                charset = mediaType.charset();
            }
            if (charset == null) {
                return CodecResult.success((T) new String(content, StandardCharsets.UTF_8));
            } else {
                return CodecResult.success((T) new String(content, charset));
            }
        }
        return CodecResult.fail();
    }

    @Override
    public CodecResult<byte[]> doEncode(MediaType mediaType, HttpHeaders headers,
                                        Object entity, Class<?> type, Type genericType) {
        if (String.class.isAssignableFrom(type)) {
            Charset charset = null;
            if (mediaType != null) {
                charset = mediaType.charset();
            }
            if (charset == null) {
                return CodecResult.success(((String) entity).getBytes(StandardCharsets.UTF_8));
            } else {
                return CodecResult.success(((String) entity).getBytes(mediaType.charset()));
            }
        }
        return CodecResult.fail();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWER_PRECEDENCE;
    }
}
