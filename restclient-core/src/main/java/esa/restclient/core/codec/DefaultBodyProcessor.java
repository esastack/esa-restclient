package esa.restclient.core.codec;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.OrderedComparator;
import esa.restclient.core.MediaType;
import esa.restclient.core.exception.CodecException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.rmi.server.ExportException;
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
    public Object read(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders, InputStream bodyStream) {
        BodyReader bodyReader = lookForReader(type, genericType, mediaType, httpHeaders);
        if (bodyReader == null) {
            throw new CodecException("No decoder!Type:" + type.getCanonicalName() +
                    ",genericType:" + genericType +
                    ",mediaType:" + mediaType +
                    ",httpHeaders:" + httpHeaders.toString());
        }
        try {
            return bodyReader.read(type, genericType, mediaType, httpHeaders, bodyStream);
        } catch (Throwable e) {
            throw new CodecException("Read error!cause:" + e.getMessage(), e);
        }
    }


    @Override
    public void write(Object entity, Type genericType, MediaType mediaType, HttpHeaders httpHeaders, OutputStream bodyStream) {
        Class type = entity.getClass();
        BodyWriter bodyWriter = lookForWriter(entity.getClass(), genericType, mediaType, httpHeaders);
        if (bodyWriter == null) {
            throw new CodecException("No Encoder!Type:" + type.getCanonicalName() +
                    ",genericType:" + genericType +
                    ",mediaType:" + mediaType +
                    ",httpHeaders:" + httpHeaders.toString());
        }
        try {
            bodyWriter.write(entity, genericType, mediaType, httpHeaders, bodyStream);
        } catch (Throwable e) {
            throw new CodecException("Write error!cause:" + e.getMessage(), e);
        }
    }

    private BodyReader lookForReader(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders) {
        for (BodyReader bodyReader : orderedBodyReaders) {
            if (bodyReader.canRead(type, genericType, mediaType, httpHeaders)) {
                return bodyReader;
            }
        }
        return null;
    }

    private BodyWriter lookForWriter(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders) {
        for (BodyWriter bodyWriter : orderedBodyWriters) {
            if (bodyWriter.canWrite(type, genericType, mediaType, httpHeaders)) {
                return bodyWriter;
            }
        }
        return null;
    }
}
