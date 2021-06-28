package esa.restclient.codec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import esa.commons.http.HttpHeaders;
import esa.commons.io.IOUtils;
import esa.restclient.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class JsonBodyWriter implements BodyWriter {

    private static final Gson gson = new GsonBuilder().create();

    @Override
    public boolean canWrite(Class rawType, Type type, MediaType mediaType, HttpHeaders httpHeaders) {
        if (mediaType == null) {
            return false;
        }
        return mediaType.type().equalsIgnoreCase(MediaType.APPLICATION_JSON.type()) &&
                mediaType.subtype().equalsIgnoreCase(MediaType.APPLICATION_JSON.subtype());
    }

    @Override
    public void write(Object entity,
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
            IOUtils.write(gson.toJson(entity).getBytes(StandardCharsets.UTF_8), bodyStream);
        } else {
            IOUtils.write(gson.toJson(entity).getBytes(charset), bodyStream);
        }
    }
}
