package esa.restclient.codec;

import esa.commons.annotation.Internal;
import esa.commons.http.HttpHeaders;
import esa.httpclient.core.util.Ordered;
import esa.restclient.ContentType;
import esa.restclient.MediaType;
import esa.restclient.ResponseBodyContent;
import esa.restclient.RestRequest;

import java.lang.reflect.Type;

/**
 * The <code>DecoderSelector</code> is designed to dynamically find the appropriate {@link Decoder} for the response.
 * <p>
 * If you want to implement a <code>DecoderSelector</code>, please implement the {@link ByteDecoderSelector}
 * interface inherited from the <code>DecoderSelector</code> instead of directly using the internal interface of
 * the <code>DecoderSelector</code>
 *
 * @see Decoder
 * @see ByteDecoderSelector
 */
@Internal
public interface DecoderSelector extends Ordered {
    Decoder select(RestRequest request, ContentType[] acceptTypes, MediaType responseMediaType,
                   HttpHeaders responseHeaders, ResponseBodyContent<?> data, Type type);

    @Override
    default int getOrder() {
        return MIDDLE_PRECEDENCE;
    }
}
