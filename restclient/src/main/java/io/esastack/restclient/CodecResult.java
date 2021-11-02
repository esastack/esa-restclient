package io.esastack.restclient;

public class CodecResult<T> {

    private boolean isSuccess;
    private T result;

    protected CodecResult(boolean isSuccess, T result) {
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
}
