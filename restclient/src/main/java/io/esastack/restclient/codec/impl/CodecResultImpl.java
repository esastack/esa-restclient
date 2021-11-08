package io.esastack.restclient.codec.impl;

import io.esastack.restclient.codec.CodecResult;

public class CodecResultImpl<T> implements CodecResult<T> {

    private boolean isSuccess;
    private T result;

    public CodecResultImpl(boolean isSuccess, T result) {
        this.isSuccess = isSuccess;
        this.result = result;
    }

    @Override
    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public T getResult() {
        return result;
    }
}
