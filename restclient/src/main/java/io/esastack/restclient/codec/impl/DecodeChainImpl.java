package io.esastack.restclient.codec.impl;

import esa.commons.Checks;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.MediaType;
import io.esastack.restclient.ClientInnerComposition;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestRequestBase;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.codec.DecodeAdvice;
import io.esastack.restclient.codec.DecodeAdviceContext;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.Decoder;
import io.esastack.restclient.codec.ResponseContent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.CodecException;

import java.lang.reflect.Type;

public final class DecodeChainImpl implements DecodeAdviceContext, DecodeContext {

    private final RestRequest request;
    private final RestResponse response;
    private final DecodeAdvice[] advices;
    private final Decoder[] decoders;
    private final Class<?> type;
    private final Type genericType;
    private int adviceIndex = 0;
    private int decodeIndex = 0;
    private boolean decodeHadStart = false;
    private ResponseContent<?> responseContent;

    public DecodeChainImpl(RestRequestBase request,
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
        this.type = type;
        this.genericType = genericType;
        this.responseContent = ResponseContent.of(ByteBufUtil.getBytes(byteBuf));
        Decoder decoderOfRequest = request.decoder();
        if (decoderOfRequest == null) {
            this.decoders = clientInnerComposition.decoders();
        } else {
            this.decoders = new Decoder[]{decoderOfRequest};
        }
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
    public MediaType contentType() {
        return response.contentType();
    }

    @Override
    public HttpHeaders headers() {
        return response.headers();
    }

    @Override
    public ResponseContent<?> content() {
        return responseContent;
    }

    @Override
    public void content(ResponseContent<?> responseContent) {
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
    public Object next() throws Exception {
        if (this.decodeHadStart) {
            return decode();
        }

        if (advices == null || adviceIndex >= advices.length) {
            this.decodeHadStart = true;
            return decode();
        }

        return advices[adviceIndex++].aroundDecode(this);
    }

    private Object decode() throws Exception {
        if (decodeIndex < decoders.length) {
            return decoders[decodeIndex++].decode(this);
        }

        throw new CodecException("There is no suitable decoder for this response,"
                + " Please set correct decoder!"
                + " , headers of response : " + headers()
                + " , expected type : " + type
                + " , expected genericType : " + genericType);
    }

}
