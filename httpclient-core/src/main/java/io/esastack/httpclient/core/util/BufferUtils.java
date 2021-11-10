package io.esastack.httpclient.core.util;

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.buffer.BufferUtil;
import io.netty.buffer.ByteBuf;

public class BufferUtils {

    private BufferUtils() {

    }

    public static ByteBuf toByteBuf(Buffer buffer) {
        Object unwrap = BufferUtil.unwrap(buffer);
        if (unwrap instanceof ByteBuf) {
            return (ByteBuf) unwrap;
        }
        throw new UnsupportedOperationException("The type of unwrap is not ByteBuf! unwrap : " + unwrap);
    }

}
