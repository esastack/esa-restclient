package esa.restclient.core.codec;

import esa.commons.http.HttpHeaders;
import esa.commons.io.IOUtils;
import esa.restclient.core.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringBodyReader implements BodyReader<String> {

    @Override
    public boolean canRead(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders) {
        return String.class.equals(type);
    }

    @Override
    public String read(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders, InputStream bodyStream) throws IOException {
        Charset charset = null;
        if (mediaType != null) {
            charset = mediaType.charset();
        }

        if (charset == null) {
            return IOUtils.toString(bodyStream, StandardCharsets.UTF_8);
        } else {
            return IOUtils.toString(bodyStream, charset);
        }
    }
}
