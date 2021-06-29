package esa.restclient.codec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import esa.commons.http.HttpHeaders;
import esa.commons.io.IOUtils;
import esa.restclient.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class JsonBodyReader implements BodyReader {
    private static final Gson gson = new GsonBuilder().create();

    @Override
    public boolean canRead(Class rawType, Type type, MediaType mediaType, HttpHeaders httpHeaders) {
        if (mediaType == null) {
            return false;
        }
        return mediaType.type().equalsIgnoreCase(MediaType.APPLICATION_JSON.type()) &&
                mediaType.subtype().equalsIgnoreCase(MediaType.APPLICATION_JSON.subtype());
    }

    @Override
    public Object read(Class rawType, Type type, MediaType mediaType, HttpHeaders httpHeaders, InputStream bodyStream) throws IOException {
        Charset charset = null;
        if (mediaType != null) {
            charset = mediaType.charset();
        }

        if (charset == null) {
            return gson.fromJson(IOUtils.toString(bodyStream, StandardCharsets.UTF_8), type);
        } else {
            return gson.fromJson(IOUtils.toString(bodyStream, charset), type);
        }
    }

}
