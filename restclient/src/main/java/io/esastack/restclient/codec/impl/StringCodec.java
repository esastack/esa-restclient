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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringCodec implements Encoder, Decoder {

    @SuppressWarnings("unchecked")
    @Override
    public <T> CodecResult<T> decode(MediaType mediaType, HttpHeaders headers,
                                     ResponseBody responseBody, Class<T> type, Type genericType) {
        if (String.class.isAssignableFrom(type) && responseBody.isBytes()) {
            Charset charset = null;
            if (mediaType != null) {
                charset = mediaType.charset();
            }
            if (charset == null) {
                return CodecResult.success((T) new String(responseBody.getBytes(), StandardCharsets.UTF_8));
            } else {
                return CodecResult.success((T) new String(responseBody.getBytes(), charset));
            }
        }
        return CodecResult.fail();
    }

    @Override
    public CodecResult<RequestBody> encode(MediaType mediaType, HttpHeaders headers,
                                              Object entity, Class<?> type, Type genericType) {
        if (String.class.isAssignableFrom(type)) {
            Charset charset = null;
            if (mediaType != null) {
                charset = mediaType.charset();
            }
            if (charset == null) {
                return CodecResult.success(RequestBody.of(((String) entity).getBytes(StandardCharsets.UTF_8)));
            } else {
                return CodecResult.success(RequestBody.of(((String) entity).getBytes(mediaType.charset())));
            }
        }
        return CodecResult.fail();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWER_PRECEDENCE;
    }
}
