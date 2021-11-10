package io.esastack.restclient.codec;

public interface ByteDecoder extends Decoder<byte[]> {

    @Override
    default Object decode(DecodeContext<byte[]> decodeContext) throws Exception {
        if (decodeContext.content().value() instanceof byte[]) {
            return doDecode(decodeContext);
        }
        return decodeContext.next();
    }

    Object doDecode(DecodeContext<byte[]> decodeContext) throws Exception;

}
