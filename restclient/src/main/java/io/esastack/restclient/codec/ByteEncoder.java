package io.esastack.restclient.codec;

public interface ByteEncoder extends Encoder<byte[]> {

    @Override
    default RequestContent<byte[]> encode(EncodeContext<byte[]> encodeContext) throws Exception {
        return doEncode(encodeContext);
    }

    RequestContent<byte[]> doEncode(EncodeContext<byte[]> encodeContext) throws Exception;
}
