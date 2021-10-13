package esa.restclient.codec;

import esa.commons.annotation.Internal;
import esa.commons.http.HttpHeaders;
import esa.restclient.ResponseBodyContent;
import io.esastack.commons.net.http.MediaType;

import java.lang.reflect.Type;

/**
 * <code>Decoder</code> is designed for the conversion of a {@link ResponseBodyContent} to a Java type.And it
 * is dynamically bound to the response through the {@link DecoderSelector}.
 * <p>
 * If you want to implement a <code>Decoder</code>, please implement the {@link ByteDecoder} interface
 * inherited from the <code>Decoder</code> instead of directly using the internal interface of the
 * <code>Decoder</code>
 *
 * @see ResponseBodyContent
 * @see ByteDecoder
 */
@Internal
public interface Decoder {

    /**
     * Decode the {@link ResponseBodyContent} into a Java object
     *
     * @param mediaType the media type of the HTTP response
     * @param headers   the headers of the HTTP response
     * @param content   the {@link ResponseBodyContent} of the HTTP response
     * @param type      the type that is to be decode from the content.
     * @param <T>       generic type
     * @return decoded value
     * @throws Exception error
     */
    <T> T decode(MediaType mediaType, HttpHeaders headers, ResponseBodyContent<?> content, Type type) throws Exception;
}
