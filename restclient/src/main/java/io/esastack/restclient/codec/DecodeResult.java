package io.esastack.restclient.codec;

import io.esastack.restclient.CodecResult;

/**
 * DecodeResult is designed as a carrier of decoded result.
 *
 * @param <T> the type of decoded result
 */
public final class DecodeResult<T> extends CodecResult<T> {

    private DecodeResult(boolean isSuccess, T result) {
        super(isSuccess, result);
    }

    public static DecodeResult<?> fail() {
        return new DecodeResult<>(false, null);
    }

    public static <T> DecodeResult<T> success(T result) {
        return new DecodeResult<>(true, result);
    }

}
