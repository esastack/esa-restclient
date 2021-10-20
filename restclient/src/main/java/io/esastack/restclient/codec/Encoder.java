package io.esastack.restclient.codec;

import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.ContentType;
import io.esastack.restclient.RequestBodyContent;

/**
 * <code>Encoder</code> is designed for the conversion of a Java type to a {@link RequestBodyContent}.And it
 * is bound by request through {@link ContentType}.
 *
 * @see RequestBodyContent
 * @see ContentType
 */
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
