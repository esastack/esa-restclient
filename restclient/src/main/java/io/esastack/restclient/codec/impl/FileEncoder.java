package io.esastack.restclient.codec.impl;

import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.codec.CodecResult;
import io.esastack.restclient.codec.Encoder;
import io.esastack.restclient.codec.RequestBody;

import java.io.File;
import java.lang.reflect.Type;

public class FileEncoder implements Encoder {

    @Override
    public CodecResult<RequestBody<?>> encode(MediaType mediaType, HttpHeaders headers,
                                              Object entity, Class<?> type, Type genericType) {

        if (File.class.isAssignableFrom(type)) {
            return CodecResult.success(RequestBody.of((File) entity));
        }

        return CodecResult.fail();
    }
}
