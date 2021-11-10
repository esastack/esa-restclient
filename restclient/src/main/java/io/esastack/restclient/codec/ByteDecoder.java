package io.esastack.restclient.codec;

public interface ByteDecoder extends Decoder {

    @SuppressWarnings("unchecked")
    @Override
    default Object decode(DecodeContext<?> decodeContext) throws Exception {
        if (decodeContext.content().value() instanceof byte[]) {
            return doDecode((DecodeContext<byte[]>) decodeContext);
        }
        return decodeContext.next();
    }

    Object doDecode(DecodeContext<byte[]> decodeContext) throws Exception;

}
