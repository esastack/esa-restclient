package io.esastack.restclient.codec;

public final class CodecResult<T> {

    private boolean isSuccess;
    private T result;

    private CodecResult(boolean isSuccess, T result) {
        this.isSuccess = isSuccess;
        this.result = result;
    }

    /**
     * Identify whether the result of codec is success
     *
     * @return whether the result of codec is success
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Obtain the codec result
     *
     * @return the codec result
     */
    public T getResult() {
        return result;
    }

    public static <T> CodecResult<T> success(T result) {
        return new CodecResult<>(true, result);
    }

    public static <T> CodecResult<T> fail() {
        return new CodecResult<>(false, null);
    }
}
