package io.esastack.restclient.codec.impl;

import esa.commons.Checks;
import esa.commons.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.AcceptType;
import io.esastack.restclient.ResponseBodyContent;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.codec.DecodeAdvice;
import io.esastack.restclient.codec.DecodeContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.lang.reflect.Type;

public class DecodeContextImpl implements DecodeContext {

    private final RestRequest request;
    private final RestResponse response;
    private final DecodeAdvice[] advices;
    private int adviceIndex = 0;
    private final Type type;
    private MediaType mediaType;
    private ResponseBodyContent<?> content;

    public DecodeContextImpl(RestRequest request,
                             RestResponse response,
                             RestClientOptions clientOptions,
                             Type type,
                             MediaType mediaType,
                             ByteBuf byteBuf) {

        this.request = request;
        this.response = response;
        this.advices = clientOptions.unmodifiableDecodeAdvices();
        this.type = type;
        this.mediaType = mediaType;
        this.content = ResponseBodyContent.of(ByteBufUtil.getBytes(byteBuf));
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
            final AcceptType[] acceptTypes = request.acceptTypes();
            final HttpHeaders headers = response.headers();

            Checks.checkNotNull(acceptTypes, "acceptTypes");
            for (AcceptType acceptType : acceptTypes) {
                MediaType acceptMediaType = acceptType.mediaType();
                if (acceptMediaType == AcceptType.EMPTY_MEDIA_TYPE) {
                    return acceptType.decoder().decode(mediaType, headers, content, type);
                }

                if (acceptMediaType.isCompatibleWith(mediaType)) {
                    return acceptType.decoder().decode(mediaType, headers, content, type);
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
