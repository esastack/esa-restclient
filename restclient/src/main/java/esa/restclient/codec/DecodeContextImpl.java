package esa.restclient.codec;

import esa.commons.http.HttpHeaders;
import esa.restclient.*;

import java.lang.reflect.Type;

public class DecodeContextImpl implements DecodeContext {

    private final RestRequest request;
    private final RestResponse response;
    private final DecodeAdvice[] advices;
    private final DecoderSelector[] decoderSelectors;
    private int adviceIndex = 0;
    private final Type type;
    private MediaType mediaType;
    private ResponseBodyContent<?> content;

    public DecodeContextImpl(RestRequest request,
                             RestResponse response,
                             RestClientConfig clientConfig,
                             Type type,
                             MediaType mediaType,
                             ResponseBodyContent<?> content) {

        this.request = request;
        this.response = response;
        this.advices = clientConfig.unmodifiableDecodeAdvices();
        this.decoderSelectors = clientConfig.unmodifiableDecoderSelectors();
        this.type = type;
        this.mediaType = mediaType;
        this.content = content;
    }

    @Override
    public RestRequest request() {
        return request;
    }

    @Override
    public MediaType mediaType() {
        return mediaType;
    }

    @Override
    public void mediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public RestResponse response() {
        return response;
    }

    @Override
    public ResponseBodyContent<?> content() {
        return content;
    }

    @Override
    public void content(ResponseBodyContent<?> content) {
        this.content = content;
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public Object proceed() throws Exception {
        if (advices == null || adviceIndex >= advices.length) {
            final ContentType[] acceptTypes = request.acceptTypes();
            final HttpHeaders headers = response.headers();

            for (DecoderSelector decoderSelector : decoderSelectors) {
                Decoder decoder = decoderSelector.select(request, acceptTypes, mediaType, headers, content, type);
                if (decoder != null) {
                    return decoder.decode(mediaType, headers, content, type);
                }
            }

            throw new IllegalStateException("There is no suitable decoder for this response," +
                    "Please set correct acceptType and decoderSelector!" +
                    "request.uri: " + request.uri() +
                    ",response.status: " + response.status() +
                    ",response.headers: " + headers);
        }

        return advices[adviceIndex++].aroundDecode(this);
    }

}
