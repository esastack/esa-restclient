package io.esastack.restclient.codec.impl;

import io.esastack.httpclient.core.util.Ordered;
import io.esastack.restclient.codec.ByteCodec;
import io.esastack.restclient.codec.DecodeContext;
import io.esastack.restclient.codec.EncodeContext;
import io.esastack.restclient.codec.RequestContent;

public class ByteToByteCodec implements ByteCodec {

    @Override
    public RequestContent<byte[]> doEncode(EncodeContext<byte[]> encodeContext) throws Exception {
        Class<?> type = encodeContext.type();
        if (type.isArray() && type.getComponentType().equals(byte.class)) {
            return RequestContent.of((byte[]) encodeContext.entity());
        }

        return encodeContext.next();
    }

    @Override
    public Object doDecode(DecodeContext<byte[]> decodeContext) throws Exception {
        Class<?> type = decodeContext.type();
        if (type.isArray() && type.getComponentType().equals(byte.class)) {
            return decodeContext.content().value();
        }

        return decodeContext.next();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWER_PRECEDENCE;
    }

}