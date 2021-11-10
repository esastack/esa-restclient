package io.esastack.restclient.codec;

public interface ByteEncoder extends Encoder {

    @SuppressWarnings("unchecked")
    @Override
    default RequestContent<?> encode(EncodeContext<?> encodeContext) throws Exception {
        return doEncode((EncodeContext<byte[]>) encodeContext);
    }

    RequestContent<byte[]> doEncode(EncodeContext<byte[]> encodeContext) throws Exception;
}
