package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.commons.io.IOUtils;
import esa.restclient.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringBodyReader implements BodyReader<String> {

    @Override
    public boolean canRead(Class rawType, Type type, MediaType mediaType, HttpHeaders httpHeaders) {
        return String.class.equals(rawType);
    }

    @Override
    public String read(Class rawType, Type type, MediaType mediaType, HttpHeaders httpHeaders, InputStream bodyStream) throws IOException {
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
