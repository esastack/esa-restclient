package io.esastack.restclient.codec.impl;

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.httpclient.core.MultipartBody;
import io.esastack.restclient.codec.CodecResult;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestBody;

import java.lang.reflect.Type;

public class FormURLEncodedEncoder implements Encoder {

    @Override
    public CodecResult<RequestBody> encode(MediaType mediaType, HttpHeaders headers,
                                              Object entity, Class<?> type, Type genericType) {

        if (mediaType != null && MediaTypeUtil.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)
                && MultipartBody.class.isAssignableFrom(type)) {
            return CodecResult.success(RequestBody.of((MultipartBody) entity));
        }

        return CodecResult.fail();
    }
}
