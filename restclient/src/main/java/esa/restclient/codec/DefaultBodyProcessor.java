package esa.restclient.codec;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.OrderedComparator;
import esa.restclient.MediaType;
import esa.restclient.exception.BodyProcessException;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public class DefaultBodyProcessor implements BodyProcessor {

    private final List<BodyReader> orderedBodyReaders = new LinkedList<>();
    private final List<BodyWriter> orderedBodyWriters = new LinkedList<>();

    public DefaultBodyProcessor(List<BodyReader> bodyReaders, List<BodyWriter> bodyWriters) {
        Checks.checkNotNull(bodyReaders, "Decoders must be not null");
        Checks.checkNotNull(bodyWriters, "Encoders must be not null");
        orderedBodyReaders.addAll(bodyReaders);
        OrderedComparator.sort(orderedBodyReaders);
        orderedBodyWriters.addAll(bodyWriters);
        OrderedComparator.sort(orderedBodyWriters);
    }

    @Override
    public Object read(Class rawType, Type type, MediaType mediaType, HttpHeaders httpHeaders, InputStream bodyStream) {
        BodyReader bodyReader = lookForReader(rawType, type, mediaType, httpHeaders);
        if (bodyReader == null) {
            throw new BodyProcessException("No decoder!Type:" + rawType.getCanonicalName() +
                    ",type:" + type +
                    ",mediaType:" + mediaType +
                    ",httpHeaders:" + httpHeaders.toString());
        }
        try {
            return bodyReader.read(rawType, type, mediaType, httpHeaders, bodyStream);
        } catch (Throwable e) {
            throw new BodyProcessException("Read error!cause:" + e.getMessage(), e);
        }
    }


    @Override
    public void write(Object entity, Type type, MediaType mediaType, HttpHeaders httpHeaders, OutputStream bodyStream) {
        Class rawType = entity.getClass();
        if (type == null) {
            type = rawType;
        }
        BodyWriter bodyWriter = lookForWriter(entity.getClass(), type, mediaType, httpHeaders);
        if (bodyWriter == null) {
            throw new BodyProcessException("No Encoder!Type:" + rawType.getCanonicalName() +
                    ",type:" + type +
                    ",mediaType:" + mediaType +
                    ",httpHeaders:" + httpHeaders.toString());
        }
        try {
            bodyWriter.write(entity, type, mediaType, httpHeaders, bodyStream);
        } catch (Throwable e) {
            throw new BodyProcessException("Write error!cause:" + e.getMessage(), e);
        }
    }

    private BodyReader lookForReader(Class rawType, Type type, MediaType mediaType, HttpHeaders httpHeaders) {
        for (BodyReader bodyReader : orderedBodyReaders) {
            if (bodyReader.canRead(rawType, type, mediaType, httpHeaders)) {
                return bodyReader;
            }
        }
        return null;
    }

    private BodyWriter lookForWriter(Class rawType, Type type, MediaType mediaType, HttpHeaders httpHeaders) {
        for (BodyWriter bodyWriter : orderedBodyWriters) {
            if (bodyWriter.canWrite(rawType, type, mediaType, httpHeaders)) {
                return bodyWriter;
            }
        }
        return null;
    }
}
