package esa.restclient;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;
import esa.httpclient.core.HttpResponse;
import esa.restclient.codec.DecodeAdvice;
import esa.restclient.codec.Decoder;
import esa.restclient.codec.DecoderSelector;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.lang.reflect.Type;

public class RestResponseBaseImpl implements RestResponseBase {

    private final RestRequest request;
    private final HttpResponse response;
    private final RestClientConfig clientConfig;

    public RestResponseBaseImpl(
            RestRequest request,
            HttpResponse response,
            RestClientConfig clientConfig) {
        Checks.checkNotNull(request, "Request must be not null!");
        Checks.checkNotNull(response, "Response must be not null!");
        Checks.checkNotNull(clientConfig, "ClientConfig must be not null!");
        this.request = request;
        this.response = response;
        this.clientConfig = clientConfig;
    }

    @Override
    public int status() {
        return response.status();
    }

    @Override
    public HttpHeaders trailers() {
        return response.trailers();
    }

    @Override
    public HttpVersion version() {
        return response.version();
    }

    @Override
    public HttpHeaders headers() {
        return response.headers();
    }

    @Override
    public <T> T bodyToEntity(Class<T> entityClass) throws Exception {
        return bodyToEntity((Type) entityClass);
    }

    @Override
    public <T> T bodyToEntity(Type type) throws Exception {
        final String mediaTypeValue = response.headers().get(HttpHeaderNames.CONTENT_TYPE);
        MediaType mediaType = null;
        if (StringUtils.isNotBlank(mediaTypeValue)) {
            mediaType = MediaType.parseMediaType(mediaTypeValue);
        }

        final ContentType[] acceptTypes = request.acceptTypes();
        final HttpHeaders headers = response.headers();

        final DecoderSelector[] decoderSelectors = clientConfig.unmodifiableDecoderSelectors();
        BodyContent<byte[]> content = BodyContent.of(response.body().getByteBuf().array());
        for (DecoderSelector decoderSelector : decoderSelectors) {
            Decoder decoder = decoderSelector.select(request, acceptTypes, mediaType, headers, content, type);
            if (decoder != null) {
                return decode(decoder, mediaType,
                        headers, type, content);
            }
        }
        throw new IllegalStateException("There is no suitable decoder for this response," +
                "Please set correct acceptType and rxSerializerSelector!" +
                "request.uri: " + request.uri() +
                ",response.status: " + response.status() +
                ",response.headers: " + headers);

    }

    @SuppressWarnings("unchecked")
    private <T> T decode(Decoder decoder, MediaType mediaType,
                         HttpHeaders headers, Type type, BodyContent<byte[]> content) throws Exception {
        for (DecodeAdvice decodeAdvice : clientConfig.unmodifiableDecodeAdvices()) {
            decodeAdvice.beforeDecode(request, this, content, type);
        }

        Object entity = decoder.decode(mediaType, headers, content, type);
        for (DecodeAdvice decodeAdvice : clientConfig.unmodifiableDecodeAdvices()) {
            entity = decodeAdvice.afterDecode(request, this, content, entity, type);
        }

        return (T) entity;
    }
}
