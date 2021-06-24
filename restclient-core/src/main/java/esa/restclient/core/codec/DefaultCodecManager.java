package esa.restclient.core.codec;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.OrderedComparator;
import esa.restclient.core.MediaType;
import esa.restclient.core.exception.CodecException;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public class DefaultCodecManager implements CodecManager {

    private final List<Decoder> orderedDecoders = new LinkedList<>();
    private final List<Encoder> orderedEncoders = new LinkedList<>();

    public DefaultCodecManager(List<Decoder> decoders, List<Encoder> encoders) {
        Checks.checkNotNull(decoders, "Decoders must be not null");
        Checks.checkNotNull(encoders, "Encoders must be not null");
        orderedDecoders.addAll(decoders);
        OrderedComparator.sort(orderedDecoders);
        orderedEncoders.addAll(encoders);
        OrderedComparator.sort(orderedEncoders);
    }

    @Override
    public Object decode(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders, InputStream entityStream) {
        Decoder decoder = lookForDecoder(type, genericType, mediaType, httpHeaders);
        if (decoder == null) {
            throw new CodecException("No decoder!Type:" + type.getCanonicalName() +
                    ",genericType:" + genericType +
                    ",mediaType:" + mediaType +
                    ",httpHeaders:" + httpHeaders.toString());
        }
        return decoder.decode(type, genericType, mediaType, httpHeaders, entityStream);
    }


    @Override
    public InputStream encode(Object entity, Type genericType, MediaType mediaType, HttpHeaders httpHeaders) {
        Class type = entity.getClass();
        Encoder encoder = lookForEncode(entity.getClass(), genericType, mediaType, httpHeaders);
        if (encoder == null) {
            throw new CodecException("No Encoder!Type:" + type.getCanonicalName() +
                    ",genericType:" + genericType +
                    ",mediaType:" + mediaType +
                    ",httpHeaders:" + httpHeaders.toString());
        }
        return encoder.encode(entity, genericType, mediaType, httpHeaders);
    }

    private Decoder lookForDecoder(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders) {
        for (Decoder decoder : orderedDecoders) {
            if (decoder.canDecode(type, genericType, mediaType, httpHeaders)) {
                return decoder;
            }
        }
        return null;
    }

    private Encoder lookForEncode(Class type, Type genericType, MediaType mediaType, HttpHeaders httpHeaders) {
        for (Encoder encoder : orderedEncoders) {
            if (encoder.canEncode(type, genericType, mediaType, httpHeaders)) {
                return encoder;
            }
        }
        return null;
    }
}
