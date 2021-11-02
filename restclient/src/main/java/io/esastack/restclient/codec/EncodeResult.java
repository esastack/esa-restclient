package io.esastack.restclient.codec;

import io.esastack.restclient.CodecResult;

/**
 * EncodeResult is designed as a carrier of encoded result.
 *
 */
public final class EncodeResult extends CodecResult<RequestBody<?>> {

    private EncodeResult(boolean isSuccess, RequestBody<?> result) {
        super(isSuccess, result);
    }

    public static EncodeResult fail() {
        return new EncodeResult(false, null);
    }

    public static EncodeResult success(RequestBody<?> result) {
        return new EncodeResult(true, result);
    }
}
