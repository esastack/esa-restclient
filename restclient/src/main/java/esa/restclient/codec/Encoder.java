package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.MediaType;
import esa.restclient.RequestBodyContent;

/**
 * <code>Encoder</code> is designed for the conversion of a Java type to a {@link RequestBodyContent}.And it
 * is bound by request through {@link esa.restclient.ContentType}.
 *
 * @see RequestBodyContent
 * @see esa.restclient.ContentType
 */
// TODO: Encoder or serializer?
public interface Encoder {

    /**
     * encode the object to {@link RequestBodyContent}
     * @param mediaType the media type of the HTTP request
     * @param headers the headers of the HTTP request
     * @param entity the entity need to be encode
     * @return {@link RequestBodyContent}
     * @throws Exception error
     */
    RequestBodyContent<?> encode(MediaType mediaType, HttpHeaders headers, Object entity) throws Exception;
}
