package io.esastack.restclient.codec.impl;

import esa.commons.Checks;
import esa.commons.StringUtils;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestRequestBase;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.codec.CodecResult;
import io.esastack.restclient.codec.DecodeAdvice;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.ResponseContent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.lang.reflect.Type;

public final class DecodeContextImpl implements DecodeContext {

    private final RestRequest request;
    private final RestResponse response;
    private final DecodeAdvice[] advices;
    private final Decoder decoderOfRequest;
    private final Decoder[] decodersOfClient;

    private final Class<?> type;
    private final Type genericType;
    private int adviceIndex = 0;
    private MediaType contentType;
    private ResponseContent responseContent;

    public DecodeContextImpl(RestRequestBase request,
                             RestResponse response,
                             RestClientOptions clientOptions,
                             Class<?> type,
                             Type genericType,
                             ByteBuf byteBuf) {
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(response, "response");
        Checks.checkNotNull(clientOptions, "clientOptions");
        Checks.checkNotNull(type, "type");
        Checks.checkNotNull(byteBuf, "byteBuf");
        this.request = request;
        this.response = response;
        this.advices = clientOptions.unmodifiableDecodeAdvices();
        this.decoderOfRequest = request.decoder();
        this.decodersOfClient = clientOptions.unmodifiableDecoders();
        this.type = type;
        this.genericType = genericType;
        this.responseContent = ResponseContent.of(ByteBufUtil.getBytes(byteBuf));

        final String mediaTypeValue = response.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (StringUtils.isNotBlank(mediaTypeValue)) {
            this.contentType = MediaTypeUtil.parseMediaType(mediaTypeValue);
        }
    }

    @Override
    public RestRequest request() {
        return request;
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public void contentType(MediaType mediaType) {
        this.contentType = mediaType;
    }

    @Override
    public RestResponse response() {
        return response;
    }

    @Override
    public ResponseContent responseContent() {
        return responseContent;
    }

    @Override
    public void responseContent(ResponseContent responseContent) {
        this.responseContent = responseContent;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public Type genericType() {
        return genericType;
    }

    @Override
    public Object proceed() throws Exception {
        if (advices == null || adviceIndex >= advices.length) {
            if (decoderOfRequest != null) {
                return decodeByDecoderOfRequest();
            } else {
                return decodeByDecodersOfClient();
            }
        }

        return advices[adviceIndex++].aroundDecode(this);
    }

    private Object decodeByDecoderOfRequest() throws Exception {
        HttpHeaders headers = response.headers();
        CodecResult<?> codecResult = decoderOfRequest.decode(contentType, headers, responseContent,
                type, genericType);

        if (codecResult == null) {
            throw new CodecException("CodecResult should never be null!"
                    + " Please set correct decoder to the request!"
                    + " decoder of request : " + decoderOfRequest
                    + " , headers of responses : " + headers
                    + " , type : " + type
                    + " , genericType : " + genericType);
        }

        if (codecResult.isSuccess()) {
            return codecResult.getResult();
        }

        throw new CodecException("Decode is not success by decoderOfRequest,"
                + " Please set correct decoder to the request!"
                + " decoder of request : " + decoderOfRequest
                + " , headers of responses : " + headers
                + " , type : " + type
                + " , genericType : " + genericType);
    }

    private Object decodeByDecodersOfClient() throws Exception {
        HttpHeaders headers = response.headers();

        for (Decoder decoder : decodersOfClient) {
            CodecResult<?> codecResult = decoder.decode(contentType, headers, responseContent,
                    type, genericType);

            if (codecResult.isSuccess()) {
                return codecResult.getResult();
            }
        }
        throw new CodecException("There is no suitable decoder for this response in those decoders of client,"
                + " Please add correct decoder to the client!"
                + " , headers of responses : " + headers
                + " , type : " + type
                + " , genericType : " + genericType);
    }

}
