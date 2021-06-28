package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.commons.io.IOUtils;
import esa.restclient.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringBodyWriter implements BodyWriter<String> {
    @Override
    public boolean canWrite(Class rawType, Type type, MediaType mediaType, HttpHeaders httpHeaders) {
        return mediaType == null || MediaType.TEXT_HTML.type().equalsIgnoreCase(mediaType.type());
    }

    @Override
    public void write(String entity,
                      Type type,
                      MediaType mediaType,
                      HttpHeaders httpHeaders,
                      OutputStream bodyStream) throws IOException {
        if (entity == null) {
            return;
        }
        Charset charset = null;
        if (mediaType != null) {
            charset = mediaType.charset();
        }

        if (charset == null) {
            IOUtils.write(entity.getBytes(StandardCharsets.UTF_8), bodyStream);
        } else {
            IOUtils.write(entity.getBytes(charset), bodyStream);
        }
    }
}
