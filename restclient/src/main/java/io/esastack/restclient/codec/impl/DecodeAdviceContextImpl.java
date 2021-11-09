package io.esastack.restclient.codec.impl;

import esa.commons.Checks;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.ClientInnerComposition;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestRequestBase;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.codec.DecodeAdvice;
import io.esastack.restclient.codec.DecodeAdviceContext;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.ResponseContent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.lang.reflect.Type;

public final class DecodeAdviceContextImpl implements DecodeAdviceContext {

    private final RestRequest request;
    private final RestResponse response;
    private final DecodeAdvice[] advices;
    private final Decoder decoderOfRequest;
    private final Decoder[] decodersOfClient;

    private final Class<?> type;
    private final Type genericType;
    private int adviceIndex = 0;
    private ResponseContent responseContent;

    public DecodeAdviceContextImpl(RestRequestBase request,
                                   RestResponse response,
                                   ClientInnerComposition clientInnerComposition,
                                   Class<?> type,
                                   Type genericType,
                                   ByteBuf byteBuf) {
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(response, "response");
        Checks.checkNotNull(clientInnerComposition, "clientInnerComposition");
        Checks.checkNotNull(type, "type");
        Checks.checkNotNull(byteBuf, "byteBuf");
        this.request = request;
        this.response = response;
        this.advices = clientInnerComposition.decodeAdvices();
        this.decoderOfRequest = request.decoder();
        this.decodersOfClient = clientInnerComposition.decoders();
        this.type = type;
        this.genericType = genericType;
        this.responseContent = ResponseContent.of(ByteBufUtil.getBytes(byteBuf));
    }

    @Override
    public RestRequest request() {
        return request;
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
            MediaType contentType = response.contentType();
            if (decoderOfRequest != null) {
                return new DecodeContextImpl<>(
                        contentType,
                        response.headers(),
                        responseContent,
                        type,
                        genericType,
                        new Decoder[]{decoderOfRequest}
                ).continueToDecode();

            } else {
                return new DecodeContextImpl<>(
                        contentType,
                        response.headers(),
                        responseContent,
                        type,
                        genericType,
                        decodersOfClient
                ).continueToDecode();
            }
        }

        return advices[adviceIndex++].aroundDecode(this);
    }

}
