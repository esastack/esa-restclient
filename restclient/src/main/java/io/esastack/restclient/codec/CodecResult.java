package io.esastack.restclient.codec;

import io.esastack.restclient.codec.impl.CodecResultImpl;

public interface CodecResult<T> {

    /**
     * Identify whether the result of codec is success
     *
     * @return whether the result of codec is success
     */
    boolean isSuccess();

    /**
     * Obtain the codec result
     *
     * @return the codec result
     */
    T getResult();

    static <T> CodecResult<T> success(T result) {
        return new CodecResultImpl<>(true, result);
    }

    static <T> CodecResult<T> fail() {
        return new CodecResultImpl<>(false, null);
    }
}
